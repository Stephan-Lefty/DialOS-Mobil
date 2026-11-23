# Eine iPhone-Fassung – was vorher zu klären ist

Stand 22.11.2026. Stephans Absicht: Ende Januar 2027 mit einer
Apple-Fassung beginnen. Dieses Papier ist **keine Planung**, sondern die
Sammlung der Fragen, die vor der ersten Zeile Code beantwortet sein
sollten – weil zwei davon das Vorhaben kippen können.

## 1. Die Frage, an der alles hängt: Darf die App auf iOS überhaupt zuhören?

**Wahrscheinlich nicht so, wie auf Android.** Das ist nicht eine Hürde
unter vielen, sondern der Kern: DialOS Mobil lebt davon, dass das Telefon
daliegt und auf „Sprachsteuerung starten" hört. Auf Android trägt das ein
Vordergrunddienst mit Mikrofon-Berechtigung. Ein solches Gegenstück gibt es
bei iOS nicht.

Was die Recherche vom 22.11.2026 ergeben hat:

- iOS erlaubt Apps **keinen beliebigen Mikrofonzugriff im Hintergrund**.
- Wer trotzdem dauerhaft lauschen will, braucht das Entitlement
  `UIBackgroundModes: audio` – gedacht für Musik, Podcasts und
  Telefonie-Apps.
- **Apple prüft genau dieses Entitlement streng, wenn der Zweck
  Spracherkennung ist**, und es sind Beanstandungen und Ablehnungen
  berichtet.
- Ein Drittanbieter kann **kein eigenes Aktivierungswort auf Systemebene**
  anmelden – das kann nur Siri.
- Während der Nutzung leuchtet dauerhaft die Mikrofonanzeige.

**Daraus folgt eine Produktfrage, keine technische.** Wenn die App auf dem
iPhone nur im Vordergrund zuhören kann, ist sie etwas anderes als auf
Android: kein Zuruf in den Raum mehr, sondern eine App, die man erst
öffnen muss. Für jemanden, der den Bildschirm nicht sieht, ist genau das
Öffnen die Hürde, die wir wegnehmen wollten.

## 2. Die unbequemere Frage: Wofür ist die App auf einem iPhone da?

Auf Android füllt DialOS Mobil eine Lücke – es gibt nichts Vergleichbares,
das ohne Konto, ohne Internet und ohne Blick auf den Bildschirm
funktioniert.

Auf dem iPhone sagt man **„Hey Siri, ruf Heinz Müller an"**, und es
passiert. Siri hört systemweit zu, ist mit VoiceOver eng verzahnt und
braucht keine fremde App. Die Lücke, die diese App auf Android füllt,
sieht auf iOS kleiner aus.

**Das ist kein Gegenargument, aber eine Frage, die vorher zu beantworten
ist.** Mögliche Antworten, alle ungeprüft:

- Das geführte Diktieren von Nummern, bei dem die App vorliest,
  Korrekturen annimmt und Verhörer kennt. Siri kann eine Nummer wählen –
  aber den Umgang mit einer falsch verstandenen Ziffer hat sie nicht.
- Der Übungsmodus (siehe `uebungsmodus-entwurf.md`).
- Dass nichts das Gerät verlässt und kein Konto nötig ist.
- Dass die App ansagt, **was schiefging** – die Fehlerbilder aus 0.6.19
  sind genau das, woran es anderswo fehlt.

Wenn die Antwort lautet „im Wesentlichen dasselbe wie Siri", dann sind drei
Monate Arbeit besser in den offenen Android-Punkten aufgehoben.

**Zu klären ist das nicht am Schreibtisch, sondern bei denen, die es
betrifft.** Es gibt inzwischen einen Kontakt, der beruflich mit der
Zielgruppe arbeitet und zweimal von sich aus geschrieben hat. Die Frage
„Was können Ihre Klienten mit dem iPhone heute *nicht*, was sie bräuchten?"
ist in einer Mail gestellt und spart womöglich ein Quartal.

### Ein Gegenargument aus der Praxis (22.11.2026)

Eine Testperson, die **nicht** blind ist, schrieb:

> Also ich kann etwas schlechter sehen wie normal, ich könnte mein
> Smartphone auch so benutzen ohne die App, aber ich habe mich so daran
> gewöhnt, dass ich es nicht mehr missen möchte.

