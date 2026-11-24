# Video: Was nach der Installation passiert

Drehbuch und Aufnahmeweg, Stand 24.11.2026. Stephans Wunsch: ein Video zur
aktuellen Fassung, das zeigt – und hören lässt –, was nach dem
Herunterladen geschieht.

## Die Vorgabe, aus der sich alles andere ergibt

**Das Video muss mit ausgeschaltetem Bildschirm funktionieren.**

Das klingt widersinnig für ein Video, ist aber der Kern: Ein Teil der
Zielgruppe sieht es nie, sondern hört es. Wenn eine Stelle nur durch das
Bild verständlich wird – „hier tippen Sie auf diesen Knopf" –, ist sie für
genau die Menschen verloren, für die die App gebaut ist.

Daraus folgt:

- **Gesprochene Begleitung ist Pflicht**, keine eingeblendeten Texte. Jeder
  Schritt wird gesagt, nicht gezeigt.
- Knöpfe werden **benannt**, nicht gedeutet: „Sprachsteuerung einschalten",
  nicht „der große blaue Knopf unten".
- Die **Ansagen der App bleiben hörbar** und werden nicht untersprochen.
  Sie sind der eigentliche Inhalt – wer das Video hört, soll wissen, wie
  sich die App anhört.
- Es braucht eine **Textfassung** dazu, für alle, die kein Video abspielen
  können oder wollen.

Das Video hat damit zwei Adressaten zugleich: die helfende Person, die
installiert, und die Person, die die App danach benutzt. Beide bekommen
dasselbe Stück.

## Der Ton ist die technische Hürde

**`adb shell screenrecord` nimmt keinen Ton auf.** Das Werkzeug, mit dem
hier sonst alles am Gerät läuft, liefert ein stummes Video – und ein
stummes Video wäre bei dieser App sinnlos.

Zwei Wege:

1. **Der eingebaute Bildschirmrekorder des Telefons.** Er kann ab
   Android 11 auch den internen Ton und das Mikrofon mitschneiden, also
   die Ansagen der App **und** die gesprochene Begleitung in einem Zug.
   Das ist der einfache Weg. **Wo er auf dem Motorola edge 50 neo sitzt,
   ist nachzusehen, nicht zu raten** – vermutlich in den
   Schnelleinstellungen.
2. **Getrennt aufnehmen und zusammenfügen:** Bild über `adb`, Ton separat,
   danach mit `ffmpeg` zusammenlegen. Mehr Arbeit, dafür saubere Spuren.
   Nur nötig, wenn Weg 1 ausfällt.

Weg 1 zuerst versuchen.

## Datenschutz vorher lösen, nicht hinterher

**Die Lehre aus dem August-Video**, dokumentiert in
`veroeffentlichung.md`: Dort mussten hinterher die Kontaktliste unscharf
gemacht, der Anrufbildschirm mit einem deckenden Balken überklebt und die
Tonspur stellenweise stummgeschaltet werden – Letzteres, weil die App die
Namen **ausspricht**. Schwärzen allein hätte nichts genützt.

Das ist diesmal vermeidbar, und zwar vollständig:

- **Vor der Aufnahme erfundene Kontakte anlegen.** Ein „Heinz Müller"
  genügt. Dann gibt es nichts zu verpixeln, nichts stummzuschalten, und
  die Aufnahme ist sofort veröffentlichungsfähig.
- **Als Rufnummer Stephans zweite Karte nehmen.** Dann klingelt wirklich
  etwas – das Video zeigt einen echten Anruf –, aber es wird niemand
  gestört, und keine fremde Nummer erscheint im Bild oder im Ton.
- Danach die erfundenen Kontakte wieder löschen.
- **Für den Widget-Abschnitt eine leere Seite des Startbildschirms
  benutzen**, auf der nur das Widget liegt. Dann ist das Widget zu sehen
  und nicht die private App-Sammlung – im ersten Versuch standen dort
  Banking-, Reise- und Nachrichten-Apps im Bild. Das löst zugleich den
  Datenschutz und macht die Aufnahme ruhiger.

Das spart die ganze Nachbearbeitung. Beim letzten Mal war sie der
aufwendigste Teil.

