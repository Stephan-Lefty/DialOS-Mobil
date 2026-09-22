#!/usr/bin/env python3
"""Drei Hochformat-Grafiken (1080x1920) für Instagram- und Facebook-Stories.

9:16 ist das Story- und Reels-Format bei Instagram und Facebook. Die
quadratischen Grafiken (facebook-grafik-runde2.py) passen in den Feed, in
einer Story werden sie aber oben und unten mit leeren Balken gerahmt und
wirken verloren. Deshalb dieselben drei Botschaften noch einmal hochkant.

  1. dialos-mobil-story-noch-gesucht-1080x1920.png
  2. dialos-mobil-story-rueckmeldungen-1080x1920.png
  3. dialos-mobil-story-voraussetzungen-1080x1920.png

Sichere Zone: Instagram und Facebook legen oben (Profilzeile) und unten
(Antwortleiste) je einen Streifen über die Story. Alles Wichtige liegt hier
zwischen y=280 und y=1620, damit nichts verdeckt wird.

Wie viele Tester noch fehlen, kommt als Argument - die Zahl ändert sich:
    python3 docs/instagram-stories-grafik.py 3

Jede Zeile wird nach WCAG gegen den Verlauf an genau ihrer Stelle geprüft;
fällt ein Wert durch, bricht das Skript ab. Eine Grafik, die für Menschen
mit Sehbehinderung wirbt, muss selbst lesbar sein. Die Alternativtexte
stehen unten in der Ausgabe und gehören mit in den Beitrag.
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

REPO = Path(__file__).resolve().parent.parent

FEHLEND = int(sys.argv[1]) if len(sys.argv) > 1 else 3
FEHLEND_WORT = {
    1: "Ein Mensch fehlt noch", 2: "Menschen fehlen noch",
}.get(FEHLEND, "Menschen fehlen noch")
SCREENSHOT = REPO / "screenshots" / "01_startseite_aus.png"
ZIELORDNER = REPO / "screenshots" / "facebook"

BREITE, HOEHE = 1080, 1920

# Sichere Zone - außerhalb davon liegt die Bedienoberfläche der Story.
OBEN = 280
UNTEN = 1620

BLAU = (31, 111, 181)         # dialos_blue
BLAU_DUNKEL = (14, 61, 102)   # dialos_blue_dark
WEISS = (255, 255, 255)
GELB = (255, 224, 0)          # hc_yellow
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


def neues_bild():
    bild = Image.new("RGB", (BREITE, HOEHE), BLAU_DUNKEL)
    zeichner = ImageDraw.Draw(bild)
    for y in range(HOEHE):
        zeichner.line([(0, y), (BREITE, y)], fill=hintergrund_bei(y))
    return bild, zeichner


RAND = 96
TEXTBREITE = BREITE - 2 * RAND


def kopfzeile(zeichner):
    zeichner.text((RAND, OBEN), "DIALOS MOBIL", font=schrift("Medium", 42),
                  fill=GELB)


def fusszeile(zeichner, text="Mitmachen: dialos.org"):
    y = UNTEN - 60
    zeichner.text((RAND, y), text, font=schrift("Bold", 46), fill=GELB)
    return y


def speichern(bild, name):
    ZIELORDNER.mkdir(parents=True, exist_ok=True)
    ziel = ZIELORDNER / name
    bild.save(ziel, "PNG", optimize=True)
    print(f"Erzeugt: {ziel.relative_to(REPO)}  "
          f"({bild.width}x{bild.height}, {ziel.stat().st_size // 1024} KB)")
    return ziel


pruefungen = []


def merke(grafik, name, farbe, y, schwelle):
    pruefungen.append((grafik, name, farbe, y, schwelle))


# ---------------------------------------------------------------------------
# 1. Wie viele noch fehlen
# ---------------------------------------------------------------------------
bild, z = neues_bild()
kopfzeile(z)
merke("1 noch gesucht", "Kopfzeile", GELB, OBEN, 3.0)

y = 470
zahl_font = schrift("Black", 460)
z.text((RAND - 12, y), str(FEHLEND), font=zahl_font, fill=GELB)
merke("1 noch gesucht", "Riesenzahl", GELB, y + 230, 3.0)

y += 560
titel_font = schrift("Black", 92)
for zeile in umbrechen(z, FEHLEND_WORT, titel_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=titel_font, fill=WEISS)
    merke("1 noch gesucht", "Überschrift", WEISS, y, 3.0)
    y += 110

y += 34
text_font = schrift("Regular", 46)
absatz = ("Danach kann DialOS Mobil in den Play Store: eine App, mit der "
          "blinde Menschen allein durch Sprechen telefonieren.")
for zeile in umbrechen(z, absatz, text_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=text_font, fill=FLIESSTEXT)
    merke("1 noch gesucht", "Fließtext", FLIESSTEXT, y, 4.5)
    y += 62

fusszeile(z)
merke("1 noch gesucht", "Fußzeile", GELB, UNTEN - 60, 3.0)
speichern(bild, "dialos-mobil-story-noch-gesucht-1080x1920.png")

# ---------------------------------------------------------------------------
# 2. Was aus den Rückmeldungen wird
#
# Die Zahlen sind gezählt, nicht geschätzt: 30 Stichpunkte im
# Änderungsprotokoll zwischen 0.6.3 und 0.6.15, alle aus dem Test
# entstanden. Wer sie ändert, zählt vorher nach:
#     sed -n '/^### 0\.6\.15/,/^### 0\.6\.2 /p' README.md | grep -c "^- \*\*"
#
# Die Botschaft ist bewusst nicht "die App hat viele Fehler", sondern
# "deine Meldung wird etwas". Das ist die Frage, die jemand vor dem
# Mitmachen wirklich hat - vor allem bei einer Zielgruppe, der schon oft
# Beteiligung versprochen und dann nichts geliefert wurde.
# ---------------------------------------------------------------------------
bild, z = neues_bild()
kopfzeile(z)
merke("2 Rückmeldungen", "Kopfzeile", GELB, OBEN, 3.0)

y = 470
titel_font = schrift("Black", 100)
for zeile in umbrechen(z, "30 Fehler behoben. Meist am selben Tag.",
                       titel_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=titel_font, fill=WEISS)
    merke("2 Rückmeldungen", "Überschrift", WEISS, y, 3.0)
    y += 122

y += 50
text_font = schrift("Regular", 46)
absatz = ("Alles davon kam aus dem Test. Drei Fehler waren so schwer, "
          "dass die App vorher kaum zu gebrauchen war - gefunden hat sie "
          "nicht mein Testen, sondern der Alltag anderer.")
for zeile in umbrechen(z, absatz, text_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=text_font, fill=FLIESSTEXT)
    merke("2 Rückmeldungen", "Fließtext", FLIESSTEXT, y, 4.5)
    y += 62

y += 56
schluss_font = schrift("Bold", 56)
for zeile in umbrechen(z, "Deine Meldung wird etwas.", schluss_font,
                       TEXTBREITE):
    z.text((RAND, y), zeile, font=schluss_font, fill=GELB)
    merke("2 Rückmeldungen", "Schlusszeile", GELB, y, 3.0)
    y += 70

fusszeile(z)
merke("2 Rückmeldungen", "Fußzeile", GELB, UNTEN - 60, 3.0)
speichern(bild, "dialos-mobil-story-rueckmeldungen-1080x1920.png")

# ---------------------------------------------------------------------------
# 3. Voraussetzungen, mit Screenshot unten
# ---------------------------------------------------------------------------
bild, z = neues_bild()
kopfzeile(z)
merke("3 Voraussetzungen", "Kopfzeile", GELB, OBEN, 3.0)

y = 440
titel_font = schrift("Black", 88)
for zeile in umbrechen(z, "Was du brauchst", titel_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=titel_font, fill=WEISS)
    merke("3 Voraussetzungen", "Überschrift", WEISS, y, 3.0)
    y += 104

y += 30
punkt_font = schrift("Regular", 44)
for punkt in ("Ein Android-Handy mit SIM-Karte",
              "Ein Google-Konto darauf",
              "14 Tage die App installiert lassen",
              "Deutsch sprechen"):
    z.text((RAND, y), "•", font=punkt_font, fill=GELB)
    for zeile in umbrechen(z, punkt, punkt_font, TEXTBREITE - 52):
        z.text((RAND + 52, y), zeile, font=punkt_font, fill=FLIESSTEXT)
        merke("3 Voraussetzungen", "Aufzählung", FLIESSTEXT, y, 4.5)
        y += 58
    y += 22

# Screenshot mittig unter der Aufzählung, gerahmt. Höhe so gewählt, dass er
# über der Antwortleiste (UNTEN) bleibt.
schuss = Image.open(SCREENSHOT).convert("RGB")
schuss_hoehe = min(720, UNTEN - y - 30)
schuss_breite = int(schuss.width * schuss_hoehe / schuss.height)
schuss = schuss.resize((schuss_breite, schuss_hoehe), Image.LANCZOS)
schuss_x = (BREITE - schuss_breite) // 2
schuss_y = y + 10
z.rounded_rectangle(
    [schuss_x - 7, schuss_y - 7,
     schuss_x + schuss_breite + 7, schuss_y + schuss_hoehe + 7],
    radius=22, fill=WEISS,
)
bild.paste(schuss, (schuss_x, schuss_y))

speichern(bild, "dialos-mobil-story-voraussetzungen-1080x1920.png")

# ---------------------------------------------------------------------------
print()
print("Kontrast nach WCAG, gemessen gegen den Verlauf an der Stelle,")
print("an der die Zeile tatsächlich steht (Fließtext ab 4.5:1,")
print("große Schrift ab 3:1):")

alles_gut = True
letzte_grafik = None
for grafik, name, farbe, y, schwelle in pruefungen:
    if grafik != letzte_grafik:
        print(f"\n  {grafik}")
        letzte_grafik = grafik
    wert = kontrast(farbe, hintergrund_bei(y))
    ok = wert >= schwelle
    alles_gut = alles_gut and ok
    print(f"    {name:<16} y={y:<5} {wert:5.2f}:1  (nötig {schwelle}:1)  "
          f"{'ok' if ok else 'ZU SCHWACH'}")

if not alles_gut:
    raise SystemExit(
        "\nMindestens ein Wert liegt unter der Schwelle. Eine Grafik, die "
        "für Menschen mit Sehbehinderung wirbt, sollte selbst lesbar sein."
    )

print()
print("Alternativtexte für den Beitrag:")
print()
print("1) dialos-mobil-story-noch-gesucht-1080x1920.png")
print(f"   Blaue Hochformat-Grafik mit einer großen gelben {FEHLEND}. Text: "
      f"{FEHLEND_WORT}. Danach kann DialOS Mobil in den Play Store - eine "
      "App, mit der blinde Menschen allein durch Sprechen telefonieren.")
print()
print("2) dialos-mobil-story-rueckmeldungen-1080x1920.png")
print("   Blaue Hochformat-Grafik mit der Überschrift: 30 Fehler behoben, "
      "meist am selben Tag. Darunter: Alles davon kam aus dem Test, drei "
      "Fehler waren so schwer, dass die App vorher kaum zu gebrauchen war. "
      "Schlusszeile: Deine Meldung wird etwas.")
print()
print("3) dialos-mobil-story-voraussetzungen-1080x1920.png")
print("   Blaue Hochformat-Grafik mit der Überschrift: Was du brauchst. "
      "Aufgezählt: ein Android-Handy mit SIM-Karte, ein Google-Konto darauf, "
      "vierzehn Tage die App installiert lassen, Deutsch sprechen. Darunter "
      "der Startbildschirm der App mit einem großen blauen Knopf.")
