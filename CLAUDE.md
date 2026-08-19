# Hinweise für Claude

Dieses Repository ist **DialOS Mobil** – der Handy-Ableger von
[DialOS](https://github.com/Stephan-Lefty/DialOS). Eine Android-App, mit der
blinde und motorisch stark eingeschränkte Menschen **allein durch Sprechen
telefonieren** können. Stephan arbeitet allein daran (kein Team) – bitte
durchgehend "du" statt "ihr/euch", und Stephan darf dich gerne "ClaudIA"
nennen.

**Lies zuerst [README.md](README.md)** – dort steht der vollständige
Funktionsumfang, der technische Aufbau, die Begründung für die beiden nicht
offensichtlichen Entwurfsentscheidungen und die bekannten Grenzen. Diese
Datei hier ist nur die Landkarte und der aktuelle Stand.

Konkrete offene Aufgaben stehen ausschließlich in [TODO.md](TODO.md)
(+ `TODO.en.md`) – nicht hier, damit der Stand an einer Stelle bleibt.

## Aktueller Stand (2026-08-19)

Version **0.6.0**. Build grün, Unit-Tests grün, Lint ohne Fehler, App-Bundle
für den Play Store gebaut (~56 MB). Das Repo ist seit 2026-08-19
**öffentlich** unter Apache 2.0.

**Auf Stephans Motorola edge 50 neo (Android 16) belegt:** Modell entpacken,
Mikrofon-Vordergrunddienst, Dialogstart per Knopf, Kartenerkennung bei zwei
SIM, Lautstärke- und Kontrastumschalter, Einstellungsseite mit
Selbstrückkehr.

**Noch nie mit einer Stimme geprüft** (steht in `TODO.md`): wie zuverlässig
das Aktivierungswort anspringt, und ob die gesprochene Kartenwahl greift.
Bei einem Test protokollierte Vosk "sprach steigt" für vermutlich
"Sprachsteuerung" - die Schwellwerte in `CommandParser.isWakePhrase` sind
geraten, nicht gemessen. Der Dialog protokolliert inzwischen jeden
erkannten Satz, damit sich das an echten Daten nachziehen lässt.

Nächster Schritt ist die Einreichung im Play Store, Anleitung in
[docs/veroeffentlichung.md](docs/veroeffentlichung.md).

## SMS: bewusst entfernt (0.6.0)

Kurznachrichten waren in 0.2.0 gebaut und funktionierten. Sie sind wieder
raus, weil `SEND_SMS` die Play-Store-Prüfung praktisch garantiert hätte
scheitern lassen - Google lässt SMS-Berechtigungen nur für eine
abschließende Liste zugelassener Anwendungsfälle zu, auf der ein
Sprachwähler nicht steht. Falls das Thema wiederkommt: der Code steht in
der Git-Historie (Commit "0.2.0: Kurznachrichten per Sprache"), und
WhatsApp/Signal sind keine Alternative (keine Sende-Schnittstelle, auf
Stephans Gerät nachgeprüft).

## Bauen

```bash
export JAVA_HOME=/home/stephan/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2
cd "/mnt/raid/eigene Daten/GitHub/Stephan-Lefty/DialOS-Mobil"
./gradlew assembleDebug     # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew test              # Unit-Tests, ohne Gerät lauffähig
./gradlew lintDebug
```

Auf Stephans Rechner ist als System-JDK nur Java 26 installiert – damit
läuft AGP nicht. Das oben gesetzte JDK 17 liegt im Gradle-Cache und ist der
Weg, der hier funktioniert. Das Android SDK liegt unter
`/home/stephan/Android/Sdk` (Plattform 36, Build-Tools 36), eingetragen in
`local.properties` (nicht im Repo).

Das deutsche Vosk-Modell (~46 MB) liegt bewusst **nicht** im Repo. Der
Gradle-Task `prepareVoskModel` lädt es beim ersten Build und entpackt es
nach `app/build/generated/assets/model-de/`, inklusive der `uuid`-Datei, die
Vosks `StorageService` zum Abgleich braucht. Die uuid wird deterministisch
aus dem Modellnamen abgeleitet – sonst würde die App das Modell bei jedem
Build neu ins Dateisystem entpacken.

## Landkarte des Codes

Alles unter `app/src/main/java/org/dialos/mobil/`:

- `VoiceService` – Vordergrunddienst, hält alles zusammen, wählt.
- `VoiceEngine` – Vosk-Anbindung.
- `DialogController` – der Gesprächsablauf als Zustandsautomat. **Hier
  ändern, wenn sich der Ablauf ändern soll**, nicht im Service.
- `CommandParser`, `NameMatcher`, `GermanNumbers` – reine Kotlin-Logik ohne
  Android-Abhängigkeit, deshalb unter `app/src/test/` getestet. Neue
  Befehle oder Erkennungsregeln immer mit Test.
- `ContactRepository` – Adressbuch.

**Falle beim Anpassen von `CommandParser`:** Der erkannte Text wird vor dem
Vergleich durch `NameMatcher.normalize` geschickt (klein, ohne Umlaute).
Alle Muster und Wortlisten müssen deshalb ebenfalls normalisiert vorliegen –
ein `"wähle"` im Regex trifft nie, es muss `"wahle"` heißen. Genau daran ist
beim ersten Testlauf ein Test gescheitert.

## Arbeitsweise mit Stephan

- Technisch versiert, aber kein Android-Experte – Erklärungen kompakt, aber
  das "warum" nicht weglassen.
- Bei Unklarheiten lieber kurz nachfragen; Stephan antwortet schnell.
- Commit-Nachrichten ausführlich mit Begründung ("warum"), nicht nur "was".
- Terminal-Befehle immer mit absoluten Pfaden.
- Konventionen über alle Repos hinweg: README in beiden Sprachen mit
  Änderungsprotokoll, oben verlinkt neben dem Sprachumschalter; `TODO.md`
  auf Deutsch mit den Abschnitten "Offen" und "✅ Erledigt".
