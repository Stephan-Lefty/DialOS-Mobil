package org.dialos.mobil

/**
 * Entscheidet, welche Stimme benutzt wird - und vor allem, wann eine
 * gespeicherte Stimme als verloren gelten darf.
 *
 * **Der Anlass, 27./28.10.2026.** Eine Testperson meldete nach einem
 * Aussetzer der Sprachausgabe: „Nur die Stimme, die ich geändert habe, war
 * nicht mehr da. Musste diese erneut einstellen." Der Grund stand in
 * [Speaker.applyPreferences]: Fand sich die gewünschte Stimme nicht in
 * `tts.voices`, setzte die App `prefs.voiceName = null` - sie **löschte die
 * Einstellung**. Ausgelöst wurde das von einer Liste, die bloß noch leer
 * war, weil die Sprach-Engine gerade erst hochkam.
 *
 * Ein vorübergehender Zustand zerstörte damit dauerhaft eine Einstellung.
 * Für jemanden, der den Bildschirm nicht sieht, ist das Nachstellen einer
 * Stimme keine Kleinigkeit.
 *
 * Die Unterscheidung, um die es geht: **"Liste leer" heißt nicht "Stimme
 * weg".** Eine leere Liste sagt nichts über die Stimme aus, sondern nur
 * über den Zeitpunkt der Frage.
 */
object StimmenWahl {

    enum class Ergebnis {
        /** Keine Stimme gewählt - die Voreinstellung der Engine gilt. */
        STANDARD,

        /** Die gewünschte Stimme ist da und wird gesetzt. */
        GEWUENSCHTE,

        /**
         * Die Engine hat noch keine Liste geliefert. Standard sprechen,
         * aber die Einstellung behalten und beim nächsten Satz erneut
         * fragen.
         */
        SPAETER_NOCHMAL,

        /**
         * Die Liste ist da, die Stimme steht nicht darin - sie ist
         * tatsächlich verschwunden (Engine gewechselt, Sprachpaket
         * gelöscht). Standard sprechen.
         *
         * **Die Einstellung wird trotzdem nicht gelöscht.** Sie gehört dem
         * Nutzer, nicht der App. Kommt das Sprachpaket zurück, greift sie
         * beim nächsten Start wieder; und wer sie ändern will, findet sie
         * dort, wo er sie hinterlassen hat.
         */
        NICHT_VORHANDEN,
    }

    fun entscheide(gewuenscht: String?, verfuegbar: List<String>?): Ergebnis = when {
        gewuenscht == null -> Ergebnis.STANDARD
        verfuegbar.isNullOrEmpty() -> Ergebnis.SPAETER_NOCHMAL
        gewuenscht in verfuegbar -> Ergebnis.GEWUENSCHTE
        else -> Ergebnis.NICHT_VORHANDEN
    }
}