**Das steht gegen die Vermutung oben.** Wenn jemand die App nicht braucht
und sie trotzdem behält, liegt der Wert nicht allein darin, eine fehlende
Fähigkeit zu ersetzen – sondern im Ablauf selbst. Dann ist „auf dem iPhone
kann Siri das schon" nicht automatisch das Ende der Überlegung: Siri kann
eine Nummer wählen, aber nicht das, was diese App um das Wählen herum tut
– vorlesen, nachfragen, Korrekturen annehmen, Fehlschläge ansagen.

Die Frage aus Abschnitt 2 bleibt damit offen, aber sie ist nicht mehr
rhetorisch. **Wer sie beantwortet, sollte auch Menschen fragen, die die App
nicht nötig hätten.** Das war bisher niemand in der Testgruppe – ausgewählt
wurde nach Bedarf, nicht nach Vorliebe.

## 3. Vosk läuft auf iOS – aber nicht so bequem wie auf Android

Die Erkennung selbst ist kein Hindernis: Vosk unterstützt iOS und Swift,
Audio käme über `AVAudioEngine`. Aber das vorgebaute Paket ist weniger
fertig als auf Android – die iOS-Bibliothek gilt als „auf Anfrage"
erhältlich statt als fertiges SDK, und die Einbindung ist mehr Handarbeit.

**Einzuplanen, nicht abschreckend.** Wichtiger als der Aufwand ist, dass
das Sprachmodell (rund 46 MB) dieselbe Rolle spielt wie auf Android: Es
macht die App groß und unabhängig vom Netz.

## 4. Das Entwicklerkonto – und ein Geld-Hinweis, der hierher passt

Das Apple Developer Program kostet 99 USD im Jahr. **Für gemeinnützige
Organisationen erlässt Apple diese Gebühr vollständig.** Bedingungen, die
auf uns zutreffen würden:

- Es muss ein **Organisationskonto** sein, kein Einzelkonto – die App
  gehört ohnehin dem eingetragenen Verein.
- Die Apps dürfen **nichts kosten** und keine Käufe enthalten. Trifft zu.
- Der Erlass muss **bei der Anmeldung** beantragt werden, nicht danach.

**Zwei Dinge dazu sind ungeprüft** und gehören nachgesehen, bevor man sich
darauf verlässt: ob ein österreichischer Verein für Apple als „nonprofit
organization" zählt, und welche Nachweise und Vorlaufzeiten ein
Organisationskonto braucht. Beides klärt man mit Apple, nicht durch Raten.

**Das ist der Grund, dieses Papier jetzt zu schreiben und nicht im Januar:**
Eine Kontoanmeldung mit Nachweisen und Gebührenerlass ist kein
Nachmittagsprojekt. Wenn das im Januar beginnt, beginnt die Arbeit im März.

## 5. Ein Terminkonflikt, der leicht übersehen wird

**Bis zum 1. Februar 2027 muss ein 16-KB-taugliches Android-Update live
sein**, sonst lassen sich danach gar keine Updates mehr veröffentlichen
(siehe `TODO.md`, „16-KB-Speicherseiten"). Das liegt genau auf dem
geplanten Apple-Start Ende Januar.

**Vorschlag:** Den 16-KB-Fix im Dezember oder Anfang Januar erledigen und
live haben. Dann ist Ende Januar frei – und die Android-Fassung, die jetzt
öffentlich ist, bleibt aktualisierbar.

## Was ich vorschlagen würde, in dieser Reihenfolge

1. **Dezember:** 16-KB-Fix für 0.6.20, live bringen. Harte Frist, kein
   Aufschub möglich.
2. **Dezember/Januar:** Die Produktfrage aus Abschnitt 2 stellen – bei dem
   Kontakt aus der Praxis und, wenn möglich, bei einer iPhone-Nutzerin aus
   der Zielgruppe.
3. **Anfang Januar:** Anmeldung des Organisationskontos bei Apple
   anstoßen, Gebührenerlass gleich mitbeantragen.
4. **Ende Januar:** Erst dann Code – und zwar mit einem ersten
   Wegwerf-Versuch, der nur eine Frage beantwortet: Kommt eine App, die im
   Hintergrund auf ein Aktivierungswort hört, durch Apples Prüfung? Alles
   andere hängt daran.