## Was der erste Versuch gelehrt hat (24.11.2026)

Die erste Aufnahme (2:04 min) ist unbrauchbar, aber lehrreich. Der Ton war
in Ordnung – durchgehend gesprochen, Mittelwert −29 dB, Spitze −3,5 dB,
nicht übersteuert. **Gescheitert ist das Bild.**

**Die Regel, die gefehlt hat: Das Bild zeigt, wovon gerade die Rede ist.**
35 der 124 Sekunden zeigten den Startbildschirm, während über die App
gesprochen wurde. Das ist der ganze Unterschied zwischen chaotisch und
ruhig – und es macht die Aufnahme unrettbar, weil sich das Bild nicht
herausschneiden lässt, ohne die Erzählung zu zerreißen.

**Nicht gemeint ist „den Startbildschirm nie zeigen".** Seit dem
24.11.2026 liegt dort das Widget, und das *ist* Inhalt – für Menschen mit
motorischen Einschränkungen sogar der wichtigere Weg: eine große Fläche
statt eines kleinen Symbols. Der Startbildschirm gehört also genau dann
ins Bild, wenn das Widget das Thema ist, und sonst nicht.

Dazu drei kleinere Befunde:

- **Die Kartenabfrage bei zwei SIM war nicht eingeplant.** Die App fragt
  vor dem Wählen: „Über welche Karte soll ich anrufen? Für … sagen Sie 1.
  Für … sagen Sie 2." Das gehört in den Ablauf, sonst überrascht es wieder
  – und es ist für Zuhörer mit zwei Karten eine nützliche Stelle.
- **Dabei stehen die Namen der Mobilfunkanbieter im Bild und im Ton.**
  Harmlos, aber man sollte es vorher wissen und nicht hinterher merken.
- **Das Ende verlief sich** in den Benachrichtigungs-Einstellungen. Die
  Aufnahme gehört beendet, solange die App noch zu sehen ist.

**Wo die Löcher waren**, als Hilfe fürs Tempo: zwei lange Pausen bei
47–62 s und 109–121 s, insgesamt rund 60 Sekunden Stille in 124. Wer frei
spricht, unterschätzt das. Lieber kurz sammeln, dann sprechen – eine Pause
vor dem Satz ist besser als eine mittendrin.

## Das Drehbuch

Richtwert zwei bis drei Minuten. Jeder Abschnitt beginnt mit einem Satz,
der sagt, was jetzt kommt – damit man beim Hören weiß, wo man ist.

**1. Was das hier ist (etwa 15 Sekunden)**

> „DialOS Mobil – was nach der Installation passiert. Ich gehe die
> Einrichtung einmal durch, so wie sie beim ersten Start abläuft. Alles,
> was die App sagt, hören Sie mit."

**2. Die App zum ersten Mal öffnen (etwa 30 Sekunden)**

Die App startet, der Einrichtungsbildschirm erscheint. Gesprochen:

> „Beim ersten Start fragt die App nach drei Dingen. Zuerst die
> Berechtigungen: Mikrofon, Kontakte, Telefonieren und Benachrichtigungen.
> Der Knopf heißt ‚Berechtigungen erteilen'."

Die Android-Abfragen laufen durch, jede einzeln bestätigt. Das ist der
Abschnitt, bei dem eine sehende Person helfen muss – und das wird gesagt,
statt es zu verschweigen.

**3. Die Akku-Ausnahme (etwa 20 Sekunden)**

> „Der zweite Knopf heißt ‚Akku-Optimierung ausnehmen'. Ohne ihn schläft
> die App nach einiger Zeit ein und hört das Aktivierungswort nicht mehr.
> Das ist kein Schönheitsfehler, sondern der häufigste Grund, warum sie
> später scheinbar nicht reagiert."

**4. Einschalten – und die App spricht zum ersten Mal (etwa 20 Sekunden)**

> „Der dritte Knopf: ‚Sprachsteuerung einschalten'."

Dann **Pause** und die Ansage der App ungeschnitten stehen lassen. Dieser
Moment ist der wichtigste im ganzen Video: Hier hört man zum ersten Mal,
wie sich das Ding anfühlt.

