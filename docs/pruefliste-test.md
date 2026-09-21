[Deutsch](pruefliste-test.md) | [English](pruefliste-test.en.md)

# Prüfliste für den geschlossenen Test

Für die Testpersonen von **DialOS Mobil**, Fassung 0.6.13 (21.09.2026).

Diese Liste ist zum Vorlesen gedacht – die Punkte stehen einzeln und
durchnummeriert, ohne Tabellen und ohne Bilder, damit ein Screenreader sie
am Stück durchgehen kann.

**Wichtig vorweg:** Sie müssen nicht alles durcharbeiten. Schon eine einzige
Rückmeldung zu einem einzigen Punkt hilft. Und die Punkte 1 bis 3 sind die
wichtigsten – wenn Sie nur Zeit für drei haben, dann für diese.

Was uns dabei am meisten hilft: **nicht nur, ob etwas geht, sondern was Sie
gehört haben.** Wenn die App etwas Falsches sagt, ist der genaue Wortlaut
Gold wert.

## Das Wichtigste

### 0. Sehen Sie bitte zuerst nach diesem einen Schalter

In der App unter **Infos & Einstellungen** gibt es den Schalter **„Auf
‚Sprachsteuerung starten' hören"**. Der muss **an** sein.

Warum das vorneweg steht: Am 21.09.2026 hat sich beim Testen gezeigt, dass
die App bei ausgeschaltetem Schalter das Aktivierungswort zwar einwandfrei
versteht – aber nichts tut und nichts sagt. Von außen sieht das genauso aus,
als würde sie einen nicht hören. Wer das erlebt, sucht den Fehler bei der
eigenen Aussprache, und niemand kommt auf die Idee, dass ein Schalter schuld
ist.

Ab dieser Fassung sagt die App in dem Fall Bescheid. Trotzdem: Wenn das
Aktivierungswort bei Ihnen bisher nie funktioniert hat, ist dieser Schalter
der erste Ort zum Nachsehen – und bitte sagen Sie uns, wie er stand. Das ist
für uns wertvoller als jede andere Rückmeldung, weil wir dann wissen, ob wir
einen echten Erkennungsfehler jagen oder nur einen unglücklichen Schalter.

### 1. Hört die App zu, ohne dass Sie den Bildschirm berühren?

Sagen Sie – ohne das Telefon anzufassen, aus etwa einem Meter Abstand:

```
Sprachsteuerung starten
```

Die App sollte antworten: „Wen möchten Sie anrufen?"

Bitte probieren Sie das **mehrmals über den Tag verteilt**, auch wenn das
Telefon in der Tasche liegt oder auf dem Tisch. Und sagen Sie uns, **wie oft
von wie vielen Versuchen** es geklappt hat. Diese Zahl ist die wichtigste
Rückmeldung aus dem ganzen Test.

### 2. Nach einem Neustart des Telefons

Starten Sie das Telefon einmal neu. Danach sollte eine Benachrichtigung
kommen, **die man hört** – mit Ton, nicht stumm im Schacht.

Bis zur Vorversion war die App nach jedem Neustart taub, ohne es zu sagen.
Sie zeigte „DialOS Mobil hört zu" und hörte nichts. Genau das soll jetzt
nicht mehr vorkommen.

Bitte melden: Kam die Benachrichtigung? War sie hörbar? Und lief die
Sprachsteuerung nach einem Tippen darauf wieder?

### 3. Wenn das Telefon stumm gestellt ist

Stellen Sie das Telefon leise oder stumm und sagen Sie dann
„Sprachsteuerung starten".

Die App sollte die Lautstärke selbst so weit anheben, dass Sie die Antwort
hören. Wenn Sie vorher lauter gestellt hatten, soll das so bleiben.

## Wenn Sie mögen

### 4. Ohne das Aktivierungswort starten

