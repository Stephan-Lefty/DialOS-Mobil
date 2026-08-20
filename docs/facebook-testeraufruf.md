# Facebook: Aufruf zur Testteilnahme

Stand: 20.08.2026. Bezieht sich auf App-Version 0.6.2 und die Seite
https://dialos.org/dialos-mobil-tester-gesucht/

**Bild dazu:**
[`screenshots/facebook/facebook-testeraufruf-1200x630.png`](../screenshots/facebook/facebook-testeraufruf-1200x630.png),
erzeugt von [`facebook-grafik.py`](facebook-grafik.py). 1200×630 ist das
Format, das Facebook für geteilte Beiträge erwartet – bei einem anderen
Seitenverhältnis schneidet es eigenmächtig zu, meist quer durch die Schrift.
Deshalb **nicht** einen der App-Screenshots direkt nehmen: Die sind
1200×2670, davon zeigt Facebook einen schmalen Streifen aus der Mitte.

Das Skript prüft beim Erzeugen die Kontraste nach WCAG gegen den
Farbverlauf an genau der Stelle, an der die jeweilige Zeile steht, und
bricht ab, wenn ein Wert unter der Schwelle liegt. Eine Grafik, die für
Menschen mit Sehbehinderung wirbt, sollte selbst lesbar sein.

**Wichtig beim Posten:** Den Link erst einbauen, wenn die Vorschau geladen
hat, und dann die Link-Vorschau stehen lassen. Facebook drosselt Beiträge
mit nacktem Link im Text weniger stark, wenn die Vorschaukarte da ist.

---

## Fassung 1: die persönliche (empfohlen)

> Meine Mutter kann kein Smartphone bedienen. Nicht, weil sie nicht
> telefonieren kann – sondern weil man ein Telefon heute *ansehen* muss, um
> es zu benutzen.
>
> Deshalb habe ich eine App gebaut, die das umdreht.
>
> Man sagt: „Sprachsteuerung starten." Die App antwortet: „Wen möchten Sie
> anrufen?" Man sagt einen Namen. Die App fragt zurück, ob sie wählen soll.
> Man sagt Ja. Fertig. Der Bildschirm bleibt unberührt.
>
> Sie heißt **DialOS Mobil**, ist kostenlos und quelloffen, und – das ist
> mir das Wichtigste – **sie sendet nichts**. Die Spracherkennung läuft
> vollständig auf dem Handy. Die App hat nicht einmal eine
> Internetberechtigung, sie *kann* technisch gar nichts verschicken. Kein
> Ton, kein Kontakt, kein Wort verlässt das Gerät. Sie funktioniert deshalb
> auch im Funkloch und im Flugmodus.
>
> Jetzt brauche ich Hilfe. Bevor Google eine App in den Play Store lässt,
> verlangt es einen Test **mit mindestens 12 Personen über 14 Tage**. Ohne
> diese 12 Leute bleibt sie liegen.
>
> **Du brauchst nur:** ein Android-Handy mit SIM-Karte, ein Google-Konto,
> und die Geduld, die App zwei Wochen drauf zu lassen.
>
> Und wenn du eine einzige Sache zurückmeldest, dann bitte diese: Springt
> das Aktivierungswort bei *deiner* Stimme zuverlässig an? Bisher habe ich
> das nur mit meiner eigenen geprüft – und eine Sprachsteuerung, die nicht
> anspringt, ist wertlos.
>
> Anmelden (zwei Felder, mehr nicht):
> 👉 https://dialos.org/dialos-mobil-tester-gesucht/
>
> Teilen hilft mir übrigens genauso wie Mitmachen. Ich brauche zwölf.
>
> #Barrierefreiheit #Android #OpenSource #Sprachsteuerung #Inklusion

---

## Fassung 2: die kurze (für Gruppen, Kommentare, Stories)

> **Ich suche 12 Leute mit einem Android-Handy.**
>
> Ich habe eine App gebaut, mit der blinde und motorisch eingeschränkte
> Menschen allein durch Sprechen telefonieren können. Sagen, wen man
> anrufen will – die App ruft an. Ohne Hinsehen, ohne Tippen.
>
> Die Spracherkennung läuft komplett auf dem Handy. Die App hat keine
> Internetberechtigung und kann deshalb gar nichts senden. Kostenlos,
> quelloffen, werbefrei.
>
> Google lässt sie nur in den Play Store, wenn vorher 12 Personen sie 14
> Tage lang testen. Genau die suche ich.
>
> 👉 https://dialos.org/dialos-mobil-tester-gesucht/

---

## Was in beiden Fassungen bewusst so steht

- **Der Nutzen zuerst, die Technik danach.** Wer scrollt, entscheidet in
  der ersten Zeile. „Meine Mutter kann kein Smartphone bedienen" hält an,
  „offline Spracherkennung mit Vosk" nicht.
- **Kein Wort zu viel über Vosk, Kotlin oder Play Console.** Das Publikum
  bei Facebook ist nicht das bei GitHub.
- **Die Bitte ist konkret und klein.** „12 Leute, 14 Tage, Android mit
  SIM" ist beantwortbar. „Testet doch mal meine App" ist es nicht.
- **Der Datenschutz steht als Verkaufsargument da, nicht als Kleingedrucktes.**
  Bei einer App, die dauerhaft zuhört, ist das die erste Sorge – die gehört
  beantwortet, bevor sie gestellt wird.
- **Die Teilen-Bitte am Schluss** steht nur in der langen Fassung. Sie
  wirkt, kostet aber Glaubwürdigkeit, wenn sie zu früh kommt.

## Was NICHT hineingehört

- Kein Versprechen eines Erscheinungsdatums. Wann Google prüft, weiß
  niemand.
- Keine Screenshots aus dem echten Adressbuch. Für das Demo-Video mussten
  Namen nachträglich unkenntlich gemacht werden – hier gleich mit den
  vorbereiteten Screenshots aus `screenshots/` arbeiten.
- Nicht behaupten, die App sei „im Play Store". Sie ist es noch nicht.