**5. Ein Anruf, ohne den Bildschirm anzufassen (etwa 60 Sekunden)**

Telefon hinlegen, sichtbar die Hände wegnehmen – **das Bild bleibt auf der
App**.

> „Ab jetzt wird nichts mehr berührt."

„Sprachsteuerung starten" – Ansage abwarten – „Heinz Müller" – Rückfrage
abwarten – „Ja".

**Dann kommt bei zwei Karten noch eine Frage**, und die gehört angesagt,
weil sie sonst wie ein Fehler wirkt:

> „Weil in diesem Telefon zwei SIM-Karten stecken, fragt die App jetzt
> noch, über welche telefoniert werden soll. Mit einer Karte entfällt
> dieser Schritt."

Die Antwort sprechen – „eins" oder „zwei" – und den Anruf beginnen lassen.
Alle Ansagen ungekürzt stehen lassen.

**6. Der zweite Weg: das Widget (etwa 30 Sekunden)**

Jetzt – und nur jetzt – auf die Startbildschirmseite wechseln, auf der das
Widget liegt.

> „Es geht auch ohne das Aktivierungswort. Auf dem Startbildschirm liegt
> ein breiter Balken: einmal antippen, und die App fragt sofort, wen Sie
> anrufen möchten. Wer das Sprechen des Aktivierungsworts umgehen möchte
> oder nur eine große Fläche zum Treffen braucht, nimmt diesen Weg."

Antippen, Ansage abwarten, dann abbrechen oder einen zweiten kurzen Anruf
zeigen. Der Balken zeigt außerdem an, ob die Sprachsteuerung gerade läuft –
das ist einen Halbsatz wert, weil es die häufigste Unsicherheit beantwortet.

**7. Der eine Satz, der später Ärger spart (etwa 20 Sekunden)**

> „Eines noch, weil es die häufigste Rückmeldung ist: Nach jedem Update
> aus dem Play Store ist die Sprachsteuerung ausgeschaltet. Das macht
> Android so, nicht die App. Es erscheint dann eine Benachrichtigung – die
> einmal antippen, und es läuft weiter."

**8. Schluss (etwa 10 Sekunden)** – im App-Fenster oder auf dem Widget,
nicht in irgendwelchen Einstellungen. Die Aufnahme wird beendet, solange
noch zu sehen ist, wovon die Rede war.

> „Das war die Einrichtung. Sprechen Sie dabei in normaler Lautstärke –
> lauter zu sprechen macht die Erkennung schlechter, nicht besser."

Dieser letzte Satz gehört hinein, obwohl er wie ein Detail wirkt. Er ist
gemessen und der häufigste Bedienfehler.

## Was nicht ins Video gehört

- **Keine Aufzählung der Funktionen.** Kontrastansicht, Stimmenauswahl,
  Sprechtempo – all das kann die App und hat hier nichts zu suchen.
  (Das Widget ist seit dem 24.11.2026 die Ausnahme: Es ist kein
  Ausstattungsmerkmal, sondern ein zweiter Weg zum selben Ziel.) Das Video beantwortet eine Frage: Was passiert nach
  der Installation?
- **Keine Musik.** Sie überdeckt genau das, worauf es ankommt.
- **Kein Zeitraffer.** Dass die App einen Moment braucht, bevor sie
  antwortet, ist eine Information und kein Mangel. Wer das Video
  beschleunigt, erzeugt eine Erwartung, die das Gerät nicht einlöst.

## Danach

- Ablegen in der Mediathek von dialos.org, **nicht bei YouTube** – so wie
  beim August-Video entschieden: eigene Infrastruktur, kein fremdes Konto
  nötig.
- Einbinden auf `/dialos-mobil/` (720) und `/en/dialos-mobil/` (721), bei
  der Installationsanleitung.
- **Textfassung daneben stellen.** Das Drehbuch oben ist bereits fast die
  Textfassung; sie braucht nur die Ansagen der App im Wortlaut.
- Das Rohmaterial bleibt **außerhalb des Repos** – es enthält Stephans
  Stimme, so wie beim letzten Mal.
