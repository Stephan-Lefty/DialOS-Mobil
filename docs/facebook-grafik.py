#!/usr/bin/env python3
"""Baut die Grafik zum Facebook-Aufruf für DialOS Mobil.

Erzeugt screenshots/facebook/facebook-testeraufruf-1200x630.png.

1200x630 ist das Format, das Facebook (und LinkedIn, Mastodon, WhatsApp)
für geteilte Beiträge erwartet. Wird ein anderes Seitenverhältnis geliefert,
schneidet Facebook eigenmächtig zu - meist quer durch die Schrift.

Warum kein Screenshot als Aufmacher: Die App-Screenshots sind 1200x2670,
also hochkant. Facebook zeigt davon einen schmalen Streifen aus der Mitte.
Deshalb dieses Querformat, in dem der Screenshot verkleinert und gerahmt
rechts sitzt.

Gestaltung bewusst kontraststark (heller Text auf DialOS-Blau, Prüfwerte
unten in der Ausgabe): Ein Aufruf für Menschen mit Sehbehinderung, der
selbst schlecht lesbar ist, wäre eine seltsame Visitenkarte. Der Text steht
zusätzlich im Beitrag selbst - niemand ist darauf angewiesen, die Grafik
lesen zu können.
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

REPO = Path(__file__).resolve().parent.parent
SCREENSHOT = REPO / "screenshots" / "01_startseite_aus.png"
LOGO = REPO / "assets" / "logo.png"
ZIEL = REPO / "screenshots" / "facebook" / "facebook-testeraufruf-1200x630.png"

BREITE, HOEHE = 1200, 630

BLAU = (31, 111, 181)        # dialos_blue
BLAU_DUNKEL = (14, 61, 102)  # dialos_blue_dark
WEISS = (255, 255, 255)
GELB = (255, 224, 0)         # hc_yellow

# Fuer den Fliesstext NICHT dialos_blue_container (#D3E4F3) verwenden: Auf
# dem helleren oberen Ende des Verlaufs kommt das nur auf 4,04:1 und liegt
# damit unter der WCAG-Schwelle von 4,5:1 fuer Fliesstext. Bei einem Aufruf
# fuer Menschen mit Sehbehinderung ist das die falsche Zahl. Dieser Ton
# liegt darueber und wirkt trotzdem nicht wie hartes Weiss.
FLIESSTEXT = (233, 242, 251)

SCHRIFTEN = "/usr/share/fonts/noto"


def schrift(stil, groesse):
    return ImageFont.truetype(f"{SCHRIFTEN}/NotoSans-{stil}.ttf", groesse)


def umbrechen(zeichner, text, font, max_breite):
    """Wortweiser Umbruch anhand der tatsaechlich gemessenen Breite."""
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
    """Relative Leuchtdichte nach WCAG."""
    def kanal(w):
        w /= 255
        return w / 12.92 if w <= 0.03928 else ((w + 0.055) / 1.055) ** 2.4
    r, g, b = (kanal(k) for k in farbe[:3])
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def kontrast(vorne, hinten):
    a, b = sorted((leuchtkraft(vorne), leuchtkraft(hinten)), reverse=True)
    return (a + 0.05) / (b + 0.05)


def hintergrund_bei(y):
    """Farbe des Verlaufs auf Höhe y - Bezugswert für die Kontrastprüfung."""
    anteil = min(max(y / HOEHE, 0.0), 1.0)
    return tuple(
        int(BLAU[i] + (BLAU_DUNKEL[i] - BLAU[i]) * anteil) for i in range(3)
    )


bild = Image.new("RGB", (BREITE, HOEHE), BLAU_DUNKEL)
zeichner = ImageDraw.Draw(bild)

# Farbverlauf von hell oben nach dunkel unten - eine Fläche in einem Ton
# wirkt bei dieser Größe tot.
for y in range(HOEHE):
    zeichner.line([(0, y), (BREITE, y)], fill=hintergrund_bei(y))

# --- rechte Seite: der Screenshot, leicht gerahmt --------------------------
RAND = 48
schuss = Image.open(SCREENSHOT).convert("RGB")
schuss_hoehe = HOEHE - 2 * RAND
schuss_breite = int(schuss.width * schuss_hoehe / schuss.height)
schuss = schuss.resize((schuss_breite, schuss_hoehe), Image.LANCZOS)

schuss_x = BREITE - RAND - schuss_breite
zeichner.rounded_rectangle(
    [schuss_x - 6, RAND - 6, schuss_x + schuss_breite + 6, RAND + schuss_hoehe + 6],
    radius=22, fill=(255, 255, 255),
)
bild.paste(schuss, (schuss_x, RAND))

# --- linke Seite: Text -----------------------------------------------------
text_links = 64
text_breite = schuss_x - text_links - 56

y = 92

kopf_font = schrift("Medium", 27)
zeichner.text((text_links, y), "DIALOS MOBIL", font=kopf_font, fill=GELB)
y += 52

titel_font = schrift("Black", 55)
for zeile in umbrechen(zeichner, "Ich suche 12 Menschen mit einem Android-Handy",
                       titel_font, text_breite):
    zeichner.text((text_links, y), zeile, font=titel_font, fill=WEISS)
    y += 66

y += 26
text_font = schrift("Regular", 29)
absatz = ("Eine App, mit der blinde Menschen allein durch Sprechen "
          "telefonieren. Offline, ohne Internet, kostenlos.")
fliesstext_y = y
for zeile in umbrechen(zeichner, absatz, text_font, text_breite):
    zeichner.text((text_links, y), zeile, font=text_font, fill=FLIESSTEXT)
    y += 40

# Fußzeile unten festgenagelt, nicht am Fließtext hängend - sonst wandert
# sie, sobald der Text eine Zeile mehr braucht.
fuss_font = schrift("Bold", 30)
zeichner.text((text_links, HOEHE - 84), "dialos.org", font=fuss_font, fill=GELB)

ZIEL.parent.mkdir(parents=True, exist_ok=True)
bild.save(ZIEL, "PNG", optimize=True)

print(f"Erzeugt: {ZIEL}")
print(f"  {bild.width}x{bild.height}, {ZIEL.stat().st_size // 1024} KB")
print()
print("Kontrast nach WCAG, gemessen gegen den Verlauf an der Stelle,")
print("an der die Zeile tatsächlich steht (Fließtext ab 4.5:1,")
print("große Schrift ab 3:1):")

# Fußzeile absichtlich zusätzlich gegen die HELLSTE Stelle des Verlaufs
# geprüft: Sie steht zwar unten im Dunklen, aber wer die Grafik später
# umbaut, verschiebt sie womöglich nach oben.
pruefungen = [
    ("Kopfzeile gelb", GELB, 92, 3.0),
    ("Überschrift weiß", WEISS, 144, 3.0),
    ("Fließtext", FLIESSTEXT, fliesstext_y, 4.5),
    ("Fußzeile gelb", GELB, HOEHE - 84, 3.0),
    ("Gelb im ungünstigsten Fall", GELB, 0, 3.0),
]

alles_gut = True
for name, vorne, hoehe, schwelle in pruefungen:
    wert = kontrast(vorne, hintergrund_bei(hoehe))
    ok = wert >= schwelle
    alles_gut = alles_gut and ok
    print(f"  {name:<28} {wert:5.2f}:1  (nötig {schwelle}:1)  "
          f"{'ok' if ok else 'ZU SCHWACH'}")

if not alles_gut:
    raise SystemExit("\nMindestens ein Wert liegt unter der Schwelle. Eine "
                     "Grafik, die für Menschen mit Sehbehinderung wirbt, "
                     "sollte selbst lesbar sein.")
