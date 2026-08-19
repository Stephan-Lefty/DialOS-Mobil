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
    private val prefs: Prefs,
    private val listener: Listener
) {

    interface Listener {
        fun onDialogStateChanged(state: DialogState, spokenHint: String?)

        /** true = Mikrofon anhalten (während der eigenen Ansage oder im Gespräch). */
        fun onPauseRecognition(paused: Boolean)

        fun onPlaceCall(entry: PhoneEntry?, rawNumber: String)
    }

    private val handler = Handler(Looper.getMainLooper())

    var state: DialogState = DialogState.WAITING_FOR_WAKE
        private set

    private var candidates: List<PhoneEntry> = emptyList()
    private var candidateIndex = 0
    private var choices: List<ContactMatch> = emptyList()
    private var dictatedDigits = StringBuilder()
    private var lastPrompt: String = ""

    private val timeoutRunnable = Runnable {
        if (state != DialogState.WAITING_FOR_WAKE && state != DialogState.CALLING) {
            say(context.getString(R.string.say_timeout)) { goIdle() }
        }
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
    // Zustände
    // -----------------------------------------------------------------------

    private fun handleName(text: String) {
        when (val command = CommandParser.parse(text)) {
            Command.Cancel, Command.ShutDown -> cancel()
            Command.Help -> say(context.getString(R.string.say_help))
            Command.Repeat -> say(lastPrompt.ifEmpty { context.getString(R.string.say_ready) })
            Command.DialNumber -> startNumberDictation()
            is Command.CallName -> lookUp(command.name)
            // Viele Nutzer sagen einfach nur den Namen.
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
        candidates = match.entries
        candidateIndex = 0
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

    private fun handleConfirmation(text: String) {
        when (CommandParser.parse(text)) {
            Command.Yes, Command.Done -> {
                val entry = candidates.getOrNull(candidateIndex)
                if (entry != null) {
                    placeCall(entry, entry.number)
                } else if (dictatedDigits.isNotEmpty()) {
                    placeCall(null, dictatedDigits.toString())
                } else {
                    say(context.getString(R.string.say_not_understood)) { backToAskingName() }
                }
            }

            Command.No -> {
                if (candidates.isNotEmpty() && candidateIndex + 1 < candidates.size) {
                    candidateIndex++
                    say(context.getString(R.string.say_next_number)) { proposeCurrentCandidate() }
                } else {
                    say(context.getString(R.string.say_cancelled)) { backToAskingName() }
                }
            }

            Command.Cancel, Command.ShutDown -> cancel()
            Command.Repeat -> say(lastPrompt)
            else -> say(context.getString(R.string.say_not_understood)) { say(lastPrompt) }
        }
    }

    private fun startNumberDictation() {
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
        val spoken = entry?.name ?: GermanNumbers.spellOut(number)
        state = DialogState.CALLING
        publish()
        listener.onPauseRecognition(true)
        speaker.speak(context.getString(R.string.say_calling, spoken)) {
            listener.onPlaceCall(entry, number)
        }
    }

    private fun backToAskingName() {
        reset()
        state = DialogState.ASKING_NAME
        publish()
        say(context.getString(R.string.say_ready))
    }

    private fun cancel() {
        say(context.getString(R.string.say_cancelled)) { goIdle() }
    }

    // -----------------------------------------------------------------------
    // Hilfsmittel
    // -----------------------------------------------------------------------

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
            handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
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
    }
}
