# Google Play Console: Data-Safety-Formular – Ausfüllhilfe

Der Fragebogen unter *App-Inhalte → Datensicherheit*. Für DialOS Mobil ist
er ungewöhnlich einfach, weil die App keine Internetberechtigung hat und
technisch nichts senden kann.

## Grundfragen

| Frage | Antwort | Begründung |
|---|---|---|
| Erhebt oder teilt die App Nutzerdaten? | **Nein** | Kein Datentyp verlässt das Gerät. Verarbeitung ausschließlich lokal. |
| Werden Daten bei der Übertragung verschlüsselt? | *entfällt* | Es findet keine Übertragung statt. |
| Können Nutzer die Löschung ihrer Daten beantragen? | *entfällt* | Es werden keine Daten beim Anbieter gespeichert. |

**Das ist der ganze Fragebogen.** Wird die erste Frage mit „Nein"
beantwortet, entfallen alle Datentyp-Abschnitte.

## Warum „Nein" hier korrekt ist

Google unterscheidet zwischen *Erhebung* (Daten verlassen das Gerät) und
*Zugriff* (die App liest etwas auf dem Gerät). Zugriff allein ist keine
Erhebung.

DialOS Mobil greift auf Mikrofon, Kontakte und Telefonstatus zu, sendet
aber nichts. Nachprüfbar: **die App fordert die Berechtigung `INTERNET`
nicht an** und kann deshalb keine Verbindung öffnen. Das lässt sich am
fertigen Paket belegen:

```bash
aapt2 dump permissions app-release.aab | grep INTERNET   # keine Treffer
```

Falls Google nachfragt, ist genau das die Antwort: keine
Internetberechtigung, quelloffener Code zum Nachlesen.

## Zusätzliche Erklärungen an anderer Stelle

Diese gehören **nicht** ins Data-Safety-Formular, werden aber im selben
Zug abgefragt:

### Berechtigungserklärung: Anrufen (`CALL_PHONE`)

> Die App ist ein Sprachwähler für blinde und motorisch eingeschränkte
> Menschen. Der Nutzer sagt den Namen eines Kontakts, die App liest ihn zur
> Bestätigung vor und baut den Anruf auf. Ohne die Berechtigung zu
> telefonieren gibt es keine App – das Anrufen ist die einzige Funktion.
> Es wird kein Anrufprotokoll geführt und keines gelesen.

### Berechtigungserklärung: Vordergrunddienst Mikrofon

> Die App hört dauerhaft auf das Aktivierungswort „Sprachsteuerung
> starten", damit sie ohne Berührung des Bildschirms ansprechbar ist – der
> Zweck der App für Menschen, die den Bildschirm nicht bedienen können. Die
> Erkennung läuft offline auf dem Gerät (Vosk); es wird kein Ton
> gespeichert und keiner übertragen. Eine dauerhafte Benachrichtigung zeigt
> an, dass zugehört wird.

Google verlangt hierzu ein **kurzes Video**, das den Ablauf zeigt.
Vorhanden und einzutragen:

**https://dialos.org/wp-content/uploads/2026/08/DialOS-Mobil-Demo.mp4**

### Berechtigungserklärung: Akku-Optimierung ausnehmen

> Wird Android erlaubt, den Dienst schlafen zu legen, hört die App das
> Aktivierungswort nicht mehr – für einen blinden Nutzer fällt die App dann
> ohne erkennbaren Grund aus. Die Ausnahme ist optional und wird vom Nutzer
> ausdrücklich erteilt; die App funktioniert auch ohne sie, dann aber nur
> so lange, wie Android den Dienst am Leben lässt.

## Werbung und Inhalte

| Frage | Antwort |
|---|---|
| Enthält die App Werbung? | Nein |
| In-App-Käufe? | Nein |
| Zielgruppe | Erwachsene (18+), keine Ausrichtung auf Kinder |
| Inhaltseinstufung | Fragebogen ausfüllen; ergibt „Ohne Altersbeschränkung" |
| Datensicherheits-Bereich vollständig? | Ja, siehe oben |
