#!/usr/bin/env python3
"""Zwei quadratische Grafiken (1500x1500) für die Verbandsmedien.

Angefordert vom BSVÖ am 23.08.2026 für Website, Newsletter und Social
Media. Quadratisch, weil Instagram und Facebook damit am wenigsten
anstellen - jedes andere Seitenverhältnis wird dort beschnitten.

  1. dialos-mobil-so-funktioniert-es-1500.png
     Der Gesprächsablauf als Sprechblasen. Erklärt die App ohne ein einziges
     Fachwort und ohne dass man sie je gesehen haben muss.

  2. dialos-mobil-tester-gesucht-1500.png
     Der Startbildschirm neben den Voraussetzungen zum Mitmachen.

Zur Schriftgröße: Die kleinste Schrift liegt bei 40 px auf 1500 px Breite.
Das ist für eine Grafik viel und für dieses Publikum das Mindeste. Wer
diese Meldung liest, sieht schlecht - eine Grafik in Agenturgröße wäre hier
eine Unhöflichkeit.

Die Alternativtexte stehen unten in der Ausgabe. Sie gehören mit an den
Verband: Eine Grafik ohne Alternativtext ist auf der Website eines
Blindenverbands ein Eigentor.
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

REPO = Path(__file__).resolve().parent.parent
SCREENSHOT = REPO / "screenshots" / "01_startseite_aus.png"
ZIELORDNER = REPO / "screenshots" / "verbaende"

KANTE = 1500

BLAU = (31, 111, 181)         # dialos_blue
BLAU_DUNKEL = (14, 61, 102)   # dialos_blue_dark
WEISS = (255, 255, 255)
GELB = (255, 224, 0)          # hc_yellow
TEXT_DUNKEL = (17, 20, 23)    # on_surface
GRAU = (90, 100, 110)         # on_surface_muted
BLASSBLAU = (227, 237, 245)   # surface_variant

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


pruefungen = []


def pruefe(name, vorne, hinten, schwelle=4.5):
    pruefungen.append((name, kontrast(vorne, hinten), schwelle))


# ---------------------------------------------------------------------------
# Grafik 1: der Gesprächsablauf
# ---------------------------------------------------------------------------
def grafik_gespraech():
    bild = Image.new("RGB", (KANTE, KANTE), BLAU_DUNKEL)
    z = ImageDraw.Draw(bild)

    for y in range(KANTE):
        anteil = y / KANTE
        z.line([(0, y), (KANTE, y)],
               fill=tuple(int(BLAU[i] + (BLAU_DUNKEL[i] - BLAU[i]) * anteil)
                          for i in range(3)))

    rand = 80
    titel_font = schrift("Black", 76)
    z.text((rand, 78), "Telefonieren,", font=titel_font, fill=WEISS)
    z.text((rand, 166), "ohne hinzusehen", font=titel_font, fill=WEISS)

    # Wer spricht, Text, Seite ("nutzer" rechts, "app" links)
    verlauf = [
        ("nutzer", "„Sprachsteuerung starten“"),
        ("app", "„Wen möchten Sie anrufen?“"),
        ("nutzer", "„Anna Berger anrufen“"),
        ("app", "„Soll ich Anna Berger anrufen?\nSagen Sie Ja oder Nein.“"),
        ("nutzer", "„Ja“"),
        ("app", "„Ich rufe Anna Berger an.“"),
    ]

    blasen_font = schrift("Medium", 44)
    kennung_font = schrift("Bold", 28)
    y = 306
    max_text = 880
    innen = 30

    for wer, text in verlauf:
        zeilen = []
        for absatz in text.split("\n"):
            zeilen += umbrechen(z, absatz, blasen_font, max_text)

        breite = max(z.textlength(zl, font=blasen_font) for zl in zeilen)
        breite = int(breite) + 2 * innen
        hoehe = len(zeilen) * 56 + 2 * innen - 8

        if wer == "nutzer":
            x1 = KANTE - rand - breite
            fuell, vorne, kennung = GELB, TEXT_DUNKEL, "SIE"
        else:
            x1 = rand
            fuell, vorne, kennung = WEISS, TEXT_DUNKEL, "DIE APP"

        z.text((x1 + 4, y - 34), kennung, font=kennung_font,
               fill=GELB if wer == "app" else BLASSBLAU)
        z.rounded_rectangle([x1, y, x1 + breite, y + hoehe], radius=26,
                            fill=fuell)

        ty = y + innen - 4
        for zl in zeilen:
            z.text((x1 + innen, ty), zl, font=blasen_font, fill=vorne)
            ty += 56

        y += hoehe + 62

    fuss_font = schrift("Bold", 40)
    z.text((rand, KANTE - 108),
           "DialOS Mobil · kostenlos · dialos.org",
           font=fuss_font, fill=GELB)

    pruefe("G1 Titel weiß auf blau", WEISS, BLAU, 3.0)
    pruefe("G1 Text in gelber Blase", TEXT_DUNKEL, GELB)
    pruefe("G1 Text in weißer Blase", TEXT_DUNKEL, WEISS)
    pruefe("G1 Kennung gelb auf blau", GELB, BLAU, 3.0)
    pruefe("G1 Fußzeile gelb", GELB, BLAU_DUNKEL, 3.0)
    return bild


# ---------------------------------------------------------------------------
# Grafik 2: Tester gesucht
# ---------------------------------------------------------------------------
def grafik_tester():
    bild = Image.new("RGB", (KANTE, KANTE), WEISS)
    z = ImageDraw.Draw(bild)

    kopf_hoehe = 300
    z.rectangle([0, 0, KANTE, kopf_hoehe], fill=BLAU_DUNKEL)

    rand = 80
    titel_font = schrift("Black", 72)
    z.text((rand, 66), "Testerinnen und", font=titel_font, fill=WEISS)
    z.text((rand, 152), "Tester gesucht", font=titel_font, fill=GELB)

    # Screenshot links
    schuss = Image.open(SCREENSHOT).convert("RGB")
    ziel_hoehe = 880
    ziel_breite = int(schuss.width * ziel_hoehe / schuss.height)
    schuss = schuss.resize((ziel_breite, ziel_hoehe), Image.LANCZOS)
    sx, sy = rand, kopf_hoehe + 80
    z.rounded_rectangle([sx - 5, sy - 5, sx + ziel_breite + 5,
                         sy + ziel_hoehe + 5], radius=20, fill=BLASSBLAU)
    bild.paste(schuss, (sx, sy))

    # Voraussetzungen rechts
    tx = sx + ziel_breite + 70
    tb = KANTE - rand - tx
    y = sy + 6

    lead_font = schrift("Bold", 46)
    for zl in umbrechen(z, "Sie brauchen dafür:", lead_font, tb):
        z.text((tx, y), zl, font=lead_font, fill=BLAU_DUNKEL)
        y += 60
    y += 26

    punkt_font = schrift("Regular", 40)
    for punkt in [
        "ein Android-Handy ab Android 8 mit SIM-Karte",
        "ein Google-Konto",
        "zwei Wochen Zeit – die App bleibt einfach drauf",
        "Deutsch als Bediensprache",
    ]:
        zeilen = umbrechen(z, punkt, punkt_font, tb - 46)
        z.ellipse([tx + 4, y + 15, tx + 22, y + 33], fill=BLAU)
        for i, zl in enumerate(zeilen):
            z.text((tx + 46, y), zl, font=punkt_font, fill=TEXT_DUNKEL)
            y += 50
        y += 26

    fuss_y = KANTE - 190
    z.rectangle([0, fuss_y, KANTE, KANTE], fill=BLAU)
    adr_font = schrift("Bold", 44)
    klein_font = schrift("Regular", 36)
    # Weiß statt BLASSBLAU: Der hellblaue Ton kam auf diesem Blau nur auf
    # 4,43:1 und lag damit knapp unter der Schwelle für Fließtext.
    z.text((rand, fuss_y + 40), "Kostenlos, werbefrei, quelloffen.",
           font=klein_font, fill=WEISS)
    z.text((rand, fuss_y + 96), "dialos.org", font=adr_font, fill=GELB)

    pruefe("G2 Titel weiß auf dunkelblau", WEISS, BLAU_DUNKEL, 3.0)
    pruefe("G2 Titel gelb auf dunkelblau", GELB, BLAU_DUNKEL, 3.0)
    pruefe("G2 Aufzählung auf weiß", TEXT_DUNKEL, WEISS)
    pruefe("G2 Zwischentitel auf weiß", BLAU_DUNKEL, WEISS)
    pruefe("G2 Fußzeile weiß auf blau", WEISS, BLAU)
    pruefe("G2 Adresse gelb auf blau", GELB, BLAU, 3.0)
    return bild


ALTTEXTE = {
    "dialos-mobil-so-funktioniert-es-1500.png":
        "Schaubild mit der Überschrift „Telefonieren, ohne hinzusehen“. In "
        "abwechselnden Sprechblasen steht ein Gespräch zwischen Nutzer und "
        "App: Sie sagen „Sprachsteuerung starten“, die App fragt „Wen "
        "möchten Sie anrufen?“, Sie sagen „Anna Berger anrufen“, die App "
        "fragt zurück „Soll ich Anna Berger anrufen? Sagen Sie Ja oder "
        "Nein.“, Sie antworten „Ja“, und die App sagt „Ich rufe Anna Berger "
        "an.“ Unten steht: DialOS Mobil, kostenlos, dialos.org.",
    "dialos-mobil-tester-gesucht-1500.png":
        "Aufruf mit der Überschrift „Testerinnen und Tester gesucht“. Links "
        "der Startbildschirm der App: ein sehr großer blauer Knopf mit der "
        "Aufschrift „Sprachsteuerung einschalten“ füllt die halbe "
        "Bildschirmhöhe. Rechts daneben unter „Sie brauchen dafür“ vier "
        "Punkte: ein Android-Handy ab Android 8 mit SIM-Karte, ein "
        "Google-Konto, zwei Wochen Zeit – die App bleibt einfach drauf, und "
        "Deutsch als Bediensprache. Unten steht: Kostenlos, werbefrei, "
        "quelloffen. dialos.org.",
}

# Erst bauen, dann pruefen, dann erst schreiben. Andersherum laege bei
# einem Fehlschlag eine Grafik auf der Platte, die die Pruefung nicht
# bestanden hat - und genau die wuerde dann verschickt.
fertig = [
    ("dialos-mobil-so-funktioniert-es-1500.png", grafik_gespraech()),
    ("dialos-mobil-tester-gesucht-1500.png", grafik_tester()),
]

print("Kontrast nach WCAG (Fließtext ab 4.5:1, große Schrift ab 3:1):")
alles_gut = True
for name, wert, schwelle in pruefungen:
    ok = wert >= schwelle
    alles_gut = alles_gut and ok
    print(f"  {name:<32} {wert:5.2f}:1  (nötig {schwelle})  "
          f"{'ok' if ok else 'ZU SCHWACH'}")

if not alles_gut:
    raise SystemExit("\nMindestens ein Wert liegt unter der Schwelle. "
                     "Es wurde nichts geschrieben.")

ZIELORDNER.mkdir(parents=True, exist_ok=True)
print()
for name, bild in fertig:
    pfad = ZIELORDNER / name
    bild.save(pfad, "PNG", optimize=True)
    print(f"{pfad.name}  {KANTE}x{KANTE}, {pfad.stat().st_size // 1024} KB")

print()
print("Alternativtexte (gehören mit an den Verband):")
for name, text in ALTTEXTE.items():
    print(f"\n{name}\n  {text}")
