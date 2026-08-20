# website/ – die Seiten zu DialOS Mobil auf dialos.org

Hier liegen die Werkzeuge, mit denen die Testersuche für DialOS Mobil auf
[dialos.org](https://dialos.org) veröffentlicht wird. Alles läuft über die
REST-Schnittstelle von WordPress, ohne Klicken im Backend – damit ist jede
Seite reproduzierbar und nachlesbar, statt einmalig von Hand gebaut.

**Alle Skripte sind beliebig oft ausführbar.** Sie legen nichts doppelt an,
sondern aktualisieren, was schon da ist. Wer einen Text ändern will, ändert
ihn hier und lässt das Skript erneut laufen – nicht im WordPress-Editor,
sonst überschreibt der nächste Lauf die Änderung.

## Abgrenzung zu DialOS

[DialOS](https://github.com/Stephan-Lefty/DialOS) (das Desktop-System) ist
ein eigenes Repo mit eigener Zeitrechnung. Die beiden Strecken bleiben
getrennt: **Was DialOS Mobil betrifft, gehört hierher.** Unter
`DialOS/Wordpressinstallation/` liegen weiterhin die Werkzeuge, die die
Website als Ganzes betreffen (Änderungsprotokoll-Abgleich, SEO, die
Datenschutzseite) – und dort liegt bislang auch die Zugangsdatei.

## Zugangsdaten

Stehen **nicht** im Repo. `wp_zugang.py` sucht sie der Reihe nach hier:

1. Pfad aus der Umgebungsvariablen `DIALOS_WP_ENV`
2. `website/.env` (steht in `.gitignore`)
3. `~/.config/dialos/wordpress.env`
4. `DialOS/Wordpressinstallation/.env` – der bisherige Ort

Aufbau:

```
WP_URL=https://dialos.org
WP_USER=ClaudIA
WP_APP_PASSWORD=xxxx xxxx xxxx xxxx xxxx xxxx
```

Das Passwort ist ein **Anwendungspasswort** aus dem WordPress-Profil, kein
Anmeldepasswort. Es lässt sich dort einzeln zurückziehen, ohne dass sonst
etwas kaputtgeht.

## Was wovon kommt

| Datei | Legt an / ändert |
|---|---|
| `dialos-mobil-tester-gesucht.py` | Die vier Screenshots in der Mediathek und das Anmeldeformular (Contact Form 7, id 205, Hash `b923396`). **Quelle der Wahrheit für das Formular.** |
| `dialos-mobil-neuigkeit.py` | Die beiden Beiträge `/dialos-mobil-tester-gesucht/` (deutsch) und `/dialos-mobil-testers-wanted/` (englisch) samt Einträgen auf `/neuigkeiten/`. Das Formular steckt direkt in beiden. |
| `dialos-kommentare-einstellen.py` | Einmaliges Aufräumen: schließt Kommentare bei englischen Beiträgen, wirft Selbst-Pingbacks in den Papierkorb. |
| `wp-plugin/dialos-kommentare/` | WordPress-Plugin für den Dauerbetrieb (siehe unten). |
| `wp_zugang.py` | Gemeinsamer Zugang, wird von den anderen importiert. |

Reihenfolge beim Neuaufbau von null: erst `dialos-mobil-tester-gesucht.py`
(erzeugt Formular und Bilder), dann `dialos-mobil-neuigkeit.py` (verweist
darauf).

## Das Plugin

`wp-plugin/dialos-kommentare/` regelt zweierlei dauerhaft:

- **Deutsche Beiträge** bekommen einen durchgehend deutschen
  Kommentarbereich. Das Theme *wlow* gibt drei Texte fest auf Englisch aus
  (`1 Response`, `Submit Comment`, `Comments RSS Feed`); die werden
  übersetzt. Ins Theme geschrieben wäre das beim nächsten Update spurlos
  weg.
- **Englische Beiträge** bekommen gar keinen Kommentarbereich. Die Website
  läuft auf `de_DE`, dort käme sonst alles auf Deutsch heraus
  (`Schreibe einen Kommentar`, `Antworten`, `sagt:`, deutsches Datum).
  Erkannt werden sie am Sprachumschalter „Deutsch" oben im Beitrag – der
  Konvention, die auf dialos.org ohnehin gilt.

Außerdem unterbindet es Selbst-Pingbacks: Jedes Beitragspaar verlinkt
gegenseitig aufeinander, woraus WordPress sonst einen Kommentar macht, der
unter dem Text wie eine echte Wortmeldung aussieht.

Installieren als ZIP über *Plugins → Installieren → Plugin hochladen*:

```bash
cd website/wp-plugin && zip -r dialos-kommentare.zip dialos-kommentare
```

Das ZIP steht bewusst nicht im Repo – es wäre eine Kopie, die still veraltet.

## Zwei Fallen im REST-Zugang von Contact Form 7

Beide am 20.08.2026 an CF7 6.1.7 herausgefunden, beide von der Sorte, die
wie Erfolg aussieht:

1. **Ohne `?context=save` speichert der Endpunkt nicht.** Er antwortet
   trotzdem mit HTTP 200 und einem vollständig aussehenden Formular – nur
   `"id": null`. Wer das ungeprüft weiterverwendet, schreibt `id="None"` in
   den Shortcode und veröffentlicht eine Seite mit „Kontaktformular wurde
   nicht gefunden".
2. **Beim Schreiben gehören `form`, `mail`, `mail_2`, `messages` auf die
   oberste Ebene**, nicht unter `properties` – obwohl das Lesen sie genau
   dort zurückgibt. Unter `properties` werden sie stillschweigend verworfen,
   und man bekommt die CF7-Standardvorlage unter eigenem Titel.

`dialos-mobil-tester-gesucht.py` prüft deshalb nach dem Speichern nach, ob
wirklich die eigenen Felder drinstehen, und bricht sonst ab.

## Barrierefreiheit ist hier kein Beiwerk

Drei Entscheidungen, die aus der Zielgruppe folgen und nicht aus Geschmack:

- **Alternativtexte der Screenshots beschreiben das Bild**, nicht die Datei.
  Sie werden vorgelesen.
- **Der Honeypot gegen Spam ist per CSS-`clip` versteckt, nicht per
  `display:none`.** Ein Screenreader liest ihn samt Label „Bitte lasse
  dieses Feld leer." vor. Wäre er ganz verborgen, würde ein blinder Nutzer
  ihn womöglich ausfüllen – und seine Anmeldung wäre lautlos verworfen.
  Falls der Honeypot je ersetzt wird: dieses Verhalten nachprüfen.
- **Keine doppelten Überschriften.** Das Formular trägt keine eigene, weil
  darüber schon eine `h2` steht; doppelt heißt für einen Screenreader
  denselben Satz zweimal.
