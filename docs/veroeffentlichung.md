# Veröffentlichung im Google Play Store

Schritt für Schritt. Was erledigt ist, ist abgehakt; der Rest braucht
Stephans Konto, seine Entscheidung oder ein Gerät.

## 1. Der Signaturschlüssel

Ohne ihn geht nichts, und er lässt sich **nicht ersetzen**: Geht er
verloren, kann die App nie wieder aktualisiert werden – Google akzeptiert
nur Updates mit derselben Signatur.

```bash
cd "/mnt/raid/eigene Daten/GitHub/Stephan-Lefty/DialOS-Mobil"
keytool -genkeypair -v \
  -keystore dialos-mobil-release.jks \
  -alias dialos -keyalg RSA -keysize 4096 -validity 10000
```

Danach `keystore.properties` im Projektwurzelverzeichnis anlegen:

```properties
storeFile=/absoluter/pfad/dialos-mobil-release.jks
storePassword=DEIN_PASSWORT
keyAlias=dialos
keyPassword=DEIN_PASSWORT
```

Beides steht in `.gitignore` und darf **niemals** ins Repo. Den Schlüssel
zusätzlich außerhalb dieses Rechners sichern – dieselbe Sorgfalt wie beim
Sicherheits-Stick von DialOS.

- [x] Schlüssel erzeugt (2026-08-20)
- [x] `keystore.properties` angelegt, Rechte 600
- [ ] **Schlüssel an einem zweiten Ort gesichert** – noch offen, bitte nachholen

## 2. Das Paket bauen

```bash
export JAVA_HOME=/home/stephan/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2
./gradlew bundleRelease
```

Ergebnis: `app/build/outputs/bundle/release/app-release.aab` (~56 MB).

- [x] Baut durch, auch mit Verschleierung (ProGuard); Vosk überlebt sie
- [x] Mit Schlüssel signiert und geprüft (`jarsigner -verify` → `jar verified`,
      CN=Stephan Rösner, SHA384withRSA, 4096 Bit, gültig bis 2054)

## 3. Grafiken

| Was | Größe | Datei | Status |
|---|---|---|---|
| App-Symbol | 512×512 | `screenshots/playstore/play-store-icon-512.png` | ✅ |
| Funktionsgrafik | 1024×500 | `screenshots/playstore/play-store-feature-graphic-1024x500.png` | ✅ |
| Screenshots Telefon | mind. 2, empfohlen 4–8 | `screenshots/0*.png` | ✅ 4 Stück, 1200×2670 |

