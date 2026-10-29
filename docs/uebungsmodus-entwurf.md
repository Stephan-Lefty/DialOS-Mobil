# Übungsmodus – Entwurf

Stand 29.10.2026. **Entwurf, nicht gebaut.** Vorgesehen für die Zeit nach
der Testphase (die vierzehn Tage laufen seit dem 28.10.).

## Woher der Vorschlag kommt

Aus der Praxis, 28.10.2026, von jemandem, der beruflich mit der Zielgruppe
arbeitet:

> Menschen, die nicht so Smartphone-affin sind und dazu noch älter und
> schlecht sehen, brauchen Eingewöhnungszeiten. In denen können sie zum
> Testen ja nicht immer einen Bekannten anrufen im Sinne „hat's geklappt?".

## Warum das wichtiger ist, als es klingt

**Es gibt bis heute keine Möglichkeit, diese App gefahrlos auszuprobieren.**
Jeder Versuch ruft jemanden an. Wer unsicher ist, übt deshalb nicht – und
wer nicht übt, benutzt die App im Ernstfall nicht.

Das deckt sich mit einer Messung: Am 27.10.2026 lagen die aktiven Nutzer
pro Tag bei **null**, bei acht bis zehn Installationen. Eine Testperson
schrieb, sie nutze die App „sehr selten". Ein Teil davon ist sicher
Gelegenheit – aber ein Teil ist wahrscheinlich genau diese Hemmschwelle.

Ein Übungsmodus nimmt sie weg, und zwar ohne jemanden um etwas zu bitten.

## Der Ablauf

Alles läuft wie sonst – Name oder Nummer, Rückfragen, Bestätigung. Nur der
letzte Schritt wird ersetzt:

```
„Übung. Wen möchten Sie anrufen?"
→ „Heinz Müller"
„Heinz Müller, mobil. Soll ich anrufen? Sagen Sie Ja oder Nein."
→ „Ja"
„Übung beendet. Jetzt hätte ich Heinz Müller angerufen."
```

Starten per Sprache (`Übung starten`, `Üben`) und per Schaltfläche in der
App. Beenden mit `Abbrechen` wie überall sonst.

## Die Gefahr, und sie ist der eigentliche Entwurfspunkt

**Jemand übt, vergisst es – und glaubt im Ernstfall, er habe angerufen.**
Für eine App, die Menschen benutzen, die den Bildschirm nicht sehen, wäre
das der schlimmste denkbare Fehler: Sie hielte sich für hilfreich und wäre
gefährlich.

Drei Vorkehrungen, und keine davon ist verzichtbar:

1. **Der Modus hält nie länger als einen Durchgang.** Nach dem Satz „Übung
   beendet" ist die App zurück im normalen Zustand. Es gibt keinen
   Schalter, der dauerhaft an bleibt, und damit keinen Zustand, den man
   vergessen kann.
2. **Jede Ansage im Übungsmodus beginnt mit „Übung".** Nicht nur die erste.
   Wer zwischendurch abgelenkt wird, hört es beim nächsten Satz wieder.
3. **Der Abschlusssatz sagt ausdrücklich, dass nicht angerufen wurde.**
   „Übung beendet. Jetzt hätte ich … angerufen." – im Konjunktiv, nicht
   „Anruf simuliert" oder ähnlichem Fachdeutsch.

## Wie sichergestellt wird, dass wirklich nichts gewählt wird

Nicht dadurch, dass der Aufruf übersprungen wird – sondern doppelt:

- `DialogController` ruft `listener.onPlaceCall(…)` im Übungsmodus gar
  nicht erst auf.
- `VoiceService.onPlaceCall` prüft den Zustand **ein zweites Mal** und
  bricht ab, wenn eine Übung läuft. Diese zweite Prüfung ist bewusst
  redundant: Ein künftiger Umbau am Ablauf darf nicht versehentlich einen
  Weg öffnen, der wählt.

Dazu ein Test, der genau das festhält – dass ein vollständiger Durchgang im
Übungsmodus **null** Wählversuche erzeugt. Reine Ablauflogik gehört dafür
in eine eigene Klasse; `DialogController` hängt an Android und ist nicht
unit-testbar (Vorbilder: `StartVersuche`, `StimmenWahl`, `Unterscheidung`).

## Was der Modus nicht sein soll

**Kein Tutorial mit Lektionen.** Der Vorschlag nannte beides, „Tutorial /
Training". Ein geführter Lehrgang klingt verlockend, hat aber einen Haken:
Er lehrt seine eigene Reihenfolge, nicht die der App. Was diese Zielgruppe
braucht, ist nicht eine Erklärung, sondern ein gefahrloser Ernstfall.

**Keine Dauerschleife.** Nach jedem Durchgang endet die Übung. Wer noch
einmal will, sagt es noch einmal – das kostet einen Satz und schließt
dafür die Vergessens-Gefahr aus.

## Offen

- **Was passiert bei einem Kontakt, den es nicht gibt?** Im Übungsmodus ist
  „nicht gefunden" ein nützliches Ergebnis, kein Fehlschlag – der Ablauf
  sollte das sagen und weiterlaufen, statt abzubrechen.
- **Soll die Übung auch das Diktieren von Nummern abdecken?** Dafür
  spricht, dass genau dort die meisten Fehler entstehen. Dagegen, dass eine
  geübte Nummer am Ende niemanden anruft und der Nutzer nicht erfährt, ob
  sie richtig gewesen wäre. Ein Vorschlag: Die App liest die erkannte
  Nummer am Ende Ziffer für Ziffer vor – dann kann man sie mit dem Zettel
  vergleichen, und das ist sogar mehr wert als ein echter Anruf.
