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
| `dialos-mobil-neuigkeit.py` | Die beiden Beiträge `/dialos-mobil-tester-gesucht/` (deutsch) und `/dialos-mobil-testers-wanted/` (englisch) samt Einträgen auf `/neuigkeiten/`. Das Formular steckt direkt in beiden. **In einem Punkt überholt** – siehe unten. |
| `dialos-mobil-testbericht.py` | Die beiden Beiträge zu den ersten Rückmeldungen aus dem geschlossenen Test (05.09.2026), samt Einträgen auf allen vier Übersichtsseiten. **Aktuelles Muster** – hiervon abschauen, nicht von `dialos-mobil-neuigkeit.py`. |
| `dialos-kommentare-einstellen.py` | Einmaliges Aufräumen: schließt Kommentare bei englischen Beiträgen, wirft Selbst-Pingbacks in den Papierkorb. |
| `wp-plugin/dialos-kommentare/` | WordPress-Plugin für den Dauerbetrieb (siehe unten). |
| `wp_zugang.py` | Gemeinsamer Zugang, wird von den anderen importiert. |

Reihenfolge beim Neuaufbau von null: erst `dialos-mobil-tester-gesucht.py`
(erzeugt Formular und Bilder), dann `dialos-mobil-neuigkeit.py` (verweist
darauf).

## Die Website hat sich am 25.08.2026 umgebaut

Wer hier ein Skript schreibt, muss zwei Änderungen kennen. `dialos-mobil-neuigkeit.py`
stammt von davor und würde heute an der Sicherheitsabfrage abbrechen – das ist
Absicht, kein Fehler.

**Englische Beiträge liegen unter `/en/`.** Früher lagen beide Sprachen flach
nebeneinander (`/testers-wanted/`), heute ist es `/en/testers-wanted/`. Wer die
alte Form verlinkt, verlinkt ins Leere.

**Statt einer zweispaltigen Seite gibt es vier einsprachige:**

| Seite | id | Wofür |
|---|---|---|
| `/neuigkeiten/` | 184 | deutsch, aktuell – höchstens **5** Einträge |
| `/aeltere-neuigkeiten/` | 258 | deutsches Archiv |
| `/en/news/` | 363 | englisch, aktuell – höchstens **5** Einträge |
| `/en/older-news/` | 364 | englisches Archiv |

Die Einträge stehen in **einem** `wp:html`-Block als
`<div class="dialos-news-entry">` innerhalb von `<div class="dialos-news-list">`.
Neuester oben; was über den fünften hinausgeht, wandert oben ins Archiv. Die
Verweise zwischen Übersicht und Archiv am Anfang bzw. Ende der Seiten dürfen
**nie** entfernt oder verdoppelt werden.

**Jeder Beitrag beginnt mit dem Sprachmarker:**

```html
<!-- wp:paragraph {"className":"dialos-lang-marker"} -->
<p class="dialos-lang-marker"><a href="…andere Sprachfassung…">English</a></p>
<!-- /wp:paragraph -->
```

Daran erkennt der tägliche Blog-Abgleich (geplante Aufgabe `dialos-blog-sync`,
werktags 15:03), welche zwei Beiträge zusammengehören. Fehlt er, bleibt der
Beitrag bei der automatischen Pflege außen vor. Beim englischen Beitrag steht
der `wp:html`-Block mit der eigenen Meta-Zeile **davor**.

Weil dieselbe geplante Aufgabe die Übersichtsseiten ohnehin pflegt, ist das
Einfügen von Hand nur nötig, wenn ein Beitrag sofort sichtbar sein soll.
Beides ist verträglich: Der Abgleich erkennt einen bereits vorhandenen
Eintrag an seiner URL und legt ihn nicht doppelt an.

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
