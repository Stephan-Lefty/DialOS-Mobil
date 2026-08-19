[Deutsch](README.md) | [English](README.en.md) | [Änderungsprotokoll](#änderungsprotokoll) | [TODO](TODO.md)

<img src="assets/logo.png" alt="DialOS Logo" width="200">

# DialOS Mobil

Der Handy-Ableger von [DialOS](https://github.com/Stephan-Lefty/DialOS):
eine Android-App, mit der man **allein durch Sprechen telefonieren** kann.
Gedacht für blinde und motorisch stark eingeschränkte Menschen, die ein
Telefon nicht bedienen können – aber angerufen werden wollen und selbst
anrufen möchten.

Die Erkennung läuft **vollständig offline** auf dem Gerät, mit derselben
Engine wie der DialOS-Desktop (Vosk). Es verlässt kein Ton das Telefon,
und die App funktioniert ohne Internet und ohne Google-Dienste.

Dieses Projekt ist in Zusammenarbeit mit [Claude](https://claude.com) entstanden.

## So läuft ein Anruf ab

```
Nutzer:  „Sprachsteuerung starten“
App:     „Sprachsteuerung bereit. Wen möchten Sie anrufen?“
Nutzer:  „Max Mustermann anrufen“
App:     „Max Mustermann, Mobil, anrufen?“
Nutzer:  „Ja“
App:     „Ich rufe Max Mustermann an.“   → der Anruf startet
```

Eine Nummer diktieren statt einen Kontakt zu nennen:

```
Nutzer:  „Nummer wählen“
App:     „Bitte sprechen Sie die Nummer, Ziffer für Ziffer.
          Sagen Sie fertig, wenn Sie durch sind.“
Nutzer:  „null eins sieben neun …“
App:     „0 1 7 9 …“                     (liest nach jeder Gruppe zurück)
Nutzer:  „fertig“
App:     „Nummer 0 1 7 9 … anrufen?“
Nutzer:  „Ja“
```

Eine Kurznachricht diktieren:

```
Nutzer:  „Schreibe Max Mustermann“
App:     „Nachricht an Max Mustermann. Was soll ich schreiben?
          Sagen Sie fertig, wenn Sie durch sind.“
Nutzer:  „Ich komme heute eine Stunde später“
App:     „Ich komme heute eine Stunde später“   (liest nach jedem Stück zurück)
Nutzer:  „fertig“
App:     „Ich schreibe an Max Mustermann: Ich komme heute eine Stunde
          später. Absenden?“
Nutzer:  „Ja“
App:     „Nachricht gesendet.“
```

Jederzeit möglich: **„Abbrechen“**, **„Hilfe“**, **„Wiederholen“**,
**„Sprachsteuerung beenden“**. Bleibt es 15 Sekunden still, beendet die
App den Dialog von selbst und wartet wieder auf das Aktivierungswort.

## Funktionen

- **Aktivierungswort „Sprachsteuerung starten“** – die App hört dauerhaft
  im Hintergrund mit, ohne dass der Bildschirm an sein muss.
- **Kontaktsuche, die Verhörer verzeiht.** Drei Verfahren greifen
  ineinander: wortweiser Vergleich (Nachname allein genügt),
  Levenshtein-Ähnlichkeit (`Musterman` → `Mustermann`) und
  **Kölner Phonetik** – damit finden `Meier`, `Maier`, `Mayer` und `Meyer`
  denselben Kontakt.
- **Rückfrage bei mehreren Treffern:** Die App liest die Vorschläge
  nummeriert vor, der Nutzer sagt „eins“, „zwei“ oder den Namen noch einmal.
- **Mehrere Nummern pro Kontakt:** Mobil zuerst, dann Privat, dann Arbeit.
  Ein „Nein“ springt zum nächsten Vorschlag statt abzubrechen.
- **Ziffern diktieren** in deutscher Sprache, auch zusammengesetzt
  („einundzwanzig“ → `21`), mit `plus` für die Ländervorwahl und
  „doppel sieben“ für `77`.
- **Bestätigung vor dem Wählen** (abschaltbar) – eine Fehlerkennung soll
  keinen Fehlanruf auslösen.
- **Kurznachrichten diktieren und absenden**, ohne den Bildschirm zu
  berühren. Der Text wird vor dem Absenden vollständig vorgelesen, und
  diese Bestätigung ist **nicht** abschaltbar: eine SMS ist unwiderruflich
  und kostet Geld.
- **Vier Wege, das Gespräch zu starten:** Aktivierungswort, große
  Schaltfläche in der App, Kachel in den Schnelleinstellungen, oder die
  Assistenten-Geste (die App lässt sich als Standard-Assistent setzen).
- **Autostart nach dem Neustart** des Telefons.
- **Barrierefreie Oberfläche:** sehr große Schaltflächen, hoher Kontrast,
  Statusanzeige als Live-Region – TalkBack liest jede Änderung mit vor.
- **Kontrast umschaltbar** (Knopf oben rechts): schwarzer Grund mit gelben
  Schaltflächen für Menschen mit starker Sehbehinderung.
- **Lautstärke umschaltbar** (Knopf oben links): 50 % oder volle Lautstärke.
  Beim Start der App werden immer 50 % gesetzt – ein zu leise gedrehtes
  Telefon fällt sonst erst auf, wenn die App scheinbar schweigt.

## Technischer Aufbau

| Baustein | Aufgabe |
|---|---|
| [`VoiceService`](app/src/main/java/org/dialos/mobil/VoiceService.kt) | Vordergrunddienst, hält Mikrofon und Dialog am Leben, wählt über `TelecomManager` |
| [`VoiceEngine`](app/src/main/java/org/dialos/mobil/VoiceEngine.kt) | Vosk-Anbindung: Modell entpacken, zuhören, pausieren |
| [`DialogController`](app/src/main/java/org/dialos/mobil/DialogController.kt) | Der Gesprächsablauf als Zustandsautomat – ohne Android-Abhängigkeiten im Kern |
| [`CommandParser`](app/src/main/java/org/dialos/mobil/CommandParser.kt) | Erkennt Aktivierungswort und Befehle im erkannten Text |
| [`NameMatcher`](app/src/main/java/org/dialos/mobil/NameMatcher.kt) | Namensvergleich inkl. Kölner Phonetik |
| [`GermanNumbers`](app/src/main/java/org/dialos/mobil/GermanNumbers.kt) | Deutsche Zahlwörter → Ziffern |
| [`ContactRepository`](app/src/main/java/org/dialos/mobil/ContactRepository.kt) | Adressbuch lesen und durchsuchen |

Zwei Entwurfsentscheidungen, die nicht offensichtlich sind:

- **Freier Wortschatz statt Grammatik für das Aktivierungswort.** Eine auf
  den Wake-Satz beschränkte Grammatik wäre sparsamer, verlangt beim
  Umschalten in den Befehlsmodus aber, das Mikrofon kurz freizugeben –
  genau in dem Moment, in dem der Nutzer weiterspricht.
- **Wählen über `TelecomManager.placeCall` statt `ACTION_CALL`.** Seit
  Android 10 darf ein Hintergrunddienst keine Activity mehr starten; der
  Telecom-Dienst nimmt den Anruf dagegen zuverlässig entgegen.

Während die App selbst spricht, wird die Erkennung angehalten – sonst
hört sie ihre eigene Stimme. Läuft ein Gespräch, pausiert sie ebenfalls
und prüft alle zwei Sekunden, ob aufgelegt wurde.

## Bauen

Voraussetzungen: Android SDK (Plattform 36, Build-Tools 36) und ein JDK 17.

```bash
./gradlew assembleDebug
```

Das deutsche Vosk-Modell (~46 MB) liegt bewusst **nicht** im Repo –
Gradle lädt es beim ersten Build automatisch von
[alphacephei.com](https://alphacephei.com/vosk/models) herunter und
entpackt es in die Assets. Ein eigenes Modell lässt sich mitgeben:

```bash
./gradlew assembleDebug -PvoskModelZip=/pfad/zum/modell.zip
```

Tests (Namensvergleich, Zahlwörter, Befehlserkennung – ohne Gerät lauffähig):

```bash
./gradlew test
```

Das fertige APK liegt unter `app/build/outputs/apk/debug/app-debug.apk`
und ist rund 62 MB groß – das Sprachmodell macht den Großteil aus.

## Einrichtung auf dem Telefon

1. APK installieren (Installation aus unbekannten Quellen erlauben).
2. App öffnen, **„Berechtigungen erteilen“** antippen: Mikrofon, Kontakte,
   Telefonieren und Benachrichtigungen.
3. **„Akku-Optimierung ausnehmen“** antippen – sonst schläft der Dienst
   nach einiger Zeit ein und hört das Aktivierungswort nicht mehr.
4. **„Sprachsteuerung einschalten“**. Die App bestätigt gesprochen.

Optional, aber für die Zielgruppe sinnvoll:

- In den Android-Einstellungen unter *Apps → Standard-Apps → Digitaler
  Assistent* **DialOS Mobil** auswählen. Dann startet die Assistenten-Geste
  direkt das Gespräch.
- Die Kachel **„Sprachsteuerung“** in die Schnelleinstellungen ziehen.

## Bekannte Grenzen

- **Autostart ab Android 14:** Ein Dienst mit Mikrofon-Zugriff darf nicht
  aus dem Hintergrund gestartet werden. Nach einem Neustart legt die App
  deshalb eine antippbare Benachrichtigung an, statt sofort loszuhören.
- **Dauerhaftes Zuhören kostet Akku.** Auf einem Testgerät mit
  ausgenommener Akku-Optimierung ist mit spürbarem Mehrverbrauch zu
  rechnen; das kleine Vosk-Modell ist bewusst gewählt, um das zu begrenzen.
- **Das kleine deutsche Modell** (`vosk-model-small-de-0.15`) ist auf
  Kommandos ausgelegt, nicht auf Diktat. Ungewöhnliche Eigennamen erkennt
  es entsprechend schlechter – dafür gleicht der Namensvergleich einiges aus.
- **Freier Text wird schlechter erkannt als Befehle.** Das kleine Modell
  ist auf Kommandos ausgelegt; beim Diktieren einer Nachricht ist mit
  Fehlern zu rechnen. Deshalb wird vor dem Absenden immer vorgelesen.
- **Nur SMS, kein WhatsApp oder Signal.** Diese Dienste bieten keine
  Möglichkeit, eine Nachricht abzusenden – ihr Intent öffnet nur den Chat
  mit vorbereitetem Text, auf „Senden“ müsste getippt werden. Das ließe
  sich nur über einen Bedienungshilfe-Dienst umgehen, der WhatsApps
  Oberfläche fernsteuert; der bricht bei jedem Update und versagt dann
  still. Für eine App, auf die sich ein blinder Mensch verlässt, ist das
  das schlechteste Fehlverhalten.
- **Die App ersetzt keine Notruffunktion.** Ein Notruf sollte niemals von
  einer Spracherkennung abhängen.

## Danksagung

- [Vosk](https://alphacephei.com/vosk/) von Alpha Cephei – Offline-Erkennung
  und deutsches Sprachmodell (Apache-Lizenz 2.0).
- Logo und Farben aus dem [DialOS](https://github.com/Stephan-Lefty/DialOS)-Projekt.

## Änderungsprotokoll

### 0.3.0 (2026-08-19)

- **Kontrast-Umschalter** oben rechts: schwarzer Grund, gelbe
  Schaltflächen. Gelb auf Schwarz bleibt bei Blendung und Kontrastverlust
  lesbar, wo Blau auf Weiß verschwimmt.
- **Lautstärke-Umschalter** oben links, 50 % oder 100 %. Die App setzt bei
  jedem Start 50 % – das Testgerät stand auf 27 %, und wer nichts sieht,
  bemerkt eine zu leise Ansage erst, wenn die App scheinbar schweigt.
  Die Sprachausgabe liegt dafür jetzt auf dem Medien-Kanal, den auch die
  Lautstärketasten des Telefons regeln.
- Die Schaltfläche **„Jetzt sprechen“ ist entfallen**: sie war redundant.
  Ist die Sprachsteuerung aus, hilft sie nicht; ist sie an, genügt das
  Aktivierungswort. Für den manuellen Start gibt es weiterhin die
  Schnelleinstellungs-Kachel, den Startsymbol-Kurzbefehl und den
  Assistenten-Aufruf.
- Die Abschnitte „Berechtigungen“ und „Dauerbetrieb“ sind zurückhaltender
  gestaltet – sie werden einmal gebraucht, nicht täglich.

### 0.2.0 (2026-08-19)

- **Kurznachrichten per Sprache**: „Schreibe Max Mustermann“, Text
  diktieren, mit „fertig“ abschließen. Die App liest Empfänger und Text
  noch einmal vor und sendet erst nach „Ja“.
- Bestätigung vor dem Absenden ist bewusst nicht abschaltbar – anders als
  beim Anruf ist eine SMS unwiderruflich und kostenpflichtig.
- Für Nachrichten werden Mobilnummern bevorzugt; bleibt nur eine
  Festnetznummer übrig, weist die App gesprochen darauf hin.
- Neue Sprachbefehle: „Löschen“ / „noch mal von vorn“ verwirft den
  diktierten Text. Beim Diktieren gilt eine längere Denkpause (30 statt
  15 Sekunden).
- Die SIM-Karte kommt aus der Android-Standardeinstellung für SMS – auf
  Geräten mit zwei Karten wird also die genommen, die dort ohnehin
  eingestellt ist.
- Erster Lauf auf echter Hardware bestanden (Motorola edge 50 neo,
  Android 16).
- Material-You-Farben entfernt und alle Farbrollen fest gesetzt: das
  DialOS-Blau war durch vom Hintergrundbild abgeleitete Töne ersetzt
  worden, der Kontrast hing damit vom Zufall ab.

### 0.1.0 (2026-08-19)

- Erste Fassung.
- Aktivierungswort „Sprachsteuerung starten“ über dauerhaft laufende
  Offline-Erkennung (Vosk, deutsches Modell).
- Anrufen per Kontaktname mit Kölner Phonetik, Levenshtein-Ähnlichkeit und
  wortweisem Vergleich; Rückfrage bei mehrdeutigen Treffern.
- Rufnummern per Sprache diktieren, inklusive deutscher Zahlwörter,
  Ländervorwahl mit „plus“ und „doppel“ für Wiederholungen.
- Bestätigung vor dem Wählen, abschaltbar.
- Start zusätzlich über Schaltfläche, Schnelleinstellungs-Kachel,
  Startsymbol-Kurzbefehl und Assistenten-Aufruf.
- Autostart nach Neustart mit Ausweich-Benachrichtigung ab Android 14.
- Barrierefreie Oberfläche mit sehr großen Schaltflächen und Live-Region
  für TalkBack.
- Logo und Farbschema aus DialOS übernommen.
- Modell wird beim Build automatisch geladen und liegt nicht im Repo.
