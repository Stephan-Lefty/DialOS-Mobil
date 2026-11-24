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

Das spart die ganze Nachbearbeitung. Beim letzten Mal war sie der
aufwendigste Teil.

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

**5. Ein Anruf, ohne den Bildschirm anzufassen (etwa 45 Sekunden)**

Telefon hinlegen, sichtbar die Hände wegnehmen.

> „Ab jetzt wird nichts mehr berührt."

„Sprachsteuerung starten" – Ansage abwarten – „Heinz Müller" – Rückfrage
abwarten – „Ja" – der Anruf beginnt. Alle Ansagen ungekürzt.

**6. Der eine Satz, der später Ärger spart (etwa 20 Sekunden)**

> „Eines noch, weil es die häufigste Rückmeldung ist: Nach jedem Update
> aus dem Play Store ist die Sprachsteuerung ausgeschaltet. Das macht
> Android so, nicht die App. Es erscheint dann eine Benachrichtigung – die
> einmal antippen, und es läuft weiter."

**7. Schluss (etwa 10 Sekunden)**

> „Das war die Einrichtung. Sprechen Sie dabei in normaler Lautstärke –
> lauter zu sprechen macht die Erkennung schlechter, nicht besser."

Dieser letzte Satz gehört hinein, obwohl er wie ein Detail wirkt. Er ist
gemessen und der häufigste Bedienfehler.

## Was nicht ins Video gehört

- **Keine Aufzählung der Funktionen.** Widget, Kontrastansicht,
  Stimmenauswahl, zwei SIM-Karten – all das kann die App und hat hier
  nichts zu suchen. Das Video beantwortet eine Frage: Was passiert nach
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
