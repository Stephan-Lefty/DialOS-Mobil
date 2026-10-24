# Mail zur Fassung 0.6.18

Entwurf vom 24.10.2026. **Verschickt wird erst, wenn Google 0.6.18
durchgelassen hat** – sonst sucht jemand ein Update, das noch nicht da ist.
Nachsehen in der Console unter *Veröffentlichungen – Übersicht*: Solange
dort „Änderungen, die überprüft werden" steht, ist es nicht soweit.

## Warum diese Mail zwei Teile hat

Die Empfänger sind in zwei Lagen, und die Mail muss beiden gerecht werden,
ohne die jeweils andere zu verwirren:

- **Wer die App schon hat** (zehn Anmeldungen, fünf aktive Geräte) bekommt
  das Update über den Play Store, ohne etwas zu tun. Für sie zählt genau
  ein Satz: **Nach dem Update ist die Sprachsteuerung aus.** Wer die
  Benachrichtigung überhört, hat danach eine App, die nichts tut – und
  meldet „geht nicht mehr". Das ist seit Monaten das häufigste
  Missverständnis.
- **Wer noch nicht dabei ist** (fünf der fünfzehn Adressen) scheitert
  höchstwahrscheinlich an der Anmeldung, nicht an der App. Der Opt-in
  verlangt, einen Link mit *demselben Google-Konto* zu öffnen wie auf dem
  Telefon. Für jemanden, der den Bildschirm nicht sieht, ist das die
  schwerste Stelle des ganzen Vorgangs.

## Eine Entscheidung, die im Text steckt

Der Direkt-Download von dialos.org wird **nicht** als gleichwertiger Weg
angeboten, sondern nur als Notausgang. Grund: Wer so installiert, zählt für
Google **nicht** als Tester. Für die Freigabe braucht es zwölf *angemeldete*
Tester über vierzehn Tage; zurzeit sind es zehn. Wer am Play-Test vorbei
installiert, hilft sich selbst, aber nicht dem Projekt. Deshalb steht der
Weg unten und mit Begründung.

---

## Betreff

```
DialOS Mobil: neue Fassung 0.6.18 - und ein wichtiger Hinweis nach dem Update
```

## Text

```
Hallo,

es gibt eine neue Fassung von DialOS Mobil: 0.6.18.

WICHTIG FÜR ALLE, DIE DIE APP SCHON HABEN

Das Update kommt von selbst über den Play Store. Danach ist die
Sprachsteuerung ausgeschaltet - das macht Android so, nicht die App.
Sie müssen sie einmal wieder einschalten:

1. Nach dem Update erscheint eine Benachrichtigung von DialOS Mobil.
2. Diese Benachrichtigung antippen.
3. Die App bestätigt gesprochen, dass sie wieder zuhört.

Wenn Sie die Benachrichtigung überhört haben: App öffnen und
"Sprachsteuerung einschalten" antippen. Das ist der große Knopf auf
der Startseite.

Solange das nicht geschehen ist, reagiert die App auf nichts. Das ist
kein Fehler, sondern eine Vorschrift von Android ab Version 14.

WAS NEU IST

- Lauter sprechen hilft nicht, es macht die Erkennung schlechter. Das
  steht jetzt in der Hilfe, und zwar in der gesprochenen und in der
  geschriebenen. Sprechen Sie normal laut. Der Abstand ist dabei fast
  egal - auch aus einem Meter wird zuverlässig erkannt.

- "Nein" wurde beim Bestätigen manchmal als "ein" verstanden. Dann
  stellte die App dieselbe Frage noch einmal und es ging nicht weiter.
  Das ist behoben.

- Beim Diktieren fiel gelegentlich eine Sieben weg, ohne dass die App
  das gemerkt hat. Aus neun Ziffern wurden dann acht, und die Nummer
  klang trotzdem richtig. Auch das ist behoben.

- Nummern mit Auslandsvorwahl ("null null neunundvierzig ...") kommen
  jetzt vollständig an.

WENN SIE NOCH NICHT AM TEST TEILNEHMEN

Dafür brauchen Sie Ihr Android-Handy und ein paar Minuten. Wichtig: Es
muss dasselbe Google-Konto sein, mit dem Ihr Handy angemeldet ist.

1. Öffnen Sie auf dem Handy diesen Link:

   https://play.google.com/apps/testing/org.dialos.mobil

2. Tippen Sie auf "Tester werden".

3. Danach erscheint auf derselben Seite ein Link zum Play Store.
   Diesem folgen und die App wie gewohnt installieren.

Falls sich der Link nicht öffnet oder eine andere App ihn abfängt:
Adresse von Hand in den Browser eintippen oder hineinkopieren. Das
passiert vor allem, wenn die Play-Console-App auf dem Gerät ist.

Falls "Sie sind kein Tester" erscheint: Dann ist auf dem Handy ein
anderes Google-Konto angemeldet als das, mit dem Sie den Link geöffnet
haben. Schreiben Sie mir in dem Fall, mit welcher Adresse Sie es
versucht haben - ich trage sie nach.

Dieser Schritt lässt sich leider nicht per Sprache erledigen. Wenn
jemand in Ihrer Nähe kurz auf den Bildschirm sehen kann, ist es in
zwei Minuten getan. Danach brauchen Sie den Bildschirm nicht mehr.

WENN ES GAR NICHT KLAPPT

Es gibt die App auch zum direkten Herunterladen, ohne Google-Konto und
ohne Anmeldung:

   https://dialos.org/dialos-mobil/

Der Weg über den Play Store ist mir trotzdem lieber, und zwar aus einem
Grund, den ich offen sagen möchte: Google verlangt zwölf angemeldete
Tester, bevor die App öffentlich erscheinen darf. Zurzeit sind es zehn.
Wer am Play Store vorbei installiert, hilft sich selbst, zählt aber für
diese Hürde nicht mit.

Für Rückfragen bin ich da - auch telefonisch, wenn das einfacher ist.

Viele Grüße
Stephan
```

## Was bewusst nicht drinsteht

- **Keine Tabellen, keine Aufzählungszeichen aus Sonderzeichen.** Der Text
  wird vorgelesen. Nummerierte Schritte liest ein Screenreader sauber,
  Tabellen nicht.
- **Keine Versionsnummern der Zwischenfassungen.** Wer auf 0.6.17 saß,
  bekommt 0.6.18; wer länger nicht aktualisiert hat, bekommt sie ebenso.
  Die Kette dazwischen hilft niemandem beim Verstehen.
- **Kein Hinweis auf den internen Test.** Dieser Track ist seit dem
  24.10.2026 inaktiv, und sein Opt-in-Link (`/apps/internaltest/…`) führt
  nur noch in die Irre. Falls jemand ihn noch gespeichert hat und sich
  meldet: Er ist der Grund, warum diese Person nie eine neue Fassung
  gesehen hat – Einzelheiten in `veroeffentlichung.md` unter „Die Falle mit
  dem internen Test".
- **Keine Entschuldigung dafür, dass die Korrekturen vom 07.10. erst jetzt
  ankommen.** Das ist unsere Panne, nicht ihr Problem; eine Erklärung dazu
  würde die Mail verlängern, ohne jemandem zu helfen.
