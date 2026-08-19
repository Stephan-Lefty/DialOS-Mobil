[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – DialOS Mobil

## Offen

### Noch mit der Stimme zu prüfen

- [ ] **SMS wirklich verschicken** – der komplette Weg ist gebaut, aber nie
      durchgelaufen. Am besten eine Nachricht an sich selbst; im Protokoll
      steht dann, ob die Quittung des Netzes ankommt.
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
- [ ] Sprechgeschwindigkeit der Sprachausgabe einstellbar machen.

### Technik

- [ ] `vosk-android` auf 0.3.75 und `jna` auf 5.19.1 heben – bewusst noch
      nicht gemacht, weil 0.3.47 mit dem verwendeten Modell erprobt ist und
      hier kein Gerät zum Gegentesten steht.
- [ ] Prüfen, ob ein grammatikbeschränkter Erkenner im Wartezustand Akku
      spart, ohne beim Umschalten Sprache zu verlieren (siehe Begründung
      im README).
- [ ] Release-Build signieren und den Signaturschlüssel sichern.
- [ ] Lizenz festlegen (DialOS selbst hat noch keine) und `LICENSE` anlegen.
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
