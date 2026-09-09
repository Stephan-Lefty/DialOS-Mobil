# Zweiter Testeraufruf – Text und Grafiken

Stand: 08.09.2026. Der geschlossene Test läuft, es sind **8 von 12** Testern
angemeldet. Gebraucht werden nicht nur die fehlenden vier, sondern ein
Puffer: Zwölf Eingetragene ohne Reserve bedeuten, dass ein einziger Absprung
die Zählung reißt und alles von vorn beginnt.

**Gepostet wird von Stephan selbst.** Hier stehen nur Text und Grafiken.

Der Unterschied zum Aufruf vom August: Damals gab es nur ein Versprechen.
Jetzt gibt es ein Ergebnis – drei Rückmeldungen, neun behobene Fehler. Das
ist der stärkere Aufhänger, deshalb steht er in allen Fassungen vorn.

## Grafiken

Erzeugt von [`facebook-grafik-runde2.py`](facebook-grafik-runde2.py),
1080 × 1080 (das native Maß von Facebook und Instagram):

| Datei | Aussage |
|---|---|
| `screenshots/facebook/dialos-mobil-noch-vier-1080.png` | Die konkrete Zahl |
| `screenshots/facebook/dialos-mobil-neun-fehler-1080.png` | Was der Test gebracht hat |
| `screenshots/facebook/dialos-mobil-voraussetzungen-1080.png` | Was man braucht |

Alle drei sind nach WCAG gegen den Farbverlauf geprüft; das Skript bricht
ab, wenn ein Wert unter der Schwelle liegt. Die **Alternativtexte** gibt das
Skript beim Lauf aus – sie gehören mit in den Beitrag. Ein Bild ohne
Alternativtext ist bei diesem Thema ein Eigentor.

Empfehlung: Grafik 2 („neun Fehler") als Aufmacher, weil sie das Ergebnis
zeigt statt einer Bitte. Grafik 1 und 3 als weitere Bilder im selben
Beitrag.

## Lange Fassung (Facebook)

```
Vor drei Wochen habe ich hier zwölf Menschen gesucht, die eine App testen.
Heute weiß ich, warum das nötig war.

DialOS Mobil ist ein Telefon für Menschen, die ein Telefon nicht bedienen
können. Wer blind ist oder die Hände nicht ruhig genug führt, scheitert am
Touchscreen - nicht am Telefonieren. Die App dreht das um: Man sagt, wen man
anrufen möchte, und sie ruft an. Kein Tippen, kein Zielen, kein Hinsehen.
Die Spracherkennung läuft komplett auf dem Handy, ohne Internet.

Drei Testerinnen und Tester haben inzwischen geschrieben. Sie haben in
jeweils wenigen Minuten neun Fehler gefunden, die ich in Wochen eigenen
Testens nicht bemerkt hatte.

Einer davon traf jeden Nutzer bei jedem Anruf: Die App verstand "ja bitte"
nicht. "Ja" kannte sie, "bitte" auch - "ja bitte" nicht. Mir ist das nie
passiert, weil ich beim Testen spreche wie ein Entwickler: knapp und in der
Form, die ich selbst vorgesehen habe.

Ein anderer meldete, beim Diktieren einer Telefonnummer verschwänden
einzelne Ziffern. Die Erkennung war unschuldig: Die App las nach jedem Block
die ganze bisherige Nummer vor und schaltete dabei ihr eigenes Mikrofon ab.
Wer flüssig weiterspricht, redet in dieses Loch hinein.

Genau dafür sind Tester da. Alle neun Fehler sind behoben.

Jetzt fehlen mir noch vier Menschen.

Google verlangt, bevor eine neue App in den Play Store darf, einen
geschlossenen Test mit mindestens zwölf Personen über vierzehn Tage. Acht
sind angemeldet. Ohne die letzten vier bleibt die App liegen, so fertig sie
auch ist.

Was du brauchst:
- Ein Android-Handy mit SIM-Karte (ohne Telefonie kann die App nichts tun)
- Ein Google-Konto darauf
- Die Bereitschaft, die App vierzehn Tage installiert zu lassen
- Deutsch - die Sprachbedienung gibt es vorerst nur auf Deutsch

Du musst nicht blind sein und nichts von Technik verstehen. Im Gegenteil:
Wer die App zum ersten Mal in die Hand nimmt, findet genau die Dinge, die
mir längst nicht mehr auffallen.

Anmelden mit Name und der Mailadresse deines Google-Kontos:
https://dialos.org/dialos-mobil-tester-gesucht/

Was die drei bisher gefunden haben, habe ich aufgeschrieben:
https://dialos.org/dialos-mobil-die-app-die-sich-selbst-taub-machte/

Die App ist kostenlos, werbefrei und quelloffen. Sie sammelt nichts - sie
hat nicht einmal eine Internetberechtigung.
```

## Kurze Fassung (Instagram, Mastodon, geteilte Beiträge)

```
Drei Testerinnen und Tester, neun gefundene Fehler - einer davon traf jeden
Nutzer bei jedem Anruf: Die App verstand "ja bitte" nicht.

Genau dafür sind Tester da. Alles behoben.

Jetzt fehlen mir noch vier Menschen, damit DialOS Mobil in den Play Store
darf: eine App, mit der blinde Menschen allein durch Sprechen telefonieren.
Offline, kostenlos, werbefrei.

Du brauchst ein Android-Handy mit SIM-Karte, ein Google-Konto darauf und
vierzehn Tage Geduld. Technikkenntnisse brauchst du nicht.

https://dialos.org/dialos-mobil-tester-gesucht/
```

## Sehr kurze Fassung (Kommentar, Weiterleitung, WhatsApp)

```
Mir fehlen noch vier Testerinnen oder Tester für DialOS Mobil - eine App,
mit der blinde Menschen per Sprache telefonieren. Android-Handy mit
SIM-Karte genügt, vierzehn Tage lang installiert lassen. Ohne die letzten
vier darf die App nicht in den Play Store.
https://dialos.org/dialos-mobil-tester-gesucht/
```

## Hinweise zum Posten

**Die Zahl aktuell halten.** In allen Fassungen steht „vier". Melden sich
zwei, sind es zwei – ein Aufruf, der eine überholte Zahl nennt, wirkt
unaufmerksam. Der Stand steht im Play-Console-Dashboard unter „Zugriff auf
die Produktionsversion beantragen".

**Alternativtexte nicht vergessen.** Sie stehen in der Ausgabe des
Grafik-Skripts. Bei einem Aufruf, der sich an blinde Menschen richtet und
von ihnen weitergetragen werden soll, ist ein Bild ohne Alternativtext
schwer zu erklären.

**Mastodon lohnt einen eigenen Versuch.** Die Barrierefreiheits- und
Open-Source-Leute sind dort deutlich aufgeschlossener als auf Facebook, und
Alternativtexte sind dort Selbstverständlichkeit. Das stand schon im August
als Möglichkeit im Raum und wurde nicht verfolgt.

**Wer sich meldet, ist noch kein Tester.** Auf jede Anmeldung muss die
Adresse in die Testerliste der Play Console, und die Person muss zusätzlich
den Opt-in-Link öffnen und „Tester werden" antippen. Das ist die Stelle, an
der zuletzt vier Leute hängengeblieben sind.
