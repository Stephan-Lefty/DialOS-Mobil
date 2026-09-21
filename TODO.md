[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – DialOS Mobil

## Offen

### Dringend

- [x] ~~**Der ausgeschaltete Hotword-Schalter schweigt.**~~ **Gefunden und
      behoben am 21.09.2026 (0.6.14).** Die App erkannte „sprachsteuerung
      starten" zweimal einwandfrei – im Protokoll wörtlich nachzulesen – und
      tat nichts, weil in den Einstellungen „Auf ‚Sprachsteuerung starten'
      hören" ausgeschaltet war. Keine Ansage, kein Ton, keine Reaktion.
      Derselbe Fehlertyp wie 0.6.11, nur eine Ebene höher: Wer den Bildschirm
      nicht sehen kann, hat keine Möglichkeit, „hört mich nicht" von
      „ignoriert mich absichtlich" zu unterscheiden, und sucht den Fehler bei
      der eigenen Aussprache. Genau so ist die Meldung entstanden, das
      Aktivierungswort funktioniere nicht. Die Ansage beginnt deshalb mit
      „Ich habe Sie verstanden" und kommt einmal je Aus-Phase.

- [ ] **Anbieten, den Schalter per Sprache wieder einzuschalten.** Der
      Hinweis aus 0.6.14 nennt den Weg zum Schalter – aber das Navigieren
      dorthin ist genau das, was dieser Zielgruppe schwerfällt. Besser wäre:
      „Soll ich es wieder einschalten?" Das braucht einen neuen
      Dialogzustand und ist deshalb bewusst nicht mehr in 0.6.14 gegangen,
      das ohnehin 0.6.13 hinterherlief.

- [ ] **Überlegen, ob der Schalter so leicht erreichbar sein soll**, wenn
      sein Ausschalten die Kernfunktion der App stilllegt.

- [ ] **Lydia gezielt danach fragen**, ob bei ihr dieser Schalter aus ist.
      Ihre Meldung passt genau darauf, und es wäre die einfachste Erklärung.

### Zu prüfen

- [ ] **Woher kamen die vier Zählungen am 21.09.2026?** Der Zähler stieg im
      Lauf des Tages von 18 auf 23, bei drei `installDebug`-Läufen und drei
      `force-stop`.

      **Am selben Tag gemessen, und zwei Vermutungen sind damit vom Tisch:**

      - Ein `install -r` zählt **nicht** – Zähler blieb bei 23. Aber nicht,
        weil die Ausnahme aus 0.6.7 greift, sondern weil der Dienst vorher
        aussteigt: Nach dem Update startet er aus dem Hintergrund, bekommt
        kein Mikrofon und beendet sich, bevor `noteStartCause` erreicht ist
        (Protokoll: „Hintergrundstart erkannt, Mikrofon laut System
        erlaubt: false"). Die dokumentierte Begründung stimmt also nicht mit
        dem tatsächlichen Weg überein.
      - Ein `force-stop` zählt – belegt (22 → 23). Das ist richtig so.

      Offen bleibt die Rechnung: drei `force-stop` erklären drei Zählungen,
      gezählt wurden fünf. Zwei sind unerklärt. Verdacht bleibt der Pfad
      über `START_STICKY`, bei dem `intent == null` ist – dann kann
      `expected` gar nicht true werden
      (`intent?.getBooleanExtra(…) == true` ergibt bei null immer false),
      und ein erwarteter Neustart würde als Unterbrechung gelten. Ob dieser
      Pfad je erreicht wird, ist ungeprüft: Die Hintergrundstart-Sperre aus
      0.6.11 könnte ihn abfangen, bevor gezählt wird.

      Warum das zählt: Der Zähler ist die einzige Zahl, mit der sich belegen
      lässt, dass ein Hersteller die App abräumt (siehe Michaelas Xiaomi).
      Zählt er zu großzügig, hält man ein Android-Problem für dringender als
      es ist.

- [x] ~~Kommt nach einem App-Update wirklich eine hörbare Aufforderung?~~
      **Am 21.09.2026 belegt.** Nach `installDebug` stand die
      Benachrichtigung mit `channel=voice_control_boot`, **`importance=4`**
      (also mit Ton und Einblendung) und `category=reminder` im System. Die
      Kernkorrektur aus 0.6.11 greift damit nachweislich – bisher war nur
      der Kanal belegt, nicht die Zustellung im Update-Fall.

      Dabei bestätigt, was für die Testpersonen praktisch wichtig ist: **Nach
      jedem Update ist die Sprachsteuerung aus** und muss über die
      Benachrichtigung neu eingeschaltet werden. Das ist kein Fehler,
      sondern Androids Hintergrundstart-Sperre – aber es gehört in die
      Ankündigung künftiger Updates.

- [ ] **Der Stop-Intent von außen greift nicht.** `am start-foreground-service
      -a org.dialos.mobil.action.STOP` beendete den Dienst am 21.09.2026
      nicht; es blieb nur `force-stop`. Für den Alltag belanglos (der Knopf
      in der App und die Benachrichtigung funktionieren), beim Testen am
      Gerät aber lästig, weil `force-stop` den Unterbrechungszähler
      verfälscht. Nachsehen, ob `onStartCommand` die Aktion beim Start aus
      dem Hintergrund überhaupt erreicht.

### Noch mit der Stimme zu prüfen

- [ ] **Kartenwahl im Gespräch** – die Erkennung der Karten ist auf dem
      Gerät bestätigt („1&1“ und „YELLLOW“), die gesprochene Abfrage selbst
      noch nicht. Prüfen, ob „Eins“ und der Anbietername beide greifen und
      der Anruf über die richtige Karte geht.
- [x] ~~**Aktivierungswort messen.**~~ **Erledigt am 21.09.2026** (Motorola
      edge 50 neo): fünf von sechs Rufen erkannt, der verpasste kam als
      „sprachstörungen starten“ mit 0,78 durch – knapp unter der geratenen
      Schwelle 0,82. Aus einer Viertelstunde Raumgeräusch kein einziger
      Fehlalarm, höchster Wert 0,26. Schwelle deshalb auf 0,70 gesenkt
      (`CommandParser.WAKE_MIN_RATIO`), die echten Sätze liegen als
      Regressionsfälle in `CommandParserTest`. Erschien in 0.6.13.

### Auf echter Hardware prüfen

- [x] ~~Erkennungsrate des Aktivierungsworts messen.~~ **Erledigt am
      21.09.2026**, siehe oben. Offen bleibt die Gegenprobe über einen
      längeren Zeitraum: Eine Viertelstunde Raumgeräusch ist eine Stichprobe,
      kein Alltag. Wenn aus dem Test Fehlalarme gemeldet werden, gehört die
      Schwelle noch einmal auf den Prüfstand.
- [ ] Prüfen, ob die Erkennung während der eigenen Sprachausgabe wirklich
      still ist (Echo-Problem) – besonders über Lautsprecher.
- [ ] Akkuverbrauch über einen ganzen Tag messen.
- [ ] Verhalten mit Bluetooth-Headset testen (dasselbe AIRHUG 01 wie bei
      DialOS?) – nimmt Vosk dann das Headset-Mikrofon?
- [x] ~~Autostart nach Neustart auf Android 14/15 prüfen.~~ **Geklärt am
      20.09.2026, und es war schlimmer als erwartet:** Der Dienst startet
      gar nicht (`ForegroundServiceStartNotAllowedException`) oder startet
      ohne Mikrofonzugriff. Die Ausweich-Benachrichtigung lief über den
      leisen Kanal und war damit unauffindbar. Behoben in 0.6.11.
- [ ] **Den Flugmodus-Weg am Gerät durchspielen** (0.6.12, ungetestet):
      Flugmodus an, Anruf per Sprache versuchen. Kommt die Ansage sofort?
      Erscheint die Benachrichtigung? Führt ein Tippen wirklich in die
      Flugmodus-Einstellungen? Braucht eine Stimme, ging beim Bauen nicht.
- [ ] **Die Lautstärke-Anhebung im Dienst isoliert prüfen.** Belegt ist nur
      der gemeinsame Weg (Startseite + Dienst). Ob der Dienst allein sie
      anhebt – also beim Aktivierungswort ohne Bildschirm –, ist ungeprüft.
- [ ] Überlegen, was bei aktivem „Bitte nicht stören" geschehen soll. Dann
      verweigert Android die Lautstärkeänderung, und ein Hinweis darüber
      wäre genauso unhörbar wie die Ansage, um die es geht. Vibration?
- [ ] **Am Gerät hören, ob die neue Benachrichtigung wirklich auffällt.**
      Kanal und Zustellung sind belegt, der Ton noch nicht – beim Test lief
      ein Videocall, deshalb ohne Audio geprüft.
- [ ] **Erkennen, wenn die App im laufenden Betrieb das Mikrofon verliert.**
      Der jetzige Schutz greift beim Start. Belegt ist aber auch der Fall,
      dass ein Dienst läuft und taub ist (Warnung „started from background
      can not have microphone access"). Denkbar: Wenn über längere Zeit kein
      einziges Erkennungsergebnis eintrifft, nachfragen statt schweigen.

### Funktionen

- [ ] Anruf annehmen per Sprache („Abheben“ / „Annehmen“) – aktuell kann
      die App nur anrufen, nicht abnehmen. Braucht `ANSWER_PHONE_CALLS`.
- [ ] Auflegen per Sprache.
- [ ] Lautsprecher automatisch einschalten (`EXTRA_START_CALL_WITH_SPEAKERPHONE`),
      damit ein blinder Nutzer das Telefon nicht ans Ohr halten muss –
      als Einstellung.
- [ ] Kurzwahl / Favoriten („Ruf meine Tochter an“) mit eigenen
      Bezeichnungen, die auf einen Kontakt zeigen.
- [ ] **Anrufweg wählbar machen: WhatsApp, Signal, Telegram statt Mobilfunk.**
      Gewünscht aus dem geschlossenen Test (10.09.2026): Im Büro schlechter
      Mobilfunkempfang, gutes WLAN – dort wird ohnehin über WhatsApp
      telefoniert. Denkbar als Voreinstellung oder als Rückfrage vor jedem
      Anruf.
      **Was dafür zu klären ist:** Diese Apps bieten Anrufe über einen
      Kontakt-Eintrag an (eigener MIME-Typ in `ContactsContract.Data`), nicht
      über eine offene Schnittstelle. Es funktioniert also nur für Kontakte,
      die dort auch verknüpft sind – und erfordert eine `<queries>`-Angabe im
      Manifest, um die Apps überhaupt zu sehen. **Nicht verwechseln mit dem
      SMS-Befund von 0.6.0:** Dort ging es ums Nachrichtenversenden, das
      tatsächlich keine Schnittstelle hat. Anrufe sind eine andere Frage und
      ungeprüft.
      Das Versprechen "keine Internetberechtigung" bleibt unberührt – den
      Anruf führt die andere App aus, nicht DialOS Mobil.
      Samuel selbst sieht es als Wunsch für eine spätere Version.
- [ ] Piep-Ton vor dem Zuhören (wie `dialos-start-ansage.py` bei DialOS –
      dort war ein fehlendes Startsignal ein echter Bug).

### Aus dem geschlossenen Test (ab 2026-09-05)

- [x] ~~Widget auf einem Gerät prüfen.~~ **Erledigt am 09.09.2026** auf dem
      Motorola edge 50 neo: volle Breite, Farbe und Text wechseln
      zuverlässig, ein Tippen startet den Dialog (nicht nur die App),
      TalkBack liest die richtige Beschreibung. Dabei fiel auf, dass der
      Text „Jetzt sprechen" sachlich falsch war – behoben in 0.6.7.
- [ ] Widget auf einem **schmalen** Gerät prüfen. Getestet ist bisher nur
      ein 1200 px breiter Bildschirm.
- [x] ~~Das Anbieten des Widgets am Gerät prüfen.~~ **Erledigt am
      09.09.2026** auf dem Motorola: Knopf erscheint nur ohne vorhandenes
      Widget, der Launcher zeigt den Bestätigungsdialog mit Vorschau,
      „Hinzufügen“ legt den Balken hin, danach ist der Knopf weg.
- [ ] Dasselbe **auf einem Xiaomi** prüfen. Dort stand
      „Startbildschirmverknüpfungen“ auf rot – möglicherweise scheitert die
      Anfrage genau daran, und dann greift der Rückfallhinweis.
- [x] ~~Unterbrechungserkennung am Gerät gegenprüfen.~~ **Erledigt am
      09.09.2026:** Mit `adb shell am force-stop` ausgelöst, der Startgrund
      wurde korrekt als `AFTER_INTERRUPTION` erkannt und gezählt. Dabei fiel
      auf, dass auch App-Updates mitgezählt wurden – behoben in 0.6.7, weil
      der Zähler sonst als Diagnosewerkzeug wertlos wäre.
- [ ] Die **Ansage** nach einer Unterbrechung noch hören. Bisher ist nur
      belegt, dass der Fall erkannt und gezählt wird; dass die App es auch
      ausspricht, ist ungeprüft (beim Test lief die Erkennung noch nicht,
      als der Dienst neu startete).
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

- [ ] **Native Bibliotheken auf 16-KB-Speicherseiten ausrichten.** Die Play
      Console meldet das unter „Für deinen nächsten Release" als *Erfordert
      Aktion*: Auf Geräten mit 16-KB-Arbeitsspeicher-Seitengröße kann die App
      abstürzen oder sich gar nicht erst installieren. Für die Zielgruppe wäre
      ein stiller Absturz beim Start das schlimmste Fehlerbild.

      **Am 21.09.2026 gemessen statt geraten** (`readelf -lW` auf die
      `.so`-Dateien im Bundle, Architektur arm64-v8a):

      - `libjnidispatch.so` aus jna 5.13.0: `LOAD align 0x10000` = 64 KB.
        **Schon in Ordnung.** Das bisher hier vermutete JNA-Problem gibt es
        nicht, ein jna-Upgrade ist dafür nicht nötig.
      - `libvosk.so` aus vosk-android 0.3.47: `LOAD align 0x1000` = 4 KB.
        **Der alleinige Übeltäter.**
      - Zum Vergleich heruntergeladen und nachgemessen: vosk-android 0.3.75
        liefert `0x4000` = 16 KB. **Der Fix ist eine Zeile in
        `app/build.gradle.kts`.**

      Ebenfalls am 21.09.2026 richtiggestellt: Hier stand, das müsse „bewusst
      erst nach den 14 Tagen" geschehen, weil ein Release mitten im Test die
      Zählung störe. Das stimmt nicht. Google zählt, wie lange genug
      angemeldete Tester die App installiert haben, nicht wie lange eine
      bestimmte Version liegt. Der wirkliche Grund zu warten ist ein anderer,
      siehe nächster Punkt.
- [ ] `vosk-android` von 0.3.47 auf 0.3.75 heben. Behebt das 16-KB-Thema
      (siehe oben), ist aber **kein Einzeiler zum Nebenbei-Mitnehmen**: Es
      sind 28 Versionen der Spracherkennung selbst. Zwei Dinge gehören
      danach am Gerät geprüft, nicht angenommen:

      1. Ob das mitgelieferte deutsche Modell unverändert passt.
      2. Ob sich die Erkennungsqualität verschiebt. Die am 21.09.2026
         gemessenen Schwellwerte des Aktivierungsworts
         (`CommandParser.WAKE_MIN_RATIO`, Regressionsfälle in
         `CommandParserTest`) stammen aus Ausgaben von **0.3.47**. Mit einer
         neuen Erkennungs-Bibliothek ist diese Messung hinfällig und muss
         wiederholt werden.

      Dringend wird es, wenn Produktionszugriff beantragt wird; für den
      geschlossenen Test bleibt es eine Warnung.
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
