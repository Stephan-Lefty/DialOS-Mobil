# Aufruf im BLINDzeln-Magazin

Eingereicht am 23.09.2026 an `magazin@blindzeln.org`, **erscheint Mitte
Oktober 2026**.

BLINDzeln (<https://blindzeln.org/>) betreibt Mailinglisten, Forum, Chat,
Newsportal und Podcasts für blinde und sehbehinderte Menschen und gehört
mit ml4free zusammen – siehe [testersuche-kanaele.md](testersuche-kanaele.md).
Kontakt lief über Sebastian.

## Die Vorgaben, erfragt statt angenommen

- **Keine Grafiken.** Ausdrücklich so gewünscht. Das Publikum liest mit
  dem Screenreader; eine Grafik wäre dort bestenfalls Dekoration.
- **Länge:** ausführlich erlaubt, „soll halt keine Seite werden". Der
  eingereichte Text hat **298 Wörter / rund 2.000 Zeichen** – etwa zwei
  Drittel einer A4-Seite.
- **Form:** direkt in der Mail, keine Anhänge.
- **Redaktionsschluss: nicht genannt.** Deshalb steht im Anschreiben, dass
  sich Stephan meldet, falls sich die Zahl der fehlenden Testpersonen vorher
  ändert.

## Vor dem Erscheinen prüfen

Im Text steht **„Aktuell fehlen mir noch zwei"** (Stand 23.09.2026). Das ist
die einzige Stelle, die veralten kann. Bei einer Änderung vor Mitte Oktober
eine kurze Mail an Sebastian – das kostet eine Minute und erspart dem
Magazin eine falsche Angabe.

Ebenfalls im Text: „dreißig Verbesserungen in vier Wochen". Nachzählen mit

```bash
sed -n '/^### 0\.6\.15/,/^### 0\.6\.2 /p' README.md | grep -c "^- \*\*"
```

## Der eingereichte Text

```
DialOS Mobil: Telefonieren, ohne den Bildschirm zu sehen

Ich suche Testpersonen für eine kostenlose Android-App. Mit ihr lässt
sich allein durch Sprechen telefonieren: Man sagt "Sprachsteuerung
starten", nennt einen Namen aus den eigenen Kontakten, bestätigt mit
"Ja", und der Anruf läuft. Auch eine Nummer lässt sich Ziffer für Ziffer
diktieren.

Die Erkennung läuft vollständig auf dem Telefon. Keine Cloud, kein
Konto, keine Werbung, keine Datenweitergabe, und ohne Internet
funktioniert sie genauso. Der Quelltext ist offen (Apache-Lizenz).

Warum ich suche: Google verlangt vor der Veröffentlichung einen
geschlossenen Test mit zwölf Personen über vierzehn Tage. Aktuell fehlen
mir noch zwei. Nötig sind ein Android-Telefon mit SIM-Karte, ein
Google-Konto darauf und die Bereitschaft, die App zwei Wochen
installiert zu lassen. Wichtig dabei: Ich brauche die Mailadresse, die
auf Ihrem Telefon im Play Store angemeldet ist - nur darüber kann Google
die App freischalten. Sie liegt ausschließlich in der Play Console und
wird nach dem Test gelöscht.

Benutzen müssen Sie die App nicht; wer sie nur installiert lässt, hilft
mir schon. Wer sie ausprobiert, hilft mehr. Ein Beispiel dafür, warum:
Eine Testerin meldete kürzlich, die App finde ihre Kontakte nicht. Die
App wusste in dem Moment genau, wie viele sie gelesen hatte, und sagte
es nicht. Ohne Blick auf den Bildschirm gibt es keinen Weg, das
nachzuprüfen - man sucht den Fehler dann bei der eigenen Aussprache.
Seit der nächsten Fassung nennt sie die Zahl beim Einschalten. So sind in
vier Wochen dreißig Verbesserungen entstanden, fast alle aus
Rückmeldungen, und Antwort gibt es bei mir meist am selben Tag.

Zwei Grenzen vorweg: Die App versteht nur Hochdeutsch, kein
Schweizerdeutsch - das Sprachmodell arbeitet offline und ist
entsprechend trainiert. Und Anrufe über WhatsApp oder Signal sind nicht
möglich, dafür gibt es keine Schnittstelle.

Anmeldung und Einzelheiten:

https://dialos.org/dialos-mobil-tester-gesucht/

Fragen gerne an kontakt@dialos.org

Stephan Rösner
```

## Warum der Text so gebaut ist

**Das Kontakt-Beispiel steht drin, obwohl es Platz kostet.** Es belegt in
fünf Sätzen, was der Rest behauptet, und beschreibt ein Ärgernis, das diese
Leserschaft kennt: Software, die etwas weiß und es für sich behält. Der Fall
ist echt – Lydia Oberländer am 22.09.2026, behoben in 0.6.15 am selben Tag.

**Der Google-Konto-Hinweis steht mittendrin, nicht am Ende.** Das ist die
häufigste Hürde bei geschlossenen Tests und hat in diesem Projekt Wochen
gekostet: Vier der zwölf Adressen waren keine Gmail-Adressen.

**Die Grenzen stehen im Aufruf, nicht in der Enttäuschung danach.** Wer sich
meldet und erst dann merkt, dass die App sein Schweizerdeutsch nicht
versteht, ist kein gewonnener Tester, sondern ein verlorener Kontakt – und
in einer Leserschaft, die sich untereinander kennt, spricht sich das herum.
