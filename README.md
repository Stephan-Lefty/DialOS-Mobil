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

## Bildschirmfotos

Zum Vergrößern anklicken.

<table>
<tr>
<td align="center" width="25%">
<a href="screenshots/01_startseite_aus.png"><img src="screenshots/01_startseite_aus.png" width="170" alt="Startbildschirm mit der Statusanzeige „Ausgeschaltet“ und einem großen blauen Knopf „Sprachsteuerung einschalten“"></a><br>
<sub>Ausgeschaltet</sub>
</td>
<td align="center" width="25%">
<a href="screenshots/02_startseite_hoert_zu.png"><img src="screenshots/02_startseite_hoert_zu.png" width="170" alt="Startbildschirm im Gespräch, die Statusanzeige lautet „Sprachsteuerung bereit. Wen möchten Sie anrufen?“"></a><br>
<sub>Im Gespräch</sub>
</td>
<td align="center" width="25%">
<a href="screenshots/03_infos_einstellungen.png"><img src="screenshots/03_infos_einstellungen.png" width="170" alt="Seite „Infos & Einstellungen“ mit Zurück-Knopf, Berechtigungen, Dauerbetrieb und drei Schaltern"></a><br>
<sub>Einstellungen</sub>
</td>
<td align="center" width="25%">
<a href="screenshots/04_kontrastansicht.png"><img src="screenshots/04_kontrastansicht.png" width="170" alt="Derselbe Startbildschirm in der kontraststarken Fassung: schwarzer Grund, gelbe Schaltflächen"></a><br>
<sub>Hoher Kontrast</sub>
</td>
</tr>
</table>

## So läuft ein Anruf ab

