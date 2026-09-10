package org.dialos.mobil

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log

/** Wo im Gespräch die App gerade steht. */
enum class DialogState {
    /** Wartet auf das Aktivierungswort. */
    WAITING_FOR_WAKE,

    /** Fragt nach einem Namen (oder einem anderen Befehl). */
    ASKING_NAME,

    /** Nimmt eine diktierte Rufnummer entgegen. */
    ASKING_NUMBER,

    /** Liest mehrere Kontaktvorschläge vor. */
    CHOOSING,

    /** Fragt bei zwei Karten, über welche telefoniert wird. */
    CHOOSING_SIM,

    /** Wartet auf "Ja" oder "Nein" vor dem Anruf. */
    CONFIRMING,

    /** Der Anruf läuft - die Erkennung pausiert. */
    CALLING
}

/**
 * Der eigentliche Gesprächsablauf.
 *
 * Kennt weder Mikrofon noch Telefonie: beides läuft über [Listener], damit
 * sich der Ablauf unabhängig vom Android-Drumherum nachvollziehen lässt.
 */
class DialogController(
    private val context: Context,
    private val speaker: Speaker,
    private val contacts: ContactRepository,
    private val sims: SimRepository,
    private val prefs: Prefs,
    private val listener: Listener
) {

    interface Listener {
        fun onDialogStateChanged(state: DialogState, spokenHint: String?)

        /** true = Mikrofon anhalten (während der eigenen Ansage oder im Gespräch). */
        fun onPauseRecognition(paused: Boolean)

        fun onPlaceCall(entry: PhoneEntry?, rawNumber: String, subscriptionId: Int?)
    }

    private val handler = Handler(Looper.getMainLooper())

    var state: DialogState = DialogState.WAITING_FOR_WAKE
        private set

    private var candidates: List<PhoneEntry> = emptyList()
    private var candidateIndex = 0
    private var choices: List<ContactMatch> = emptyList()
    private var dictatedDigits = StringBuilder()
    private var lastPrompt: String = ""

    /** Merkt einen genannten Nummerntyp über die Kontaktauswahl hinweg. */
    private var wantedKind: PhoneKind? = null

    /** Wie viele Ziffernblöcke bisher diktiert wurden - steuert die Führung. */
    private var blockCount = 0

    /** Karten, aus denen gerade gewählt wird, und was danach passieren soll. */
    private var simChoices: List<SimCard> = emptyList()
    private var afterSimChosen: ((Int?) -> Unit)? = null

    private val timeoutRunnable = Runnable {
        if (state == DialogState.WAITING_FOR_WAKE || state == DialogState.CALLING) {
            return@Runnable
        }
        // Eine halb diktierte Rufnummer ist zu teuer, um sie wegzuwerfen.
        // Bis 0.6.3 rief die Wartezeit hier goIdle() und damit reset() -
        // alle Ziffern waren weg, ohne Vorwarnung. Wer zehn Stellen
        // gesprochen hat, faengt nicht gern von vorn an.
        if (state == DialogState.ASKING_NUMBER && dictatedDigits.isNotEmpty()) {
            confirmDictatedNumber()
            return@Runnable
        }
        // Sonst hört die App nicht auf, sie geht nur zurück ins Lauschen.
        // Was sie ansagt, muss deshalb davon abhängen, ob das
        // Aktivierungswort überhaupt eingeschaltet ist - sonst schickt sie
        // den Nutzer zu einem Wort, auf das niemand hört.
        val text =
            if (prefs.hotwordEnabled) R.string.say_timeout
            else R.string.say_timeout_no_hotword
        say(context.getString(text)) { goIdle() }
    }

    // -----------------------------------------------------------------------
    // Eingänge
    // -----------------------------------------------------------------------

    /** Startet den Dialog ohne Aktivierungswort (Schaltfläche, Kachel, Assistent). */
    fun activate() {
        if (state == DialogState.CALLING) return
        reset()
        state = DialogState.ASKING_NAME
        publish()
        say(context.getString(R.string.say_ready))
    }

    /** Ein erkannter Satz aus der Spracherkennung. */
    fun onPhrase(text: String) {
        if (text.isBlank()) return
        Log.i(TAG, "erkannt [$state]: $text")
        cancelTimeout()

        when (state) {
            DialogState.WAITING_FOR_WAKE ->
                if (prefs.hotwordEnabled && CommandParser.isWakePhrase(text)) activate() else Unit

            DialogState.ASKING_NAME -> handleName(text)
            DialogState.ASKING_NUMBER -> handleDictatedNumber(text)
            DialogState.CHOOSING -> handleChoice(text)
            DialogState.CHOOSING_SIM -> handleSimChoice(text)
            DialogState.CONFIRMING -> handleConfirmation(text)
            DialogState.CALLING -> Unit
        }
    }

    /** Nach dem Auflegen wieder auf das Aktivierungswort warten. */
    fun onCallEnded() {
        if (state == DialogState.CALLING) goIdle()
    }

    fun reset() {
        cancelTimeout()
        candidates = emptyList()
        candidateIndex = 0
        choices = emptyList()
        dictatedDigits = StringBuilder()
        simChoices = emptyList()
        afterSimChosen = null
        wantedKind = null
        blockCount = 0
    }

    fun goIdle() {
        reset()
        state = DialogState.WAITING_FOR_WAKE
        publish()
        listener.onPauseRecognition(false)
    }

    fun shutdown() {
        cancelTimeout()
        speaker.stop()
    }

    // -----------------------------------------------------------------------
    // Namen und Auswahl
    // -----------------------------------------------------------------------

    private fun handleName(text: String) {
        when (val command = CommandParser.parse(text)) {
            Command.Cancel, Command.ShutDown -> cancel()
            Command.Help -> say(context.getString(R.string.say_help))
            Command.Repeat -> say(lastPrompt.ifEmpty { context.getString(R.string.say_ready) })
            Command.DialNumber -> startNumberDictation()
            is Command.CallName -> lookUp(command.name, command.kind)
            // Viele Nutzer sagen einfach nur den Namen.
            is Command.Unknown -> lookUp(command.text)
            else -> say(context.getString(R.string.say_not_understood))
        }
    }

    private fun lookUp(spokenName: String, kind: PhoneKind? = null) {
        if (!contacts.hasPermission() || contacts.isEmpty) {
            say(context.getString(R.string.say_no_contacts)) { goIdle() }
            return
        }
        val matches = contacts.find(spokenName)
        when {
            matches.isEmpty() ->
                say(context.getString(R.string.say_not_found, spokenName))

            matches.size == 1 ||
                matches[0].score - matches[1].score >= NameMatcher.CLEAR_WINNER_MARGIN ->
                offer(matches[0], kind)

            else -> {
                wantedKind = kind
                askWhichContact(matches, contacts.countMatches(spokenName))
            }
        }
    }

    /**
     * @param gesamt wie viele Kontakte insgesamt passen. Liegt die Zahl über
     *   der Zahl der Vorschläge, wird das ausdrücklich gesagt: Bis 0.6.9
     *   schnitt die App stillschweigend nach dem dritten ab, und wer fünf
     *   Kontakte namens Hans hatte, kam an zwei davon per Sprache nicht
     *   heran, ohne je zu erfahren, dass es sie gibt.
     */
    private fun askWhichContact(matches: List<ContactMatch>, gesamt: Int = matches.size) {
        choices = matches
        state = DialogState.CHOOSING
        publish()
        val sb = StringBuilder(
            if (gesamt > matches.size) {
                context.getString(R.string.say_choose_many, gesamt, matches.size)
            } else {
                context.getString(R.string.say_choose)
            }
        )
        matches.forEachIndexed { index, match ->
            sb.append(' ').append(context.getString(R.string.say_choose_item, index + 1, match.name))
        }
        sb.append(' ').append(
            context.getString(
                if (gesamt > matches.size) R.string.say_choose_ask_many
                else R.string.say_choose_ask
            )
        )
        say(sb.toString())
    }

    private fun handleChoice(text: String) {
        when (val command = CommandParser.parse(text)) {
            Command.ShutDown -> cancel()
            Command.Cancel -> cancelStep()
            Command.Repeat -> askWhichContact(choices)
            is Command.Choice -> {
                val match = choices.getOrNull(command.index - 1)
                if (match == null) {
                    say(context.getString(R.string.say_not_understood))
                } else {
                    offer(match, wantedKind)
                }
            }
            // Statt der Zahl wird oft der Name wiederholt.
            is Command.Unknown -> {
                val match = choices.maxByOrNull { NameMatcher.score(command.text, it.name) }
                if (match != null && NameMatcher.score(command.text, match.name) >= NameMatcher.THRESHOLD) {
                    offer(match, wantedKind)
                } else {
                    say(context.getString(R.string.say_not_understood))
                }
            }
            else -> say(context.getString(R.string.say_not_understood))
        }
    }

    private fun offer(match: ContactMatch, kind: PhoneKind? = null) {
        if (match.entries.isEmpty()) {
            say(context.getString(R.string.say_no_number, match.name))
            return
        }
        candidates = match.entries
        wantedKind = null
        // Wurde eine bestimmte Nummer verlangt ("privat"), damit anfangen -
        // sonst schlägt die App weiter die Mobilnummer vor und der Nutzer muss
        // sich durch alle Vorschläge nein-sagen.
        candidateIndex = kind?.let { wanted ->
            match.entries.indexOfFirst { it.kind == wanted }.takeIf { it >= 0 }
        } ?: 0
        if (kind != null && match.entries.none { it.kind == kind }) {
            say(context.getString(R.string.say_no_such_number, match.name)) {
                proposeCurrentCandidate()
            }
            return
        }
        proposeCurrentCandidate()
    }

    private fun proposeCurrentCandidate() {
        val entry = candidates.getOrNull(candidateIndex)
        if (entry == null) {
            say(context.getString(R.string.say_no_more)) { backToAskingName() }
            return
        }
        if (!prefs.confirmBeforeCall) {
            placeCall(entry, entry.number)
            return
        }
        state = DialogState.CONFIRMING
        publish()
        val prompt = if (candidates.size > 1) {
            context.getString(R.string.say_confirm_contact_labeled, entry.name, entry.typeLabel)
        } else {
            context.getString(R.string.say_confirm_contact, entry.name)
        }
        say(prompt)
    }

    // -----------------------------------------------------------------------
    // Bestätigung
    // -----------------------------------------------------------------------

    private fun handleConfirmation(text: String) {
        when (val command = CommandParser.parse(text)) {
            // "Nein, privat" - der Nutzer will nicht abbrechen, sondern eine
            // bestimmte andere Nummer desselben Kontakts.
            is Command.PickKind -> {
                val index = candidates.indexOfFirst { it.kind == command.kind }
                if (index >= 0) {
                    candidateIndex = index
                    proposeCurrentCandidate()
                } else {
                    val name = candidates.getOrNull(candidateIndex)?.name.orEmpty()
                    repeatQuestionAfter(context.getString(R.string.say_no_such_number, name))
                }
            }

            Command.Yes, Command.Done -> when {
                candidates.isNotEmpty() -> {
                    val entry = candidates[candidateIndex]
                    placeCall(entry, entry.number)
                }

                dictatedDigits.isNotEmpty() -> placeCall(null, dictatedDigits.toString())

                else -> say(context.getString(R.string.say_not_understood)) { backToAskingName() }
            }

            Command.No -> when {
                candidates.isNotEmpty() && candidateIndex + 1 < candidates.size -> {
                    candidateIndex++
                    say(context.getString(R.string.say_next_number)) { proposeCurrentCandidate() }
                }

                else -> say(context.getString(R.string.say_cancelled)) { backToAskingName() }
            }

            Command.ShutDown -> cancel()
            Command.Cancel -> cancelStep()
            Command.Repeat -> say(lastPrompt)
            else -> repeatQuestionAfter(context.getString(R.string.say_not_understood))
        }
    }

    /**
     * Sagt [note] und stellt danach die zuletzt gestellte Frage erneut.
     *
     * Der Umweg über die lokale Kopie ist nötig, weil [say] selbst
     * `lastPrompt` überschreibt - ohne ihn wiederholte die App den Hinweis
     * "Das habe ich nicht verstanden" statt der Frage und der Nutzer stand
     * vor einer Sackgasse.
     */
    private fun repeatQuestionAfter(note: String) {
        val question = lastPrompt
        say(note) { say(question) }
    }

    // -----------------------------------------------------------------------
    // Rufnummer diktieren
    // -----------------------------------------------------------------------

    private fun startNumberDictation() {
        dictatedDigits = StringBuilder()
        candidates = emptyList()
        blockCount = 0
        state = DialogState.ASKING_NUMBER
        publish()
        say(context.getString(R.string.say_ask_number))
    }

    private fun handleDictatedNumber(text: String) {
        when (CommandParser.parse(text)) {
            Command.Cancel, Command.ShutDown -> {
                cancel()
                return
            }

            Command.Clear -> {
                dictatedDigits = StringBuilder()
                blockCount = 0
                say(context.getString(R.string.say_cleared))
                return
            }

            Command.Undo -> {
                if (dictatedDigits.isEmpty()) {
                    say(context.getString(R.string.say_no_digits))
                } else {
                    dictatedDigits.deleteCharAt(dictatedDigits.length - 1)
                    say(
                        context.getString(
                            R.string.say_undone,
                            spellOutOrNothing(dictatedDigits.toString())
                        )
                    )
                }
                return
            }

            Command.Repeat -> {
                say(
                    if (dictatedDigits.isEmpty()) context.getString(R.string.say_no_digits)
                    else context.getString(
                        R.string.say_so_far,
                        GermanNumbers.spellOut(dictatedDigits.toString())
                    )
                )
                return
            }

            Command.Done, Command.Yes -> {
                confirmDictatedNumber()
                return
            }

            else -> Unit
        }

        val digits = GermanNumbers.toDigits(text)
        if (digits.isEmpty()) {
            say(context.getString(R.string.say_not_understood_digits))
            return
        }
        dictatedDigits.append(digits)
        blockCount++

        // Nur die NEUEN Ziffern zurücklesen, nicht die ganze bisherige
        // Nummer. Waehrend die App spricht, ist das Mikrofon aus - wer
        // fluessig weiterdiktiert, verlor bis 0.6.3 genau die Ziffern, die
        // in diese Ansage fielen. Genau das ist im Test passiert, zweimal.
        // Je kuerzer die Bestaetigung, desto kleiner das Loch.
        val bestaetigung = GermanNumbers.spellOut(digits)
        say(
            if (blockCount == 1) {
                // Die Anleitung zu Beginn ist bis hierher schon vergessen -
                // ein Tester hat genau das gemeldet. Deshalb steht sie hier
                // noch einmal, aber nur beim ersten Mal, damit sie nicht bei
                // jedem Block im Weg steht.
                context.getString(R.string.say_digits_first, bestaetigung)
            } else {
                bestaetigung
            }
        )
    }

    private fun confirmDictatedNumber() {
        if (dictatedDigits.isEmpty()) {
            say(context.getString(R.string.say_no_digits))
            return
        }
        if (!prefs.confirmBeforeCall) {
            placeCall(null, dictatedDigits.toString())
            return
        }
        state = DialogState.CONFIRMING
        publish()
        say(
            context.getString(
                R.string.say_confirm_number,
                GermanNumbers.spellOut(dictatedDigits.toString())
            )
        )
    }

    private fun spellOutOrNothing(digits: String): String =
        if (digits.isEmpty()) context.getString(R.string.say_nothing_left)
        else GermanNumbers.spellOut(digits)

    private fun placeCall(entry: PhoneEntry?, number: String) {
        withChosenSim { subscriptionId ->
            val spoken = entry?.name ?: GermanNumbers.spellOut(number)
            state = DialogState.CALLING
            publish()
            listener.onPauseRecognition(true)
            speaker.speak(context.getString(R.string.say_calling, spoken)) {
                listener.onPlaceCall(entry, number, subscriptionId)
            }
        }
    }

    // -----------------------------------------------------------------------
    // Kartenwahl bei zwei SIM/eSIM
    // -----------------------------------------------------------------------

    /**
     * Führt [action] aus - bei mehreren Karten aber erst, nachdem der Nutzer
     * gesagt hat, über welche. Bei einer Karte (oder fehlender Berechtigung)
     * wird nicht gefragt, sonst stünde bei jedem Anruf eine überflüssige
     * Rückfrage im Weg.
     */
    private fun withChosenSim(action: (Int?) -> Unit) {
        val cards = sims.activeSims()
        if (cards.size < 2) {
            action(null)
            return
        }
        simChoices = cards
        afterSimChosen = action
        state = DialogState.CHOOSING_SIM
        publish()
        val sb = StringBuilder(context.getString(R.string.say_which_sim))
        cards.forEachIndexed { index, card ->
            sb.append(' ').append(context.getString(R.string.say_choose_item, index + 1, card.label))
        }
        say(sb.toString())
    }

    private fun handleSimChoice(text: String) {
        when (val command = CommandParser.parse(text)) {
            Command.ShutDown -> cancel()
            Command.Cancel -> cancelStep()
            Command.Repeat -> say(lastPrompt)

            is Command.Choice -> {
                val card = simChoices.getOrNull(command.index - 1)
                if (card == null) say(context.getString(R.string.say_not_understood))
                else useSim(card)
            }

            // Statt der Zahl wird oft der Name des Anbieters gesagt.
            is Command.Unknown -> {
                val best = simChoices.maxByOrNull { NameMatcher.score(command.text, it.label) }
                if (best != null && NameMatcher.score(command.text, best.label) >= NameMatcher.THRESHOLD) {
                    useSim(best)
                } else {
                    say(context.getString(R.string.say_not_understood))
                }
            }

            else -> say(context.getString(R.string.say_not_understood))
        }
    }

    private fun useSim(card: SimCard) {
        val action = afterSimChosen ?: return
        afterSimChosen = null
        simChoices = emptyList()
        action(card.subscriptionId)
    }

    // -----------------------------------------------------------------------
    // Hilfsmittel
    // -----------------------------------------------------------------------

    private fun backToAskingName() {
        reset()
        state = DialogState.ASKING_NAME
        publish()
        say(context.getString(R.string.say_ready))
    }

    /**
     * Beendet das Gespräch ganz und sagt, wie man zurückkommt.
     *
     * Der Hinweis ist der eigentliche Punkt: Danach hört die App nur noch
     * auf das Aktivierungswort - wer einen Namen sagt, redet ins Leere. Bis
     * 0.6.9 stand hier nur "Abgebrochen.", und ein Tester meldete daraufhin,
     * die Spracherkennung sei kaputt. Sie war es nicht; sie hatte nur
     * verschwiegen, was sie jetzt erwartet. Ist das Aktivierungswort
     * abgeschaltet, führt der Satz zum großen Knopf, sonst zu einem Wort,
     * auf das niemand hört.
     */
    private fun cancel() {
        val text =
            if (prefs.hotwordEnabled) R.string.say_cancelled
            else R.string.say_cancelled_no_hotword
        say(context.getString(text)) { goIdle() }
    }

    /**
     * Bricht nur den aktuellen Schritt ab, nicht das ganze Gespräch.
     *
     * Wer mitten in einer Kontaktauswahl "Abbrechen" sagt, meint meistens
     * die Auswahl - nicht, dass er doch nicht telefonieren will. Genau das
     * hat ein Tester erwartet ("die Oberfläche sieht danach wieder so aus,
     * als könnte ich einen neuen Namen nennen"). Das Gespräch ganz beenden
     * kann man weiterhin mit "Sprachsteuerung beenden", und nach 15
     * Sekunden Stille geschieht es von selbst.
     */
    private fun cancelStep() {
        say(context.getString(R.string.say_cancelled_step)) { backToAskingName() }
    }

    /**
     * Spricht einen Satz und hält dabei das Mikrofon an. Danach läuft die
     * Erkennung weiter und die Abbruch-Zeit beginnt neu.
     */
    private fun say(text: String, then: (() -> Unit)? = null) {
        lastPrompt = text
        publish(text)
        listener.onPauseRecognition(true)
        speaker.speak(text) {
            if (then != null) {
                then()
            } else {
                listener.onPauseRecognition(false)
                armTimeout()
            }
        }
    }

    private fun armTimeout() {
        cancelTimeout()
        if (state != DialogState.WAITING_FOR_WAKE && state != DialogState.CALLING) {
            handler.postDelayed(timeoutRunnable, timeoutFor(state))
        }
    }

    /**
     * Eine Rufnummer zu diktieren dauert länger als eine Frage zu
     * beantworten - besonders, wenn man sie erst nachschlagen oder von einem
     * Zettel ablesen muss. Fünfzehn Sekunden reichten dafür nicht; ein
     * Tester ist genau daran gescheitert.
     */
    private fun timeoutFor(state: DialogState): Long = when (state) {
        DialogState.ASKING_NUMBER -> NUMBER_TIMEOUT_MS
        else -> TIMEOUT_MS
    }

    private fun cancelTimeout() = handler.removeCallbacks(timeoutRunnable)

    private fun publish(spokenHint: String? = null) {
        listener.onDialogStateChanged(state, spokenHint)
    }

    private companion object {
        const val TAG = "DialogController"

        /** So lange darf es still bleiben, bevor die App von selbst aufhört. */
        const val TIMEOUT_MS = 15_000L

        /** Beim Diktieren einer Rufnummer - siehe [timeoutFor]. */
        const val NUMBER_TIMEOUT_MS = 45_000L
    }
}
