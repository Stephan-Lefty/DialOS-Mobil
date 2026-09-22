#!/usr/bin/env python3
"""Eine Hochformat-Grafik (1080x1920) mit Adresse und QR-Code.

Gedacht für den WhatsApp-Status und zum Weiterleiten. Der Unterschied zu
den Story-Grafiken (instagram-stories-grafik.py) ist der Zweck: Dort trägt
der Begleittext den Link, hier trägt ihn das Bild selbst.

Das ist nötig, weil ein weitergeleitetes Bild seine Unterschrift verliert.
Wer den Status sieht, hat den anklickbaren Link darunter; wer das Bild von
jemand anderem geschickt bekommt, hat nur das Bild. Deshalb steht die
Adresse hier groß darauf - und dazu ein QR-Code, weil Abtippen die
unwahrscheinlichere Handlung ist.

Wichtig und nicht zu vergessen: Für blinde Menschen ist diese Grafik leer.
Ein QR-Code lässt sich nicht ertasten, eine Adresse im Bild nicht vorlesen.
Die Grafik wirbt bei Sehenden, die weiterleiten können - der Begleittext
muss weiterhin alles Wesentliche als Text enthalten. Der Alternativtext am
Ende der Ausgabe gehört mit in den Beitrag.

    python3 docs/whatsapp-grafik.py [anzahl-fehlender-tester]

Der QR-Code braucht die Bibliothek `qrcode`. Fehlt sie, entsteht die
Grafik trotzdem - dann ohne Code, aber mit größerer Adresse. Auf Manjaro:

    sudo pacman -S python-qrcode

Jede Textzeile wird nach WCAG gegen den Verlauf an genau ihrer Stelle
geprüft; fällt ein Wert durch, bricht das Skript ab. Eine Grafik, die für
Menschen mit Sehbehinderung wirbt, muss selbst lesbar sein.
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

try:
    import qrcode
    QR_MOEGLICH = True
except ImportError:
    QR_MOEGLICH = False

REPO = Path(__file__).resolve().parent.parent
ZIELORDNER = REPO / "screenshots" / "facebook"

FEHLEND = int(sys.argv[1]) if len(sys.argv) > 1 else 2

# Aufgedruckt wird nur die Startseite, nicht der vollständige Pfad.
#
# Beim ersten Entwurf stand hier "dialos.org/mobil-test" - kurz, gut zu
# merken, gut abzutippen und **frei erfunden**. Diese Seite gibt es nicht;
# wer sie eingetippt hätte, wäre im Nichts gelandet, und zwar ausgerechnet
# als Belohnung dafür, sich die Mühe gemacht zu haben.
#
# Solange es keine echte Kurzadresse gibt, steht hier die Startseite: Sie
# existiert, ist kurz genug zum Abtippen, und von dort führt der Weg
# weiter. Der QR-Code zeigt dagegen direkt auf die Anmeldeseite - er wird
# gescannt, nicht gelesen, da schadet die Länge nicht.
#
# Wer später eine Weiterleitung wie /mobil-test auf dialos.org einrichtet,
# kann ADRESSE entsprechend ändern - aber erst dann.
ADRESSE = "dialos.org"
ZIEL_URL = "https://dialos.org/dialos-mobil-tester-gesucht/"

BREITE, HOEHE = 1080, 1920
OBEN, UNTEN = 280, 1620
RAND = 96
TEXTBREITE = BREITE - 2 * RAND

BLAU = (31, 111, 181)
BLAU_DUNKEL = (14, 61, 102)
WEISS = (255, 255, 255)
GELB = (255, 224, 0)
FLIESSTEXT = (233, 242, 251)

SCHRIFTEN = "/usr/share/fonts/noto"


def schrift(stil, groesse):
    return ImageFont.truetype(f"{SCHRIFTEN}/NotoSans-{stil}.ttf", groesse)


def umbrechen(zeichner, text, font, max_breite):
    zeilen, aktuell = [], ""
    for wort in text.split():
        versuch = f"{aktuell} {wort}".strip()
        if zeichner.textlength(versuch, font=font) <= max_breite:
            aktuell = versuch
        else:
            if aktuell:
                zeilen.append(aktuell)
            aktuell = wort
    if aktuell:
        zeilen.append(aktuell)
    return zeilen


def leuchtkraft(farbe):
    def kanal(w):
        w /= 255
        return w / 12.92 if w <= 0.03928 else ((w + 0.055) / 1.055) ** 2.4
    r, g, b = (kanal(k) for k in farbe[:3])
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def kontrast(vorne, hinten):
    a, b = sorted((leuchtkraft(vorne), leuchtkraft(hinten)), reverse=True)
    return (a + 0.05) / (b + 0.05)


def hintergrund_bei(y):
    anteil = min(max(y / HOEHE, 0.0), 1.0)
    return tuple(
        int(BLAU[i] + (BLAU_DUNKEL[i] - BLAU[i]) * anteil) for i in range(3)
    )


pruefungen = []


def merke(name, farbe, y, schwelle):
    pruefungen.append((name, farbe, y, schwelle))


# ---------------------------------------------------------------------------
# Die Grafik
# ---------------------------------------------------------------------------
bild = Image.new("RGB", (BREITE, HOEHE), BLAU_DUNKEL)
z = ImageDraw.Draw(bild)
for y in range(HOEHE):
    z.line([(0, y), (BREITE, y)], fill=hintergrund_bei(y))

z.text((RAND, OBEN), "DIALOS MOBIL", font=schrift("Medium", 42), fill=GELB)
merke("Kopfzeile", GELB, OBEN, 3.0)

y = 420
titel_font = schrift("Black", 92)
for zeile in umbrechen(z, f"Noch {FEHLEND} Testpersonen gesucht",
                       titel_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=titel_font, fill=WEISS)
    merke("Überschrift", WEISS, y, 3.0)
    y += 112

y += 40
text_font = schrift("Regular", 44)
absatz = ("Telefonieren, ohne den Bildschirm zu sehen: Namen sagen, "
          "bestätigen, fertig. Kostenlos, quelloffen, ohne Konto und "
          "ohne Werbung.")
for zeile in umbrechen(z, absatz, text_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=text_font, fill=FLIESSTEXT)
    merke("Fließtext", FLIESSTEXT, y, 4.5)
    y += 60

# ---------------------------------------------------------------------------
# QR-Code und Adresse
#
# Der Code steht links, die Adresse rechts daneben: Wer scannen kann,
# scannt; wer das Bild nur ansieht, tippt ab. Weiß hinterlegt, weil
# QR-Codes auf farbigem Grund von manchen Kameras nicht erkannt werden -
# der Rahmen ist keine Zierde, sondern Voraussetzung.
# ---------------------------------------------------------------------------
y += 70
qr_groesse = 420

if QR_MOEGLICH:
    code = qrcode.QRCode(box_size=10, border=2)
    code.add_data(ZIEL_URL)
    code.make(fit=True)
    qr_bild = code.make_image(fill_color="black", back_color="white")
    qr_bild = qr_bild.convert("RGB").resize((qr_groesse, qr_groesse),
                                            Image.NEAREST)
    bild.paste(qr_bild, (RAND, y))

    text_x = RAND + qr_groesse + 48
    z.text((text_x, y + 90), "Scannen", font=schrift("Bold", 52), fill=WEISS)
    merke("Scannen", WEISS, y + 90, 3.0)
    z.text((text_x, y + 170), "oder", font=schrift("Regular", 44),
           fill=FLIESSTEXT)
    merke("oder", FLIESSTEXT, y + 170, 4.5)
    z.text((text_x, y + 240), "eintippen:", font=schrift("Bold", 52),
           fill=WEISS)
    merke("eintippen", WEISS, y + 240, 3.0)
    y += qr_groesse + 60
else:
    print("Hinweis: Bibliothek 'qrcode' fehlt - Grafik entsteht ohne Code.")
    print("         Auf Manjaro:  sudo pacman -S python-qrcode")
    y += 20

adresse_font = schrift("Black", 62)
z.text((RAND, y), ADRESSE, font=adresse_font, fill=GELB)
merke("Adresse", GELB, y, 3.0)

# ---------------------------------------------------------------------------
# Kontrastprüfung
# ---------------------------------------------------------------------------
print()
print("Kontraste (WCAG, gegen den Verlauf an der jeweiligen Höhe):")
alles_gut = True
for name, farbe, hoehe, schwelle in pruefungen:
    wert = kontrast(farbe, hintergrund_bei(hoehe))
    ok = wert >= schwelle
    alles_gut = alles_gut and ok
    print(f"  {name:<14} y={hoehe:<5} {wert:5.2f}:1  (nötig {schwelle}:1)  "
          f"{'ok' if ok else 'ZU SCHWACH'}")

if not alles_gut:
    raise SystemExit(
        "\nMindestens ein Wert liegt unter der Schwelle. Eine Grafik, die "
        "für Menschen mit Sehbehinderung wirbt, sollte selbst lesbar sein."
    )

ZIELORDNER.mkdir(parents=True, exist_ok=True)
ziel = ZIELORDNER / "dialos-mobil-whatsapp-link-1080x1920.png"
bild.save(ziel, "PNG", optimize=True)
print()
print(f"Erzeugt: {ziel.relative_to(REPO)}  "
      f"({bild.width}x{bild.height}, {ziel.stat().st_size // 1024} KB)")

print()
print("Alternativtext für den Beitrag:")
print(f"   Blaue Hochformat-Grafik. Überschrift: Noch {FEHLEND} Testpersonen "
      "gesucht. Darunter: Telefonieren, ohne den Bildschirm zu sehen - "
      "Namen sagen, bestätigen, fertig. Kostenlos, quelloffen, ohne Konto "
      "und ohne Werbung. Unten ein QR-Code und die Adresse "
      f"{ADRESSE}.")
print()
print("Der QR-Code zeigt auf:")
print(f"   {ZIEL_URL}")