```
Nutzer:  „Sprachsteuerung starten“
App:     „Sprachsteuerung bereit. Wen möchten Sie anrufen?“
Nutzer:  „Max Mustermann anrufen“
App:     „Soll ich Max Mustermann auf Mobil anrufen?
          Sagen Sie Ja oder Nein.“
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
App:     „Soll ich die Nummer 0 1 7 9 … anrufen?
          Sagen Sie Ja oder Nein.“
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
und beobachtet den Anruf: kommt binnen zwölf Sekunden kein Gespräch
zustande, sagt sie das ausdrücklich, statt stumm zurückzufallen.

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

## Lizenz

[Apache-Lizenz 2.0](LICENSE) – Copyright 2026 Stephan Rösner.

Dieselbe Lizenz wie Vosk, das deutsche Sprachmodell und die verwendeten
Android-Bibliotheken; bewusst so gewählt, damit über den ganzen Aufbau
hinweg eine einzige Lizenz gilt und die enthaltene Patenterteilung greift.
Wer die App weitergibt, muss die [NOTICE](NOTICE)-Datei mitliefern – dort
stehen die Urheber der mitgelieferten Bestandteile.

## Änderungsprotokoll

### 0.6.2 (2026-08-20)

- **Telefonie ist jetzt Pflicht** (`uses-feature … required="true"`). Google
  Play bietet die App damit nur noch Geräten mit Telefonie an. Vorher hätte
  sie sich auf einem WLAN-Tablet installieren lassen, zugehört, den Namen
  erkannt – und beim Wählen stumm versagt. Genau das Fehlerbild, das für
  einen blinden Nutzer am schlimmsten ist. Nebeneffekt: Für den Store
  braucht es keine Tablet-Screenshots.


### 0.6.1 (2026-08-19)

**Der erste vollständige Anruf per Sprache hat funktioniert** – von
„Sprachsteuerung starten" bis zum aufgebauten Gespräch. Zwei Fehler
standen dem vorher im Weg:

- **Rufnummern werden vor dem Wählen aufbereitet.** Im Adressbuch stehen
  sie oft als „+49 176 1234-5678"; die Leerzeichen machen die
  `tel:`-Adresse ungültig, und der Telefonie-Dienst verwirft sie
  **stillschweigend**, ohne eine Ausnahme zu werfen. Genau deshalb war im
  Protokoll nichts zu sehen: kein Fehler, kein Anruf.
- **Der Anruf-Wächter räumte auf, bevor der Anruf entstand.** Er prüfte
  zwei Sekunden nach dem Wählen, ob der Audio-Modus normal ist, und
  schloss daraus „Gespräch beendet". Ein Anruf braucht aber mehrere
  Sekunden bis zum Klingeln. Jetzt gibt es 12 Sekunden Anlaufzeit – und
  kommt danach nichts zustande, **sagt die App das ausdrücklich**, statt
  stumm in den Wartezustand zurückzufallen.

**Die Rückfragen klingen jetzt wie Fragen.** Stephan wartete im Test
7 bzw. 10 Sekunden, weil „Carola Stern, Mobil, anrufen?" ihn nicht zu
einer Antwort veranlasste – beides steht so im Protokoll. Android-TTS
erzeugt keine verlässlich steigende Satzmelodie, deshalb steht die
Aufforderung jetzt im Satz: „Soll ich … anrufen? **Sagen Sie Ja oder
Nein.**" Die Kartenauswahl heißt „Für 1&1 sagen Sie 1" statt „1: 1&1" –
letzteres las die Stimme als Aufzählung vor, nicht als Angebot.

*Welche der beiden Korrekturen den Anruf tatsächlich freigemacht hat, ist
offen: Beide gingen zusammen ein, und der erfolgreiche Test lief ohne
angestecktes Kabel. Die Nummer ist der wahrscheinlichere Kandidat, weil
der Wächter den Anruf nicht verhindert, sondern nur den Dialog
zurückgesetzt hätte.*

- Nebenbei: Rufnummern erscheinen im Protokoll nur noch angedeutet
  (`017…78`), nicht mehr im Klartext.


### 0.6.0 (2026-08-19)

- **Kurznachrichten wieder entfernt.** Nicht aus technischen Gründen – der
  Weg funktionierte –, sondern für die Veröffentlichung: Google lässt die
  Berechtigung `SEND_SMS` nur für eine abschließende Liste zugelassener
  Anwendungsfälle zu, auf der ein Sprachwähler nicht steht. Die App hätte
  die Prüfung mit hoher Wahrscheinlichkeit nicht bestanden. Telefonieren
  bleibt die Kernfunktion, und dafür stehen die Chancen gut.
- Die App fordert damit **keine** SMS-Berechtigung mehr an.
- Vorbereitung für den Play Store: Release-Signierung, App-Bundle,
  Datenschutzerklärung, Store-Texte, Data-Safety-Ausfüllhilfe und eine
  [Schritt-für-Schritt-Anleitung](docs/veroeffentlichung.md).
- Nachgeprüft und im Paket belegt: Die App hat **keine
  Internetberechtigung** – sie kann technisch nichts versenden.


### 0.5.0 (2026-08-19)

- **Kartenwahl per Sprache bei zwei SIM/eSIM.** Nach der Bestätigung fragt
  die App „Über welche Karte? Eins: 1&1. Zwei: YELLLOW.“ – die Antwort geht
  per Zahl oder Anbietername. Angesagt wird der Name aus den
  Android-Einstellungen, nicht der Netzbetreiber – im Roaming hieße die
  Karte sonst „3 AT – 1&1“. Gilt für Anrufe wie für Nachrichten. Bei nur
  einer Karte wird nicht gefragt.
- Ohne diese Wahl nimmt Android stillschweigend die voreingestellte Karte –
  im Zweifel die falsche, im Ausland die teure.
- „Infos & Einstellungen“ zeigt jetzt die Version und einen Link zum
  Quelltext auf GitHub.
- Das Logo auf der Startseite ist doppelt so groß; der Startbildschirm
  lässt sich dafür scrollen, damit auf kleinen Geräten nichts abschneidet.


### 0.4.0 (2026-08-19)

- **Startbildschirm auf das Wesentliche reduziert:** Lautstärke, Kontrast,
  Zustand und ein einziger, doppelt so hoher Knopf. Alles zur Einrichtung
  liegt hinter „Infos & Einstellungen“ unten in der Mitte.
- **Der Knopf startet jetzt sofort das Gespräch.** Vorher musste man nach
  dem Einschalten noch „Sprachsteuerung starten“ sagen – doppelt, wenn man
  das Telefon ohnehin in der Hand hält. Das Aktivierungswort bleibt für
  später wichtig (Telefon in der Tasche, Bildschirm aus).
- „Infos & Einstellungen“ hat einen großen **Zurück-Knopf** oben und unten
  und springt nach 10 Sekunden ohne Berührung von selbst zur Startseite –
  wer versehentlich dort landet, findet sonst womöglich nicht zurück.
- Behoben: `fitsSystemWindows` überschreibt das Padding der View, auf der
  es steht – die Schaltflächen klebten dadurch am Bildschirmrand.

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
