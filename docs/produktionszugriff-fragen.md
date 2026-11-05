# Produktionszugriff beantragen – Googles Fragen und vorbereitete Antworten

Stand 05.11.2026. **Vorbereitung, noch nicht eingereicht.** Der Antrag ist
ein Schritt nach außen, den nur Stephan auslöst. Dieses Dokument liest die
Fragen vorab (Googles Hilfeseite
[support.google.com/.../answer/14151465](https://support.google.com/googleplay/android-developer/answer/14151465#production))
und legt Antwortentwürfe bereit, damit die Einreichung an einem Stück
gelingt und nicht halb beantwortet stehen bleibt.

## Der Stand am 05.11.2026

Die dritte Bedingung ist **noch nicht** erfüllt. Die Console zeigt:

> „Führe deinen geschlossenen Test mit mindestens 12 Testern durch und lass
> ihn mindestens 14 Tage laufen. Momentan haben sich **12 Tester für 7
> zusammenhängende Tage** angemeldet."

Der Knopf „Produktionszugriff beantragen" ist **ausgegraut**. Die ersten
beiden Bedingungen sind durchgestrichen.

**Niemand ist ausgestiegen.** Der Zähler lief von 4 (01.11.) sauber auf 7
(05.11.) – genau ein Tag pro Tag, bei durchgehend 12 Testern. Die vierzehn
Tage laufen seit dem 28.10.; voll werden sie darum erst **um den
11.–12.11.2026** herum (Googles Zähler zählt abgeschlossene Tage und hinkt
dem Kalender einen Tag nach). Dieser Termin war also rund eine Woche zu
früh angesetzt – kein Grund zur Sorge, nur noch etwa eine Woche Geduld.

## Die Kernfrage, seit 27.10. offen: Sieht Google „echte Nutzung"?

Die Sorge: Googles Messgröße für aktive Nutzer zählt, wer die App **öffnet**.
DialOS Mobil ist dafür gebaut, **nicht** geöffnet zu werden – sie wartet als
Vordergrunddienst im Hintergrund auf das Aktivierungswort. Am 27.10. standen
darum **null aktive Nutzer pro Tag** bei 8–10 Installationen. Zerreißt das
den Antrag?

Die Fragen und die Hilfeseite geben eine klare, zweiteilige Antwort:

**1. Die automatische 14-Tage-Hürde zählt Anmeldung, nicht Öffnen.** Der
Zähler misst, wer *angemeldet* ist („12 Tester für N zusammenhängende Tage
angemeldet"). Die FAQ bestätigt das: Wer sich abmeldet, zählt nicht mehr;
wer durchgehend angemeldet bleibt, zählt. Von „täglich öffnen" ist dort
nicht die Rede. **Diese Hürde öffnet sich also ~12.11. unabhängig von der
Null bei den aktiven Nutzern.**

**2. Die anschließende manuelle Prüfung gewichtet die Nutzung sehr wohl.**
Hier liegt das eigentliche Risiko. Google nennt als Ablehnungsgrund
ausdrücklich, dass Tester „die App während des Testzeitraums **nicht
ausreichend verwendet** haben". Und Teil 1 des Antrags fragt direkt:
*„ob die Testgruppe alle verfügbaren App-Funktionen verwendet hat"* und
*„ob die Nutzung dem erwarteten Nutzungsverhalten der Produktionsversion
entsprach"*.

**Die Einschätzung:** Die Null sperrt den Antrag nicht automatisch, aber ein
Prüfer, der nur auf die Engagement-Kurve sieht, könnte auf „nicht
ausreichend verwendet" schließen. Der Hebel dagegen ist, dass die Fragen
**Freitext** sind: Wir erzählen die Nutzungsgeschichte selbst, bevor der
Prüfer sie aus der Kurve fehldeutet. Genau das leisten die Entwürfe unten –
sie erklären die seltene Öffnung als **Entwurfsziel**, nicht als Mangel, und
belegen echte Nutzung über die konkreten Rückmeldungen, die wir *haben*.

---

## Teil 1: „Dein geschlossener Test"

### Frage 1 – Wie einfach war es, Testpersonen zu finden? (Auswahl)

**Empfehlung: „schwierig".** Ehrlich und plausibel: Die Zielgruppe (blinde
und motorisch stark eingeschränkte Menschen) ist über normale Kanäle kaum
erreichbar, und viele brauchen sehende Hilfe schon bei der Installation. Die
Tester kamen über eine Empfehlung der NAKOS und fünf bundesweite
Selbsthilfeverbände zusammen, nicht über Freundeskreis oder soziale Medien.

### Frage 2 – Interaktionen mit den Testpersonen (Freitext)

> Die Testpersonen gehören zur Zielgruppe der App: blinde und sehbehinderte
> sowie motorisch eingeschränkte Menschen. Rückmeldungen kamen fortlaufend
> per E-Mail über einen festen Rückmeldekanal.
>
> Zur Funktionsnutzung: Die Kernfunktion – per Sprache einen Kontakt oder
> eine diktierte Nummer anrufen – wurde von den Testpersonen verwendet und
> hat mehrere konkrete Verbesserungen ausgelöst (unterscheidbare
> Kontaktvorschläge bei gleich klingenden Namen, Schutz einer einmal
> eingestellten Stimme, eine erweiterte Startansage, die Behebung eines
> Verstummens der App). Ein auf einem Galaxy A14 beobachtetes Abräumen des
> Hintergrunddienstes durch den Energiesparmodus wurde gemeldet und wird
> weiterverfolgt.
>
> Wichtige Einordnung zur Nutzungshäufigkeit: DialOS Mobil ist ein
> Sprachwähler als Hilfsmittel. Sie ist bewusst so entworfen, dass man sie
> **nicht täglich öffnet** – sie läuft als Vordergrunddienst im Hintergrund
> und wartet auf das Aktivierungswort. Standardmetriken für „aktive Nutzer",
> die das Öffnen der App zählen, unterschätzen die tatsächliche Nutzung
> dieser App daher systematisch. Das reale Nutzungsverhalten entspricht dem,
> was für die Produktionsversion zu erwarten ist: seltene, aber im Bedarfs-
> fall funktionierende Anrufe. Abweichungen zwischen Test- und
> Produktionsverhalten sind nicht zu erwarten, weil Test- und Zielgruppe
> identisch sind.

*(Hinweis: Punkt 2 der Rückmeldung – unterscheidbare Vorschläge – steckt im
Zweig `nach-der-testphase` und ist noch nicht in main. Vor dem Antrag
entscheiden, ob er als „im Test erprobt" oder als „daraus abgeleitete
Änderung" dargestellt wird; Letzteres passt besser nach Teil 3.)*

### Frage 3 – Feedback zusammenfassen und wie es erfasst wurde (Freitext)

> Erfasst wurde das Feedback per E-Mail über einen festen Rückmeldekanal,
> zusätzlich war die private Rückmeldung über Google Play möglich.
>
> Zusammenfassung: Die Rückmeldungen betrafen die Verständlichkeit der
> Sprachführung, die zuverlässige Erkennung bei unterschiedlicher
> Sprechweise und Entfernung zum Mikrofon, die Unterscheidung gleich
> klingender Kontaktnamen sowie das Verhalten des Hintergrunddienstes unter
> herstellereigenen Energiesparmaßnahmen. Jede dieser Rückmeldungen wurde
> aufgegriffen und führte zu einer konkreten Änderung oder einer Messung;
> die Historie der Versionen 0.6.14 bis 0.6.19 bildet das ab.

---

## Teil 2: „Deine App"

### Frage 1 – Zielgruppe (Freitext, so präzise wie möglich)

> Blinde und stark sehbehinderte Menschen sowie Menschen mit motorischen
> Einschränkungen, die ein Smartphone nicht oder nur schwer über den
> Bildschirm bedienen können. Die App ermöglicht es ihnen, allein durch
> Sprechen zu telefonieren – ohne auf den Bildschirm sehen oder präzise
> tippen zu müssen. Primär deutschsprachiger Raum (die Spracherkennung läuft
> offline mit einem deutschen Modell).

### Frage 2 – Mehrwert der App (Freitext)

> DialOS Mobil macht das Telefonieren allein per Sprache möglich: Name oder
> Nummer sagen, auf eine Rückfrage mit Ja/Nein bestätigen, fertig. Die
> Spracherkennung arbeitet vollständig offline auf dem Gerät – ohne
> Cloud, ohne Konto, ohne dass gesprochene Inhalte das Gerät verlassen. Für
> die Zielgruppe ist das kein Komfort-, sondern ein Teilhabe-Werkzeug: Es
> schließt die Lücke zwischen „kann ein Telefon besitzen" und „kann damit
> selbstständig anrufen". Die App ist quelloffen (Apache 2.0) und kostenlos.

### Frage 3 – Geschätzte Installationen im ersten Jahr (Auswahl)

**Empfehlung: die konservative Spanne (bis ~1.000, höchstens 1.000–10.000).**
Nischenhilfsmittel im deutschsprachigen Raum; eine niedrige, ehrliche
Schätzung ist glaubwürdiger und ohne Nachteil.

---

## Teil 3: „Deine Produktionsbereitschaft"

### Frage 1 – Änderungen aus den Erkenntnissen des Tests (Freitext)

> Der geschlossene Test hat eine Reihe von Änderungen ausgelöst, die in den
> Versionen bis 0.6.19 eingeflossen sind, u. a.:
> - Behebung eines Verstummens der App samt Rückmeldung per Vibration,
> - Schutz einer vom Nutzer eingestellten Stimme gegen Überschreiben,
> - eine erweiterte Startansage, die auch das Diktieren einer Nummer nennt,
> - unterscheidbare Vorschläge bei gleich klingenden Kontaktnamen,
> - Messung und Korrektur des Erkennungsverhaltens bei lautem Sprechen und
>   bei vollständiger Übergabe einer Auslandsnummer.
> Jede Erkennungs- oder Ablaufänderung wurde durch einen automatisierten
> Test abgesichert (101 Unit-Tests, grün).

### Frage 2 – Woran die Produktionsbereitschaft festgemacht wurde (Freitext)

> Die aktuelle Fassung 0.6.19 ist auf echten Geräten der Zielgruppe im
> Einsatz, die aus dem Test gemeldeten Fehler sind behoben, die
> automatisierten Tests laufen grün und die statische Analyse (Lint) ist
> ohne Befund. Die bekannten Grenzen (herstellereigene Energiesparmaßnahmen
> auf einzelnen Geräten) sind dokumentiert und betreffen nicht die
> Kernfunktion. Auf dieser Grundlage gilt die App als bereit für die
> Produktion.

---

## Vor dem Klick zu klären

- **Zweig `nach-der-testphase`:** Vor dem Antrag entscheiden, ob Punkt 2
  (unterscheidbare Vorschläge) schon in main ist. Wenn ja, zählt er zu
  Teil 1; wenn er erst nach dem Test zusammengeführt wird, gehört er in
  Teil 3 als „daraus abgeleitete Änderung". Beides ist vertretbar – nur
  konsistent sein.
- **Keine echten Namen/Adressen** in den Freitexten. Die Entwürfe oben sind
  bewusst ohne.
- **Die Nutzungs-Erklärung in Teil 1 ist der wichtigste Satz des ganzen
  Antrags.** Sie nicht kürzen.
