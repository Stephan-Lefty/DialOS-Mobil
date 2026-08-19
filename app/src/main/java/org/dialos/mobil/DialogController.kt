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

    /** Nimmt den Text einer Kurznachricht entgegen. */
    DICTATING_MESSAGE,

    /** Liest mehrere Kontaktvorschläge vor. */
    CHOOSING,

    /** Wartet auf "Ja" oder "Nein" vor dem Anruf oder dem Absenden. */
    CONFIRMING,

    /** Der Anruf läuft - die Erkennung pausiert. */
    CALLING,

    /** Die Nachricht ist unterwegs, das Netz hat noch nicht quittiert. */
    SENDING,

    /** Fragt bei zwei Karten, über welche telefoniert/geschrieben wird. */
    CHOOSING_SIM
}

/** Was der Nutzer vorhat: anrufen oder schreiben. */
private enum class Action { CALL, MESSAGE }

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

        fun onSendMessage(entry: PhoneEntry?, rawNumber: String, text: String, subscriptionId: Int?)
    }

    private val handler = Handler(Looper.getMainLooper())

    var state: DialogState = DialogState.WAITING_FOR_WAKE
        private set

    private var action = Action.CALL
    private var candidates: List<PhoneEntry> = emptyList()
    private var candidateIndex = 0
    private var choices: List<ContactMatch> = emptyList()
    private var dictatedDigits = StringBuilder()
    private var messageText = StringBuilder()
    private var messageTarget: PhoneEntry? = null
    private var lastPrompt: String = ""

    /** Karten, aus denen gerade gewählt wird, und was danach passieren soll. */
    private var simChoices: List<SimCard> = emptyList()
    private var afterSimChosen: ((Int?) -> Unit)? = null

    private val timeoutRunnable = Runnable {
        if (state != DialogState.WAITING_FOR_WAKE &&
            state != DialogState.CALLING &&
            state != DialogState.SENDING
        ) {
            say(context.getString(R.string.say_timeout)) { goIdle() }
        }
    }

    // -----------------------------------------------------------------------
    // Eingänge
    // -----------------------------------------------------------------------

    /** Startet den Dialog ohne Aktivierungswort (Schaltfläche, Kachel, Assistent). */
    fun activate() {
        if (state == DialogState.CALLING || state == DialogState.SENDING) return
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
            DialogState.DICTATING_MESSAGE -> handleDictatedMessage(text)
            DialogState.CHOOSING -> handleChoice(text)
            DialogState.CHOOSING_SIM -> handleSimChoice(text)
            DialogState.CONFIRMING -> handleConfirmation(text)
            DialogState.CALLING, DialogState.SENDING -> Unit
        }
    }

    /** Nach dem Auflegen wieder auf das Aktivierungswort warten. */
    fun onCallEnded() {
        if (state == DialogState.CALLING) goIdle()
    }

    /** Rückmeldung des Mobilfunknetzes zum SMS-Versand. */
    fun onMessageResult(success: Boolean, error: String?) {
        if (state != DialogState.SENDING) return
        val spoken = if (success) {
            context.getString(R.string.say_message_sent)
        } else {
            context.getString(R.string.say_message_failed, error.orEmpty())
        }
        state = DialogState.ASKING_NAME
        say(spoken) { goIdle() }
    }

    fun reset() {
        cancelTimeout()
        action = Action.CALL
        candidates = emptyList()
        candidateIndex = 0
        choices = emptyList()
        dictatedDigits = StringBuilder()
        messageText = StringBuilder()
        messageTarget = null
        simChoices = emptyList()
        afterSimChosen = null
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

            Command.MessageMode -> {
                action = Action.MESSAGE
                say(context.getString(R.string.say_message_to_whom))
            }

            is Command.MessageName -> {
                action = Action.MESSAGE
                lookUp(command.name)
            }

            is Command.CallName -> {
                action = Action.CALL
                lookUp(command.name)
            }

            // Viele Nutzer sagen einfach nur den Namen. Steht bereits fest, dass
            // eine Nachricht geschrieben werden soll, bleibt es dabei.
            is Command.Unknown -> lookUp(command.text)

            else -> say(context.getString(R.string.say_not_understood))
        }
    }

    private fun lookUp(spokenName: String) {
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
                offer(matches[0])

            else -> askWhichContact(matches)
        }
    }

    private fun askWhichContact(matches: List<ContactMatch>) {
        choices = matches
        state = DialogState.CHOOSING
        publish()
        val sb = StringBuilder(context.getString(R.string.say_choose))
        matches.forEachIndexed { index, match ->
            sb.append(' ').append(context.getString(R.string.say_choose_item, index + 1, match.name))
        }
        sb.append(' ').append(context.getString(R.string.say_choose_ask))
        say(sb.toString())
    }

    private fun handleChoice(text: String) {
        when (val command = CommandParser.parse(text)) {
            Command.Cancel, Command.ShutDown -> cancel()
            Command.Repeat -> askWhichContact(choices)
            is Command.Choice -> {
                val match = choices.getOrNull(command.index - 1)
                if (match == null) {
                    say(context.getString(R.string.say_not_understood))
                } else {
                    offer(match)
                }
            }
            // Statt der Zahl wird oft der Name wiederholt.
            is Command.Unknown -> {
                val match = choices.maxByOrNull { NameMatcher.score(command.text, it.name) }
                if (match != null && NameMatcher.score(command.text, match.name) >= NameMatcher.THRESHOLD) {
                    offer(match)
                } else {
                    say(context.getString(R.string.say_not_understood))
                }
            }
            else -> say(context.getString(R.string.say_not_understood))
        }
    }

    private fun offer(match: ContactMatch) {
        if (match.entries.isEmpty()) {
            say(context.getString(R.string.say_no_number, match.name))
            return
        }
        // Für eine SMS zählen nur Nummern, die Kurznachrichten empfangen können.
        // Eine SMS an einen Festnetzanschluss verschwindet meist spurlos.
        candidates = if (action == Action.MESSAGE) {
            val mobile = match.entries.filter { it.typeLabel == mobileLabel }
            mobile.ifEmpty { match.entries }
        } else {
            match.entries
        }
        candidateIndex = 0

        if (action == Action.MESSAGE) {
            startMessageDictation()
        } else {
            proposeCurrentCandidate()
        }
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
    // Kurznachricht
    // -----------------------------------------------------------------------

    private fun startMessageDictation() {
        val entry = candidates.getOrNull(candidateIndex)
        if (entry == null) {
            say(context.getString(R.string.say_not_understood)) { backToAskingName() }
            return
        }
        messageTarget = entry
        messageText = StringBuilder()
        state = DialogState.DICTATING_MESSAGE
        publish()

        // Der Empfänger wird hier mitgesprochen: er ist damit bestätigt, ohne
        // dass vor UND nach dem Diktat gefragt werden muss.
        val intro = if (entry.typeLabel == mobileLabel) {
            context.getString(R.string.say_message_ask_text, entry.name)
        } else {
            context.getString(R.string.say_message_ask_text_landline, entry.name, entry.typeLabel)
        }
        say(intro)
    }

    private fun handleDictatedMessage(text: String) {
        when (CommandParser.parse(text)) {
            Command.Cancel, Command.ShutDown -> {
                cancel()
                return
            }

            Command.Clear -> {
                messageText = StringBuilder()
                say(context.getString(R.string.say_message_cleared))
                return
            }

            Command.Repeat -> {
                say(
                    if (messageText.isEmpty()) context.getString(R.string.say_message_empty)
                    else context.getString(R.string.say_message_so_far, messageText.toString())
                )
                return
            }

            Command.Done, Command.Yes -> {
                if (messageText.isEmpty()) {
                    say(context.getString(R.string.say_message_empty))
                } else {
                    confirmMessage()
                }
                return
            }

            else -> Unit
        }

        // Alles Übrige ist Text der Nachricht.
        if (messageText.isNotEmpty()) messageText.append(' ')
        messageText.append(text.trim())
        say(context.getString(R.string.say_message_so_far, messageText.toString()))
    }

    private fun confirmMessage() {
        val entry = messageTarget ?: return
        state = DialogState.CONFIRMING
        publish()
        // Der Text wird vor dem Absenden immer vorgelesen - die Erkennung
        // verhört sich bei freiem Text deutlich öfter als bei Befehlen.
        say(
            context.getString(
                R.string.say_message_confirm,
                entry.name,
                messageText.toString()
            )
        )
    }

    private fun sendMessage() {
        val entry = messageTarget ?: return
        val text = messageText.toString()
        withChosenSim { subscriptionId ->
            state = DialogState.SENDING
            publish()
            listener.onPauseRecognition(true)
            speaker.speak(context.getString(R.string.say_message_sending)) {
                listener.onSendMessage(entry, entry.number, text, subscriptionId)
            }
        }
    }

    // -----------------------------------------------------------------------
    // Bestätigung
    // -----------------------------------------------------------------------

    private fun handleConfirmation(text: String) {
        when (CommandParser.parse(text)) {
            Command.Yes, Command.Done -> when {
                action == Action.MESSAGE && messageTarget != null -> sendMessage()

                candidates.isNotEmpty() -> {
                    val entry = candidates[candidateIndex]
                    placeCall(entry, entry.number)
                }

                dictatedDigits.isNotEmpty() -> placeCall(null, dictatedDigits.toString())

                else -> say(context.getString(R.string.say_not_understood)) { backToAskingName() }
            }

            Command.No -> when {
                // Beim Diktat bedeutet "Nein" nicht Abbruch, sondern
                // "noch mal von vorn" - der Empfänger bleibt.
                action == Action.MESSAGE -> {
                    messageText = StringBuilder()
                    state = DialogState.DICTATING_MESSAGE
                    publish()
                    say(context.getString(R.string.say_message_cleared))
                }

                candidates.isNotEmpty() && candidateIndex + 1 < candidates.size -> {
                    candidateIndex++
                    say(context.getString(R.string.say_next_number)) { proposeCurrentCandidate() }
                }

                else -> say(context.getString(R.string.say_cancelled)) { backToAskingName() }
            }

            Command.Cancel, Command.ShutDown -> cancel()
            Command.Repeat -> say(lastPrompt)
            else -> say(context.getString(R.string.say_not_understood)) { say(lastPrompt) }
        }
    }

    // -----------------------------------------------------------------------
    // Rufnummer diktieren
    // -----------------------------------------------------------------------

    private fun startNumberDictation() {
        action = Action.CALL
        dictatedDigits = StringBuilder()
        candidates = emptyList()
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
                say(context.getString(R.string.say_no_digits))
                return
            }

            Command.Repeat -> {
                say(
                    if (dictatedDigits.isEmpty()) context.getString(R.string.say_no_digits)
                    else GermanNumbers.spellOut(dictatedDigits.toString())
                )
                return
            }

            Command.Done, Command.Yes -> {
                if (dictatedDigits.isEmpty()) {
                    say(context.getString(R.string.say_no_digits))
                } else if (!prefs.confirmBeforeCall) {
                    placeCall(null, dictatedDigits.toString())
                } else {
                    state = DialogState.CONFIRMING
                    publish()
                    say(
                        context.getString(
                            R.string.say_confirm_number,
                            GermanNumbers.spellOut(dictatedDigits.toString())
                        )
                    )
                }
                return
            }

            else -> Unit
        }

        val digits = GermanNumbers.toDigits(text)
        if (digits.isEmpty()) {
            say(context.getString(R.string.say_not_understood))
            return
        }
        dictatedDigits.append(digits)
        say(GermanNumbers.spellOut(dictatedDigits.toString()))
    }

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
            Command.Cancel, Command.ShutDown -> cancel()
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

    private val mobileLabel: String get() = context.getString(R.string.phone_type_mobile)

    private fun backToAskingName() {
        reset()
        state = DialogState.ASKING_NAME
        publish()
        say(context.getString(R.string.say_ready))
    }

    private fun cancel() {
        say(context.getString(R.string.say_cancelled)) { goIdle() }
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
        if (state != DialogState.WAITING_FOR_WAKE &&
            state != DialogState.CALLING &&
            state != DialogState.SENDING
        ) {
            val delay = if (state == DialogState.DICTATING_MESSAGE) {
                DICTATION_TIMEOUT_MS
            } else {
                TIMEOUT_MS
            }
            handler.postDelayed(timeoutRunnable, delay)
        }
    }

    private fun cancelTimeout() = handler.removeCallbacks(timeoutRunnable)

    private fun publish(spokenHint: String? = null) {
        listener.onDialogStateChanged(state, spokenHint)
    }

    private companion object {
        const val TAG = "DialogController"

        /** So lange darf es still bleiben, bevor die App von selbst aufhört. */
        const val TIMEOUT_MS = 15_000L

        /** Beim Diktieren denkt man länger nach als bei einer Ja/Nein-Frage. */
        const val DICTATION_TIMEOUT_MS = 30_000L
    }
}
