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
- **Vier Wege, das Gespräch zu starten:** Aktivierungswort, große
  Schaltfläche in der App, Kachel in den Schnelleinstellungen, oder die
  Assistenten-Geste (die App lässt sich als Standard-Assistent setzen).
- **Autostart nach dem Neustart** des Telefons.
- **Barrierefreie Oberfläche:** sehr große Schaltflächen, hoher Kontrast,
  Statusanzeige als Live-Region – TalkBack liest jede Änderung mit vor.

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
- **Die App ersetzt keine Notruffunktion.** Ein Notruf sollte niemals von
  einer Spracherkennung abhängen.

## Danksagung

- [Vosk](https://alphacephei.com/vosk/) von Alpha Cephei – Offline-Erkennung
  und deutsches Sprachmodell (Apache-Lizenz 2.0).
- Logo und Farben aus dem [DialOS](https://github.com/Stephan-Lefty/DialOS)-Projekt.

## Änderungsprotokoll

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
