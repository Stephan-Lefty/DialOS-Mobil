# Xiaomi: Einstellungen, damit die Sprachsteuerung durchhält

Auf Xiaomi-Geräten reicht es **nicht**, die Akku-Optimierung auszunehmen.
MIUI bringt eine eigene Prozessverwaltung mit, die Hintergrunddienste
unabhängig vom Android-Standardmechanismus beendet.

Aufgefallen ist das im geschlossenen Test: Eine Testerin mit einem **Xiaomi
Redmi 13C** meldete, die Sprachsteuerung beende sich immer wieder von
selbst. Die App sagte nichts, wurde einfach still, die Benachrichtigung
verschwand – und die Akku-Optimierung war bei ihr bereits ausgenommen.

## Woher diese Angaben stammen

**Alle Bezeichnungen unten sind von einem echten Gerät abgelesen**, nicht
aus dem Gedächtnis oder aus dem Internet zusammengesucht. Eine zweite
Testerin hat am 09.09.2026 ihre Einstellungen abfotografiert (ebenfalls ein
Xiaomi). Die Menüs heißen je nach MIUI- bzw. HyperOS-Fassung
unterschiedlich; weicht etwas ab, sind es meist nur andere Worte für
dasselbe.

Wer diese Datei erweitert: **bitte genauso vorgehen.** Geratene Menüpfade
sind schlimmer als gar keine – besonders für jemanden, der sie sich
vorlesen lassen muss.

## Die wichtigste Einstellung zuerst

### Hintergrund-Autostart

Eine eigene Liste in den Einstellungen, überschrieben mit
**„Hintergrund-Autostart"**. Sie zeigt oben, wie viele Apps im Hintergrund
starten dürfen, darunter die lange Liste derer, die es nicht dürfen.

**DialOS Mobil muss dort eingeschaltet sein.**

Auf dem geprüften Gerät standen nur zwei von 121 Apps auf „erlaubt“ – die
Voreinstellung ist also „verboten“. Genau das ist der wahrscheinlichste
Grund, wenn die Sprachsteuerung nach einiger Zeit verschwindet.

## Die weiteren Stellen

### App-Verhalten bei Nichtnutzung verwalten

Unter **App-Berechtigungen** ganz unten, im Abschnitt „Nicht verwendete
App-Einstellungen“:

> **App-Verhalten bei Nichtnutzung verwalten**
> Berechtigungen entfernen, temporäre Dateien löschen, Benachrichtigungen
> stoppen und die App archivieren

Darunter steht ausdrücklich, was passiert: Kontakte, Mikrofon und Telefon
werden entzogen, wenn die App einige Monate nicht verwendet wird.

**Sollte ausgeschaltet sein.** Für den vierzehntägigen Test spielt es keine
Rolle – es greift erst nach Monaten. Für den Dauerbetrieb schon: Eine
Sprachsteuerung, der man das Mikrofon entzieht, ist keine mehr.

### App-Aktivität bei Nichtbenutzung pausieren

In der **App-Info** unter „Berechtigungen“, ähnlich benannt, aber eine
andere Einstellung:

> **App-Aktivität bei Nichtbenutzung pausieren**
> Berechtigungen entfernen, temporäre Dateien löschen und Benachrichtigungen
> anhalten

**Sollte ausgeschaltet sein.** Auf dem geprüften Gerät war sie es bereits.

### Berechtigungen

Unter **App-Berechtigungen** müssen im Abschnitt „Zugelassen“ stehen:

- Benachrichtigungen
- Kontakte
- Mikrofon
- Telefon

Fehlt eines davon, funktioniert die App nicht oder nur halb. Die Liste
zeigt zu jedem Eintrag, wann zuletzt darauf zugegriffen wurde – daran lässt
sich auch ablesen, ob die Sprachsteuerung überhaupt noch läuft.

### Startbildschirmverknüpfungen

In den App-Einstellungen unter dem Namen der App:

- **Startbildschirmverknüpfungen** – auf dem geprüften Gerät auf **rot (✗)**
- Anzeige auf dem Sperrbildschirm – grün
- Neue Fenster öffnen, wenn im Hintergrund ausgeführt – grün

Für die Sprachsteuerung selbst ist das ohne Belang. **Wer das Widget von
DialOS Mobil auf dem Startbildschirm haben möchte, braucht diesen Punkt
aber vermutlich auf grün.** Das ist noch nicht überprüft – das Widget ist
neu und war auf diesem Gerät noch nicht im Einsatz.

## Wie man merkt, ob es geholfen hat

Ab Version 0.6.5 zählt die App selbst mit: In **„Infos & Einstellungen"**
steht, wie oft das Telefon die Sprachsteuerung beendet hat und wann
zuletzt. Steigt die Zahl nicht mehr, sitzen die Einstellungen.

Ab derselben Version sagt die App außerdem beim Neustart an, dass sie
unterbrochen wurde – vorher verstummte sie wortlos, und wer nicht auf den
Bildschirm sehen kann, merkte es erst, wenn er telefonieren wollte.

## Andere Hersteller

Samsung, Huawei, Oppo und OnePlus haben vergleichbare Mechanismen unter
anderen Namen. Sobald jemand mit so einem Gerät seine Einstellungen zeigt,
gehört das hier ergänzt – **nach demselben Verfahren: abgelesen, nicht
geraten.**
