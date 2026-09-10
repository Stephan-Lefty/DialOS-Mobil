#!/usr/bin/env python3
"""Drei quadratische Grafiken (1080x1080) für den zweiten Testeraufruf.

Quadratisch, weil Facebook und Instagram damit am wenigsten anstellen -
jedes andere Seitenverhältnis wird dort beschnitten. 1080 px ist das native
Maß beider Dienste; größer zu rendern bringt nichts, weil sie ohnehin
darauf herunterrechnen.

  1. dialos-mobil-noch-gesucht-1080.png
     Die konkrete Zahl. "Es fehlen noch vier" ist eine Bitte, die man
     erfüllen kann - "bitte helft mir" nicht.

  2. dialos-mobil-neun-fehler-1080.png
     Was der Test bisher gebracht hat. Der beste Grund mitzumachen ist der
     Beweis, dass Mitmachen etwas bewirkt.

  3. dialos-mobil-voraussetzungen-1080.png
     Was man braucht. Beantwortet die Rückfragen, bevor sie kommen.

Zur Schriftgröße: Die kleinste Schrift liegt bei 34 px auf 1080 px Breite.
Das ist für eine Grafik viel und für dieses Publikum das Mindeste. Wer
diese Meldung liest, sieht möglicherweise schlecht - eine Grafik in
Agenturgröße wäre hier eine Unhöflichkeit.

Jede Grafik wird nach WCAG gegen den Verlauf an genau der Stelle geprüft,
an der die Zeile steht. Fällt ein Wert durch, bricht das Skript ab: Eine
Grafik, die für Menschen mit Sehbehinderung wirbt, muss selbst lesbar sein.
Genau diese Prüfung hat am 20.08.2026 einen zu blassen Fließtext gefunden.

Die Alternativtexte stehen unten in der Ausgabe und gehören mit in den
Beitrag - ein Bild ohne Alternativtext ist bei diesem Thema ein Eigentor.
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

REPO = Path(__file__).resolve().parent.parent

# Wie viele Tester noch fehlen. Als Argument, weil sich die Zahl fast
# täglich ändert und ein Aufruf mit veralteter Zahl unaufmerksam wirkt:
#     python3 docs/facebook-grafik-runde2.py 3
FEHLEND = int(sys.argv[1]) if len(sys.argv) > 1 else 3
FEHLEND_WORT = {
    1: "Ein Mensch fehlt noch", 2: "Menschen fehlen noch",
}.get(FEHLEND, "Menschen fehlen noch")
SCREENSHOT = REPO / "screenshots" / "01_startseite_aus.png"
ZIELORDNER = REPO / "screenshots" / "facebook"

KANTE = 1080

BLAU = (31, 111, 181)         # dialos_blue
BLAU_DUNKEL = (14, 61, 102)   # dialos_blue_dark
WEISS = (255, 255, 255)
GELB = (255, 224, 0)          # hc_yellow
# Aufgehellt gegenüber dem Logo-Blassblau: Der ursprüngliche Wert lag beim
# Fließtext unter 4.5:1 - gefunden von der Prüfung unten, nicht im Auge.
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
    anteil = min(max(y / KANTE, 0.0), 1.0)
    return tuple(
        int(BLAU[i] + (BLAU_DUNKEL[i] - BLAU[i]) * anteil) for i in range(3)
    )


def neues_bild():
    bild = Image.new("RGB", (KANTE, KANTE), BLAU_DUNKEL)
    zeichner = ImageDraw.Draw(bild)
    # Verlauf von hell oben nach dunkel unten - eine Fläche in einem Ton
    # wirkt bei dieser Größe tot.
    for y in range(KANTE):
        zeichner.line([(0, y), (KANTE, y)], fill=hintergrund_bei(y))
    return bild, zeichner


RAND = 76
TEXTBREITE = KANTE - 2 * RAND


def kopfzeile(zeichner):
    zeichner.text((RAND, 68), "DIALOS MOBIL", font=schrift("Medium", 34),
                  fill=GELB)
    return 68


def fusszeile(zeichner, text="dialos.org"):
    y = KANTE - 96
    zeichner.text((RAND, y), text, font=schrift("Bold", 38), fill=GELB)
    return y


def speichern(bild, name):
    ZIELORDNER.mkdir(parents=True, exist_ok=True)
    ziel = ZIELORDNER / name
    bild.save(ziel, "PNG", optimize=True)
    print(f"Erzeugt: {ziel.relative_to(REPO)}  "
          f"({bild.width}x{bild.height}, {ziel.stat().st_size // 1024} KB)")
    return ziel


# Alle Prüfungen sammeln und am Ende gemeinsam auswerten, damit man bei
# einem Fehlschlag trotzdem sieht, wie es um die anderen steht.
pruefungen = []


def merke(grafik, name, farbe, y, schwelle):
    pruefungen.append((grafik, name, farbe, y, schwelle))


# ---------------------------------------------------------------------------
# 1. Wie viele noch fehlen
# ---------------------------------------------------------------------------
bild, z = neues_bild()
kopfzeile(z)
merke("1 noch gesucht", "Kopfzeile", GELB, 68, 3.0)

y = 190
zahl_font = schrift("Black", 300)
z.text((RAND, y), str(FEHLEND), font=zahl_font, fill=GELB)
merke("1 noch gesucht", "Riesenzahl", GELB, y + 150, 3.0)

y += 330
titel_font = schrift("Black", 76)
for zeile in umbrechen(z, FEHLEND_WORT, titel_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=titel_font, fill=WEISS)
    merke("1 noch gesucht", "Überschrift", WEISS, y, 3.0)
    y += 92

y += 24
text_font = schrift("Regular", 38)
absatz = ("Danach kann DialOS Mobil in den Play Store: eine App, mit der "
          "blinde Menschen allein durch Sprechen telefonieren.")
for zeile in umbrechen(z, absatz, text_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=text_font, fill=FLIESSTEXT)
    merke("1 noch gesucht", "Fließtext", FLIESSTEXT, y, 4.5)
    y += 52

fusszeile(z)
merke("1 noch gesucht", "Fußzeile", GELB, KANTE - 96, 3.0)
speichern(bild, "dialos-mobil-noch-gesucht-1080.png")

# ---------------------------------------------------------------------------
# 2. Neun Fehler
# ---------------------------------------------------------------------------
bild, z = neues_bild()
kopfzeile(z)
merke("2 neun Fehler", "Kopfzeile", GELB, 68, 3.0)

y = 210
titel_font = schrift("Black", 82)
for zeile in umbrechen(z, "3 Rückmeldungen. 9 Fehler gefunden.",
                       titel_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=titel_font, fill=WEISS)
    merke("2 neun Fehler", "Überschrift", WEISS, y, 3.0)
    y += 100

y += 34
text_font = schrift("Regular", 38)
absatz = ("Drei Testerinnen und Tester haben in wenigen Minuten gefunden, "
          "was ich in Wochen übersehen hatte. Einer davon traf jeden "
          "Nutzer bei jedem Anruf.")
for zeile in umbrechen(z, absatz, text_font, TEXTBREITE):
    z.text((RAND, y), zeile, font=text_font, fill=FLIESSTEXT)
    merke("2 neun Fehler", "Fließtext", FLIESSTEXT, y, 4.5)
    y += 52

y += 40
schluss_font = schrift("Bold", 46)
for zeile in umbrechen(z, "Deshalb lohnt sich Mitmachen.", schluss_font,
                       TEXTBREITE):
    z.text((RAND, y), zeile, font=schluss_font, fill=GELB)
    merke("2 neun Fehler", "Schlusszeile", GELB, y, 3.0)
    y += 58

fusszeile(z)
merke("2 neun Fehler", "Fußzeile", GELB, KANTE - 96, 3.0)
speichern(bild, "dialos-mobil-neun-fehler-1080.png")

# ---------------------------------------------------------------------------
# 3. Voraussetzungen, mit Screenshot
# ---------------------------------------------------------------------------
bild, z = neues_bild()
kopfzeile(z)
merke("3 Voraussetzungen", "Kopfzeile", GELB, 68, 3.0)

# Screenshot rechts, gerahmt. Er belegt die rechte Hälfte, der Text die linke.
schuss = Image.open(SCREENSHOT).convert("RGB")
schuss_hoehe = 620
schuss_breite = int(schuss.width * schuss_hoehe / schuss.height)
schuss = schuss.resize((schuss_breite, schuss_hoehe), Image.LANCZOS)
schuss_x = KANTE - RAND - schuss_breite
schuss_y = 230
z.rounded_rectangle(
    [schuss_x - 7, schuss_y - 7,
     schuss_x + schuss_breite + 7, schuss_y + schuss_hoehe + 7],
    radius=20, fill=WEISS,
)
bild.paste(schuss, (schuss_x, schuss_y))

text_breite = schuss_x - RAND - 48

y = 150
titel_font = schrift("Black", 66)
for zeile in umbrechen(z, "Was du brauchst", titel_font, text_breite):
    z.text((RAND, y), zeile, font=titel_font, fill=WEISS)
    merke("3 Voraussetzungen", "Überschrift", WEISS, y, 3.0)
    y += 80

y += 26
punkt_font = schrift("Regular", 36)
for punkt in ("Ein Android-Handy mit SIM-Karte",
              "Ein Google-Konto darauf",
              "14 Tage die App installiert lassen",
              "Deutsch sprechen"):
    z.text((RAND, y), "•", font=punkt_font, fill=GELB)
    for i, zeile in enumerate(umbrechen(z, punkt, punkt_font,
                                        text_breite - 40)):
        z.text((RAND + 40, y), zeile, font=punkt_font, fill=FLIESSTEXT)
        merke("3 Voraussetzungen", "Aufzählung", FLIESSTEXT, y, 4.5)
        y += 48
    y += 18

fusszeile(z)
merke("3 Voraussetzungen", "Fußzeile", GELB, KANTE - 96, 3.0)
speichern(bild, "dialos-mobil-voraussetzungen-1080.png")

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
print("1) dialos-mobil-noch-gesucht-1080.png")
print("   Blaue Grafik mit einer großen gelben Vier. Text: Vier Menschen "
      "fehlen noch. Danach kann DialOS Mobil in den Play Store - eine App, "
      "mit der blinde Menschen allein durch Sprechen telefonieren.")
print()
print("2) dialos-mobil-neun-fehler-1080.png")
print("   Blaue Grafik mit der Überschrift: Drei Rückmeldungen, neun Fehler "
      "gefunden. Darunter: Drei Testerinnen und Tester haben in wenigen "
      "Minuten gefunden, was ich in Wochen übersehen hatte.")
print()
print("3) dialos-mobil-voraussetzungen-1080.png")
print("   Blaue Grafik mit der Überschrift: Was du brauchst. Aufgezählt: ein "
      "Android-Handy mit SIM-Karte, ein Google-Konto darauf, vierzehn Tage "
      "die App installiert lassen, Deutsch sprechen. Rechts der "
      "Startbildschirm der App mit einem großen blauen Knopf.")