Die vier Screenshots zeigen: Startseite ausgeschaltet, Startseite im
Gespräch („Wen möchten Sie anrufen?"), Infos & Einstellungen, und die
kontraststarke Ansicht. Neu erzeugen lassen sie sich jederzeit mit

```bash
python3 docs/screenshots-aufnehmen.py
```

**Falle dabei:** Ist die Benachrichtigungsleiste heruntergezogen, liegt sie
über der App und das Skript findet keine Knöpfe. Vorher schließen mit
`adb shell cmd statusbar collapse`.

## 4. Texte

- [x] [Store-Beschreibung](play-store-listing.md) – Kurz- und Langtext
- [x] [Data-Safety-Formular](play-store-data-safety.md) – Ausfüllhilfe
- [x] [Datenschutzerklärung](../PRIVACY.md) (DE) und [englisch](../PRIVACY.en.md)
- [x] Datenschutzerklärung öffentlich erreichbar:
      **https://dialos.org/dialos-mobil-datenschutz/**
      (WordPress-Seite ID 196, angelegt mit
      `DialOS/Wordpressinstallation/dialos-mobil-datenschutz.py`. Das Skript
      aktualisiert die Seite bei erneutem Lauf, statt eine zweite anzulegen –
      die allgemeine Erklärung `/datenschutzerklaerung/` bleibt unberührt.)

      **Diese URL gehört in der Play Console in das Feld
      „Datenschutzerklärung".**

## 5. Video für die Berechtigungserklärung

Google verlangt für den Mikrofon-Vordergrunddienst eine Bildschirmaufnahme,
die den Ablauf zeigt. Inhalt:

1. App öffnen, „Sprachsteuerung einschalten" antippen
2. Die Ansage abwarten
3. „Sprachsteuerung starten" sagen
4. Einen Namen sagen, mit „Ja" bestätigen
5. Der Anruf beginnt

- [x] Video aufgenommen und veröffentlicht (56 s, mit Ton):
      **https://dialos.org/wp-content/uploads/2026/08/DialOS-Mobil-Demo.mp4**

      Liegt in der Mediathek von dialos.org (Medien-ID 199), nicht bei
      YouTube – eigene Infrastruktur, kein Google-Konto nötig. Die Datei
      muss erreichbar bleiben, solange die App im Store ist; Google prüft
      bei Rückfragen unter Umständen Wochen später erneut.

      Bearbeitet mit `ffmpeg`: Vorspann gekürzt, die Namensliste
      unscharf **und** in dieser Spanne stummgeschaltet (die App spricht
      die Namen aus – Schwärzen allein hätte nichts genützt), auf dem
      Anrufbildschirm ein deckender Balken über Name, Nummer und Foto.
      Deckend statt unscharf, weil sich verwaschene Ziffern
      rekonstruieren lassen. Das Original liegt außerhalb des Repos unter
      `../DialOS-Mobil-Demo.mp4` – es enthält Stephans Stimme und gehört
      nicht ins öffentliche GitHub.

## 6. In der Play Console

- [x] App anlegen, Standardsprache **Deutsch (Deutschland)**
- [x] Kategorie **Kommunikation** – eine Kategorie „Barrierefreiheit" gibt
      es bei Google Play nicht, siehe die Korrektur in
      [play-store-listing.md](play-store-listing.md)
- [x] Store-Eintrag aus [play-store-listing.md](play-store-listing.md)
- [x] Datensicherheit aus [play-store-data-safety.md](play-store-data-safety.md)
- [x] Inhaltseinstufung ausgefüllt
- [x] Berechtigungserklärungen für `CALL_PHONE`, Mikrofon-Dienst und
      Akku-Ausnahme (Texte stehen in der Data-Safety-Datei)
- [x] AAB hochgeladen (0.6.2, versionCode 8)
- [x] Am 2026-08-25 eingereicht: 14 Änderungen, Googles erste echte Prüfung
- [x] **Am 2026-09-05 genehmigt** – die App heißt im Store seitdem „DialOS
      Mobil" statt „org.dialos.mobil (unreviewed)"

## 7. Der geschlossene Test

Google verlangt bei neuen Entwicklerkonten vor der Produktionsfreigabe
einen geschlossenen Test mit **mindestens 12 Testern über 14 Tage**. Der
interne Test zählt dafür **nicht** mit.

Eingerichtet als Track „Geschlossener Test - Alpha": Release 0.6.2, Länder
Deutschland/Österreich/Schweiz, Testerliste „Tester", Feedback an
`kontakt@dialos.org`.

**Die Falle, die vier Tage gekostet hat: Eingetragen sein ist nicht
angemeldet sein.** Das Dashboard zählte „0 Tester angemeldet", obwohl die
Adresse in der Liste stand. Jeder Tester muss zusätzlich den Opt-in-Link
öffnen und „Tester werden" antippen. Erst dann zählt Google ihn, und erst
wenn zwölf **gleichzeitig** angemeldet sind, laufen die 14 Tage an. Dieser
Satz gehört in jede Mail an die Tester.

Die Links stehen in der Console unter *Geschlossener Test → Reiter Tester →
Teilnahme am Test*. Für diese App:

```
Im Web:        https://play.google.com/apps/testing/org.dialos.mobil
Auf Android:   https://play.google.com/store/apps/details?id=org.dialos.mobil
```

Der interne Test hat einen **anderen** Link
(`https://play.google.com/apps/internaltest/…`) – die beiden lassen sich
leicht verwechseln.

**Zweite Falle:** Auf manchen Geräten fängt die Play-Console-App
`play.google.com`-Adressen ab, dann öffnet sich nie die Opt-in-Seite. Abhilfe:
den Link von Hand in den Browser kopieren. Auf Stephans Gerät funktioniert
das Erzwingen über Vivaldi:

```bash
ADB=/home/stephan/Android/Sdk/platform-tools/adb
$ADB shell am start -a android.intent.action.VIEW \
  -d "https://play.google.com/apps/testing/org.dialos.mobil" \
  -n com.vivaldi.browser/com.google.android.apps.chrome.IntentDispatcher
```

**Dritte Falle:** Die Adresse in der Testerliste muss ein Google-Konto sein
und auf dem Gerät im Play Store angemeldet – nicht irgendeine Mailadresse.
Bei Nicht-Gmail-Adressen ist das die häufigste Ursache, wenn jemand „Du
bist kein Tester" sieht.

Stand: Die Testerliste hat zwölf Einträge, die Einladungen sind am
2026-09-05 verschickt. Wie viele davon angenommen haben, zeigt das
Dashboard im Kasten „Zugriff auf die Produktionsversion beantragen".

## Was die Prüfung erschweren kann

Ehrlich vorweg, damit es keine Überraschung gibt:

**`CALL_PHONE` ist eine sensible Berechtigung.** Sie wird für Apps
freigegeben, deren Kernfunktion das Telefonieren ist – das trifft hier
eindeutig zu, und die App liest kein Anrufprotokoll (das ist die deutlich
heiklere Berechtigung). Die Chancen stehen gut, aber es ist eine
Einzelfallprüfung durch einen Menschen.

**Der Mikrofon-Dauerbetrieb wird genau angeschaut.** Eine App, die
ununterbrochen zuhört, ist genau das, wovor Google Nutzer schützen will.
Die Argumente hier sind stark: keine Internetberechtigung, quelloffener
Code, Erkennung nachweislich offline, dauerhafte Benachrichtigung. Das
Video sollte das zeigen, nicht nur behaupten.

**`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`** ist der wackeligste Punkt.
Google lässt sie nur für wenige Fälle zu. Sollte sie beanstandet werden:
Sie ist verzichtbar. Die App funktioniert ohne sie, nur eben weniger
zuverlässig im Dauerbetrieb – dann wäre sie zu entfernen und der Hinweis
in „Infos & Einstellungen" umzuformulieren.

**SMS wurde bewusst entfernt** (Version 0.6.0). `SEND_SMS` hätte die
Ablehnung praktisch garantiert: Google lässt SMS-Berechtigungen nur für
eine abschließende Liste zugelassener Anwendungsfälle zu, und ein
Sprachwähler steht nicht darauf.

## Alternative: F-Droid

Falls der Play Store scheitert oder zu mühsam wird, passt F-Droid zu dieser
App besser als zu den meisten: quelloffen, Apache 2.0, keine Tracker, keine
Google-Dienste, funktioniert offline. F-Droid kennt diese
Berechtigungsbeschränkungen nicht. Der Preis ist geringere Reichweite –
die Zielgruppe findet die App dort eher über Empfehlung als über Suche.

Beides schließt sich nicht aus.