Sagen Sie zu Google: „Hey Google, öffne DialOS Mobil". Wenn in den
Einstellungen „Beim Öffnen der App gleich einschalten" an ist, fragt die App
sofort, wen Sie anrufen möchten – ohne dass Sie den Bildschirm berühren.

### 5. Die Leiste auf dem Startbildschirm

Die App bietet beim Einrichten an, eine breite Leiste auf den
Startbildschirm zu legen. Ein Tippen darauf genügt, dann können Sie sprechen.

Bitte melden: Ließ sich die Leiste hinzufügen? Findet Ihr Screenreader sie?
Und sagt sie das Richtige – also „Antippen zum Einschalten", wenn die
Sprachsteuerung aus ist, und „Antippen und sprechen", wenn sie läuft?

### 6. Ein Kontakt mit mehreren Nummern

Wenn Sie jemanden mit privater **und** mobiler Nummer im Adressbuch haben:

```
Michaela privat anrufen
```

Oder erst den Namen sagen und auf die Rückfrage mit „privat", „mobil" oder
„Arbeit" antworten.

### 7. Namen, die ähnlich klingen

Rufen Sie jemanden, dessen Name im Adressbuch mehrfach oder ähnlich
vorkommt. Schlägt die App die richtige Person vor? Liest sie **alle**
Treffer vor oder bricht sie zu früh ab?

### 8. Eine Nummer diktieren

Sagen Sie „Nummer wählen", sprechen Sie die Ziffern einzeln, und beenden Sie
mit „fertig". Wenn Sie sich verspricht: „letzte Ziffer löschen" nimmt nur
die letzte zurück, nicht alles.

Machen Sie dabei ruhig längere Pausen – die App soll Ihnen Zeit lassen.

### 9. Mittendrin abbrechen

Sagen Sie irgendwann „Abbrechen". Die App sollte danach weiter ansprechbar
sein und nicht verstummen.

### 10. Stimme und Tempo

In „Infos & Einstellungen" lassen sich die Stimme und vier
Geschwindigkeitsstufen durchschalten. Nach jedem Tippen kommt eine Hörprobe.

Bitte melden: Ist eine Stimme dabei, die Sie gut verstehen? Und ist die
schnellste Stufe schnell genug?

## Was wir schon wissen und nicht lösen können

Damit Sie es nicht erst suchen müssen:

- **Schweizerdeutsch versteht die App nicht.** Die Spracherkennung läuft
  offline auf dem Telefon und ist auf Hochdeutsch trainiert. Ein Schweizer
  Modell dieser Größe gibt es nicht.
- **Den Flugmodus kann die App nicht ausschalten.** Das ist seit Android 4.2
  Systemapps vorbehalten. Sie sagt Ihnen aber jetzt, dass er an ist, und
  führt Sie mit einer Ansage zu der Stelle, wo Sie ihn abschalten können.
- **Anrufe über WhatsApp oder Signal gehen nicht.** Beide bieten keine
  Schnittstelle dafür an; das ist auf einem echten Gerät nachgeprüft.
- **Kurznachrichten gibt es bewusst nicht.** Google lässt die
  SMS-Berechtigung nur für eine feste Liste von Anwendungsfällen zu, auf der
  ein Sprachwähler nicht steht.

## Wie Sie die Rückmeldung schicken

Formlos per Mail an Stephan genügt – ein Satz pro Punkt reicht völlig, und
Sie dürfen auch einfach drauflosschreiben, statt sich an die Nummern zu
halten.

Besonders wertvoll ist:

- **Der genaue Wortlaut**, wenn die App etwas Falsches oder Verwirrendes
  gesagt hat.
- **Was Sie gesagt haben**, wenn sie Sie nicht verstanden hat.
- **Welches Telefon** Sie benutzen, wenn etwas gar nicht funktioniert.
  Besonders bei Xiaomi, Redmi und POCO gibt es eine Einstellung namens
  „Hintergrund-Autostart", die Apps abschaltet, ohne es zu melden – siehe
  [xiaomi-einstellungen.md](xiaomi-einstellungen.md).
