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

- [ ] Schlüssel erzeugt
- [ ] `keystore.properties` angelegt
- [ ] Schlüssel an einem zweiten Ort gesichert

## 2. Das Paket bauen

```bash
export JAVA_HOME=/home/stephan/.gradle/jdks/eclipse_adoptium-17-amd64-linux.2
./gradlew bundleRelease
```

Ergebnis: `app/build/outputs/bundle/release/app-release.aab` (~56 MB).

- [x] Baut durch, auch mit Verschleierung (ProGuard); Vosk überlebt sie
- [ ] Mit Schlüssel signiert (geht erst nach Schritt 1)

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
- [ ] Datenschutzerklärung unter einer öffentlichen URL erreichbar machen
      (empfohlen: eine Seite unter `dialos.org`)

## 5. Video für die Berechtigungserklärung

Google verlangt für den Mikrofon-Vordergrunddienst eine Bildschirmaufnahme,
die den Ablauf zeigt. Inhalt:

1. App öffnen, „Sprachsteuerung einschalten" antippen
2. Die Ansage abwarten
3. „Sprachsteuerung starten" sagen
4. Einen Namen sagen, mit „Ja" bestätigen
5. Der Anruf beginnt

- [ ] Video aufgenommen (30–60 Sekunden genügen)

## 6. In der Play Console

- [ ] App anlegen, Standardsprache **Deutsch (Deutschland)**
- [ ] Kategorie **Barrierefreiheit**
- [ ] Store-Eintrag aus [play-store-listing.md](play-store-listing.md)
- [ ] Datensicherheit aus [play-store-data-safety.md](play-store-data-safety.md)
- [ ] Inhaltseinstufung ausfüllen
- [ ] Berechtigungserklärungen für `CALL_PHONE`, Mikrofon-Dienst und
      Akku-Ausnahme (Texte stehen in der Data-Safety-Datei)
- [ ] AAB hochladen
- [ ] Geschlossener Test, falls Google ihn für dieses Konto verlangt

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
