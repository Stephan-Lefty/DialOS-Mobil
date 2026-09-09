[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – DialOS Mobil

## Offen

### Noch mit der Stimme zu prüfen

- [ ] **Kartenwahl im Gespräch** – die Erkennung der Karten ist auf dem
      Gerät bestätigt („1&1“ und „YELLLOW“), die gesprochene Abfrage selbst
      noch nicht. Prüfen, ob „Eins“ und der Anbietername beide greifen und
      der Anruf über die richtige Karte geht.
- [ ] **Aktivierungswort messen.** Beim Testen protokollierte die Erkennung
      einmal „sprach steigt“ – vermutlich ein verhörtes „Sprachsteuerung“,
      das `CommandParser.isWakePhrase` nicht durchgehen ließe. Mehrfach
      sagen, Protokoll auswerten, Schwellwerte an echten Daten justieren
      statt an einer Vermutung.

### Auf echter Hardware prüfen

- [ ] Erkennungsrate des Aktivierungsworts messen: Wie oft löst
      „Sprachsteuerung starten“ wirklich aus, wie oft löst normales Reden
      im Raum fälschlich aus? Ggf. Schwellwerte in `CommandParser` anpassen.
- [ ] Prüfen, ob die Erkennung während der eigenen Sprachausgabe wirklich
      still ist (Echo-Problem) – besonders über Lautsprecher.
- [ ] Akkuverbrauch über einen ganzen Tag messen.
- [ ] Verhalten mit Bluetooth-Headset testen (dasselbe AIRHUG 01 wie bei
      DialOS?) – nimmt Vosk dann das Headset-Mikrofon?
- [ ] Autostart nach Neustart auf Android 14/15 prüfen: greift die
      Ausweich-Benachrichtigung, und ist sie für einen blinden Nutzer
      überhaupt auffindbar?

### Funktionen

- [ ] Anruf annehmen per Sprache („Abheben“ / „Annehmen“) – aktuell kann
      die App nur anrufen, nicht abnehmen. Braucht `ANSWER_PHONE_CALLS`.
- [ ] Auflegen per Sprache.
- [ ] Lautsprecher automatisch einschalten (`EXTRA_START_CALL_WITH_SPEAKERPHONE`),
      damit ein blinder Nutzer das Telefon nicht ans Ohr halten muss –
      als Einstellung.
- [ ] Kurzwahl / Favoriten („Ruf meine Tochter an“) mit eigenen
      Bezeichnungen, die auf einen Kontakt zeigen.
- [ ] Piep-Ton vor dem Zuhören (wie `dialos-start-ansage.py` bei DialOS –
      dort war ein fehlendes Startsignal ein echter Bug).

### Aus dem geschlossenen Test (ab 2026-09-05)

- [ ] **Widget auf einem Gerät prüfen** – gebaut, aber noch nie auf einem
      Startbildschirm gesehen. Nachzusehen: Geht es wirklich über die volle
      Breite (auch auf schmalen Geräten)? Schaltet die Farbe zuverlässig um?
      Startet ein Tippen den Dialog, oder öffnet es nur die App? Und liest
      TalkBack die Beschreibung vor, statt „Widget" zu sagen?
- [ ] **Unterbrechungserkennung am Gerät gegenprüfen.** Die Logik ist per
      Test abgesichert, der Weg dorthin nicht. Erzwingen lässt sich der Fall
      mit `adb shell am force-stop org.dialos.mobil` – danach muss beim
      nächsten Start die Ansage kommen und der Zähler in „Infos &
      Einstellungen“ um eins steigen. Achtung: `force-stop` ist nicht
      dasselbe wie ein Abschuss durch die Akku-Optimierung, deckt aber den
      Erkennungspfad ab.
- [x] ~~Klären, ob Michaelas Beobachtung der Fehlansage entspricht oder einem
      echten Abschuss.~~ **Geklärt am 09.09.2026: echter Abschuss.** Die App
      sagt nichts und wird einfach still, die Benachrichtigung verschwindet.
      Gerät ist ein **Xiaomi Redmi 13C**, die Akku-Optimierung war bereits
      ausgenommen – es ist also die MIUI-eigene Prozessverwaltung, nicht der
      Android-Standardmechanismus.
- [x] ~~Xiaomi/MIUI-Hinweis aufnehmen.~~ Steht seit 09.09.2026 in
      [docs/xiaomi-einstellungen.md](xiaomi-einstellungen.md), **abgelesen
      von einem echten Gerät**, nicht geraten. Die entscheidende Einstellung
      heißt „Hintergrund-Autostart“ und ist ab Werk für fast alle Apps aus.
- [ ] **Den Xiaomi-Hinweis in die App holen.** Die Datei hilft nur, wer sie
      liest – die Zielgruppe liest kein GitHub. Kurzfassung in „Infos &
      Einstellungen“, sinnvollerweise nur auf Xiaomi-Geräten eingeblendet
      (`Build.MANUFACTURER`), damit sie andere nicht verwirrt.
- [ ] **Prüfen, ob das Widget auf Xiaomi überhaupt platziert werden kann.**
      Auf dem geprüften Gerät steht „Startbildschirmverknüpfungen“ auf rot.
      Ob das auch Widgets betrifft, ist unklar – am Gerät nachsehen.
- [ ] Andere Hersteller ergänzen (Samsung, Huawei, Oppo, OnePlus), sobald
      jemand mit so einem Gerät seine Einstellungen zeigt. Nach demselben
      Verfahren: abgelesen, nicht geraten.
- [ ] Prüfen, ob die App die herstellereigene Einschränkung selbst erkennen
      kann, statt nur allgemein zu warnen. Wenn der Zähler in „Infos &
      Einstellungen“ hochläuft, obwohl die Akku-Optimierung ausgenommen ist,
      liegt genau dieser Fall vor – daraus ließe sich ein gezielter Hinweis
      ableiten.

- [ ] **Nachfragen, ob eine Nadine im Adressbuch steht.** Ein Tester meldete,
      „Nadine anrufen“ habe Martins vorgeschlagen. Der Code erklärt das
      nicht: Nadine bekommt 0,97, Martin 0,32, und die Klangcodes sind
      verschieden (626 gegen 6726). Vermutlich hat schon Vosk etwas anderes
      verstanden. Ohne Protokoll ist es Raten.
- [ ] **Erkennung von Vornamen mit Schweizer Akzent.** Derselbe Tester
      spricht Schweizerdeutsch und hatte auch auf Hochdeutsch
      Erkennungsprobleme bei Vornamen. Prüfen, ob das ein Akzent- oder ein
      Modellproblem ist.
- [ ] **Schweizerdeutsch klar in die Store-Beschreibung schreiben.** Wird
      nicht verstanden, das Vosk-Modell ist auf Hochdeutsch trainiert. Lieber
      vorher sagen als hinterher enttäuschen.
- [ ] Prüfen, ob die vier Tempostufen die richtigen sind – 1,6 könnte für
      geübte Sprachausgabe-Nutzer immer noch zu langsam sein.

### Technik

- [ ] `vosk-android` auf 0.3.75 und `jna` auf 5.19.1 heben – bewusst noch
      nicht gemacht, weil 0.3.47 mit dem verwendeten Modell erprobt ist und
      hier kein Gerät zum Gegentesten steht.
- [ ] Prüfen, ob ein grammatikbeschränkter Erkenner im Wartezustand Akku
      spart, ohne beim Umschalten Sprache zu verlieren (siehe Begründung
      im README).
- [ ] Release-Build signieren und den Signaturschlüssel sichern.
- [ ] Entscheiden, ob die App in den Play Store soll. Falls ja:
      `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` und `CALL_PHONE` brauchen eine
      Begründung, und der Play Store verlangt für `CALL_PHONE` eine
      Kernfunktions-Erklärung.
- [ ] Verhalten prüfen, wenn während des Dialogs ein Anruf hereinkommt.

### Zusammenspiel mit DialOS

- [ ] Klären, ob Handy und Laptop dieselben Kontakte teilen sollen und wie
      (Nextcloud-Kontakte über CardDAV?).
- [ ] Einheitliche Befehlssprache zwischen DialOS-Desktop und DialOS Mobil –
      der Desktop nutzt hassil, hier ist die Erkennung handgeschrieben.
      Perspektivisch sollten beide dieselben Sätze verstehen.
- [ ] Im DialOS-Repo (README + `docs/`) auf DialOS Mobil verweisen.

## ✅ Erledigt

- [x] **Rufnummern diktieren ist brauchbar geworden** (0.6.4) – 2026-09-06.
      Die App las nach jedem Ziffernblock die ganze bisherige Nummer vor und
      war dabei taub; wer weiterdiktierte, verlor genau diese Ziffern. Dazu:
      45 statt 15 Sekunden Wartezeit beim Diktieren, keine kommentarlos
      verworfene Nummer mehr bei Zeitablauf, eine Anleitung an der Stelle,
      wo sie gebraucht wird, und „letzte Ziffer löschen“ statt alles oder
      nichts. Einzelheiten im [Änderungsprotokoll](README.md#änderungsprotokoll).

- [x] **Fünf Befunde aus dem geschlossenen Test behoben** (0.6.3) –
      2026-09-05. „Ja bitte“ und „nein danke“ werden verstanden, gleich
      klingende Namen verdrängen den gemeinten nicht mehr, Nummerntypen
      („privat“, „mobil“, „Arbeit“) funktionieren im Befehl und als Antwort,
      die Sackgasse nach „Das habe ich nicht verstanden“ ist weg, und die
      Ansage nach der Wartezeit behauptet nicht mehr, die App höre auf.
      Einzelheiten im [Änderungsprotokoll](README.md#änderungsprotokoll).

- [x] Sprechgeschwindigkeit und Stimme der Sprachausgabe einstellbar –
      2026-09-05. Vier Tempostufen und die deutschen Stimmen des Geräts,
      über Knöpfe zum Weiterschalten statt Schieberegler, mit Hörprobe nach
      jedem Tippen.

- [x] Lizenz festgelegt: Apache 2.0, mit NOTICE-Datei für Vosk, das
      Sprachmodell, JNA und AndroidX – 2026-08-19

- [x] **Erster vollständiger Anruf per Sprache auf echter Hardware**
      (Motorola edge 50 neo, Android 16) – 2026-08-19. Aktivierungswort,
      Namenserkennung („carola stören" → Carola Stern), Bestätigung,
      Kartenwahl und Gesprächsaufbau. Zwei Fehler dafür behoben, siehe
      Änderungsprotokoll 0.6.1.
- [x] Kartenwahl per Sprache bei zwei SIM/eSIM, Version und
      GitHub-Link in den Einstellungen, größeres Logo – 2026-08-19
- [x] **Kurznachrichten per Sprache** (SMS) – 2026-08-19. WhatsApp bewusst
      verworfen: keine Sende-Schnittstelle, auf dem Gerät nachgeprüft.
- [x] Kontrast-Umschalter (schwarz/gelb) und Lautstärke-Umschalter
      (50 %/100 %) in der Kopfzeile, auf dem Gerät geprüft – 2026-08-19
- [x] Schaltfläche „Jetzt sprechen“ entfernt (redundant zu Hauptschalter
      und Kachel) – 2026-08-19
- [x] **Erster Lauf auf echter Hardware** (Motorola edge 50 neo, Android 16)
      – 2026-08-19. Stephan hat den Ablauf durchgesprochen, er hat
      funktioniert. Belegt im Log: Vosk-Modell entpackt (91 MB im externen
      App-Ordner), `Background started FGS: Allowed` für den
      Mikrofon-Dienst, acht Zustandswechsel des Dialogs, sauberes
      Ausschalten.
- [x] Material-You-Farben (`DynamicColors`) wieder entfernt – sie hatten
      das DialOS-Blau durch vom Hintergrundbild abgeleitete Olivtöne
      ersetzt und den Kontrast dem Zufall überlassen – 2026-08-19
- [x] Projekt aufgesetzt (Kotlin, Gradle 8.14.3, AGP 8.13, minSdk 26,
      targetSdk 36) – 2026-08-19
- [x] Vosk offline eingebunden, deutsches Modell wird beim Build geladen
      und liegt nicht im Repo – 2026-08-19
- [x] Aktivierungswort „Sprachsteuerung starten“ – 2026-08-19
- [x] Kontaktsuche mit Kölner Phonetik, Levenshtein und wortweisem
      Vergleich, inklusive Rückfrage bei mehreren Treffern – 2026-08-19
- [x] Deutsche Zahlwörter → Ziffern, Nummer diktieren – 2026-08-19
- [x] Bestätigung vor dem Wählen, abschaltbar – 2026-08-19
- [x] Wählen über `TelecomManager.placeCall` statt `ACTION_CALL` – 2026-08-19
- [x] Schnelleinstellungs-Kachel, Startsymbol-Kurzbefehl, Assistenten-Aufruf
      als zusätzliche Startwege – 2026-08-19
- [x] Barrierefreie Oberfläche (große Schaltflächen, Live-Region) – 2026-08-19
- [x] Logo und Farben aus DialOS übernommen – 2026-08-19
- [x] 22 Unit-Tests für Namensvergleich, Zahlwörter und Befehlserkennung,
      alle grün – 2026-08-19
- [x] Debug-APK gebaut (63 MB), Lint ohne Fehler – 2026-08-19
