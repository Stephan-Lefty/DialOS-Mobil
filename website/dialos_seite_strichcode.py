#!/usr/bin/env python3
"""Setzt einen Strichcode neben den Download-Knopf der Seiten 720/721.

Stephans Wunsch vom 10.10.2026: "waere es doch cool einen QR-Code in der
Repo mit aufzufuehren" - im README steht er seitdem. Auf der Webseite ist
er sogar nuetzlicher: Die Installationsanleitung richtet sich an eine
helfende Person, und die liest die Seite oft am Rechner, waehrend das
Telefon in der Hand liegt. Genau dafuer ist ein Strichcode da.

DER UNTERSCHIED ZUM README, UND WARUM ER WICHTIG IST

Im README zeigt der Strichcode auf releases/latest/download/ - immer auf
die neueste Fassung. Auf diesen Seiten waere das falsch. Hier steht
direkt daneben eine SHA-256-Summe, und ein latest-Link wuerde beim
naechsten Release eine andere Datei liefern als die Summe beschreibt.
Die Seite widerspraeche sich an genau der Stelle, die zum Pruefen
gedacht ist.

Deshalb liest dieses Werkzeug die Adresse NICHT aus einer eigenen
Konstante, sondern aus dem Download-Knopf der Seite selbst. Damit kann
der Strichcode gar nicht etwas anderes liefern als der Knopf daneben -
nicht weil jemand daran denkt, sondern weil es keine zweite Quelle gibt.
Nach einem Release genuegt es, die Seite zu aktualisieren und dieses
Werkzeug noch einmal laufen zu lassen.

Zur Sicherheit wird die verlinkte Datei geholt und ihre Pruefsumme mit
der auf der Seite verglichen. Stimmen sie nicht ueberein, bricht das
Werkzeug ab - dann ist die Seite selbst widerspruechlich, und ein
Strichcode darauf waere die kleinste Sorge.

Ohne --schreiben nur Probelauf.
"""
import argparse
import hashlib
import json
import re
import subprocess
import sys
import tempfile
import urllib.request
from pathlib import Path

from wp_zugang import AUTH, WP, call

SEITEN = {"de": 720, "en": 721}

# Die Leitfarbe aus dem DialOS-Logo (app/src/main/res/values/colors.xml,
# dialos_blue). Nicht die gemeinsame Palette - DialOS hat eine eigene.
FARBE = "1F6FB5"

ALT_TEXT = {
    "de": "Strichcode, der die App direkt auf das Telefon laedt",
    "en": "Barcode that downloads the app straight to the phone",
}

BILDUNTERSCHRIFT = {
    "de": ("Mit der Kamera des Telefons abfotografieren - der Browser "
           "laedt dann dieselbe Datei wie der Knopf darueber."),
    "en": ("Photograph it with the phone's camera - the browser then "
           "downloads the same file as the button above."),
}

MARKER = "<!-- dialos-strichcode -->"


def seite_lesen(seiten_id):
    return call("GET", f"wp/v2/pages/{seiten_id}?context=edit")["content"]["raw"]


def apk_adresse(inhalt, seiten_id):
    """Die Adresse aus dem Download-Knopf - die einzige Quelle."""
    treffer = re.findall(
        r'href="(https://github\.com/[^"]+?\.apk)"', inhalt)
    eindeutig = sorted(set(treffer))
    if not eindeutig:
        sys.exit(f"Seite {seiten_id}: kein Download-Link auf eine APK "
                 "gefunden. Zeigt die Seite noch auf GitHub?")
    if len(eindeutig) > 1:
        sys.exit(f"Seite {seiten_id}: mehrere verschiedene APK-Adressen "
                 f"({', '.join(eindeutig)}). Von Hand klaeren - raten "
                 "waere hier der falsche Weg.")
    if "/releases/latest/" in eindeutig[0]:
        sys.exit(f"Seite {seiten_id}: der Knopf zeigt auf 'latest'. Auf "
                 "dieser Seite steht eine Pruefsumme daneben; ein "
                 "latest-Link macht sie beim naechsten Release falsch. "
                 "Erst den Knopf versionsfest machen.")
    return eindeutig[0]


def summe_auf_der_seite(inhalt):
    treffer = re.findall(r'\b([0-9a-f]{64})\b', inhalt)
    return sorted(set(treffer))


def datei_pruefen(adresse, erwartet):
    """Die verlinkte Datei holen und gegen die Seite pruefen."""
    print(f"  hole {adresse.rsplit('/', 1)[-1]} ...", flush=True)
    h = hashlib.sha256()
    laenge = 0
    with urllib.request.urlopen(adresse, timeout=300) as a:
        while stueck := a.read(1 << 20):
            h.update(stueck)
            laenge += len(stueck)
    gemessen = h.hexdigest()
    print(f"  {laenge} Bytes, SHA-256 {gemessen}")
    if erwartet and gemessen not in erwartet:
        sys.exit("Die Pruefsumme der verlinkten Datei steht nicht auf der "
                 f"Seite.\n  gemessen: {gemessen}\n  auf der Seite: "
                 f"{', '.join(erwartet)}\nDie Seite widerspricht sich "
                 "selbst - das gehoert zuerst geklaert.")
    if not erwartet:
        print("  WARNUNG: auf der Seite steht keine Pruefsumme zum "
              "Vergleichen.")
    return gemessen


def strichcode_erzeugen(adresse, ziel):
    subprocess.run(
        ["qrencode", "-s", "10", "-m", "2", f"--foreground={FARBE}",
         "-o", str(ziel), adresse],
        check=True)
    zurueck = subprocess.run(
        ["zbarimg", "--quiet", "--raw", str(ziel)],
        check=True, capture_output=True, text=True).stdout.strip()
    if zurueck != adresse:
        sys.exit(f"Der erzeugte Strichcode liest sich als '{zurueck}', "
                 f"erwartet war '{adresse}'.")
    print(f"  Strichcode erzeugt und zurueckgelesen: {ziel.name}")
    return ziel


def hochladen(pfad, dateiname):
    """In die Mediathek - nicht von raw.githubusercontent einbinden.

    Sonst holt jeder Seitenaufruf ein Bild bei GitHub, und das ist ein
    Fremdaufruf auf einer Seite, deren Besucher Wert auf Datenschutz
    legen.
    """
    vorhanden = call("GET", f"wp/v2/media?search={dateiname}&per_page=100",
                     still=True) or []
    for m in vorhanden:
        if m.get("slug") == Path(dateiname).stem:
            print(f"  Mediathek: schon vorhanden (ID {m['id']})")
            return m["id"], m["source_url"]

    req = urllib.request.Request(
        f"{WP}/wp-json/wp/v2/media", data=pfad.read_bytes(), method="POST")
    req.add_header("Authorization", f"Basic {AUTH}")
    req.add_header("Content-Type", "image/png")
    req.add_header("Content-Disposition",
                   f'attachment; filename="{dateiname}"')
    with urllib.request.urlopen(req, timeout=120) as a:
        m = json.loads(a.read().decode())
    print(f"  Mediathek: hochgeladen (ID {m['id']})")
    return m["id"], m["source_url"]


def block(bild_id, bild_url, sprache):
    return (
        f"{MARKER}\n"
        '<!-- wp:image {"id":%d,"width":220,"sizeSlug":"full",'
        '"linkDestination":"none","align":"center"} -->\n' % bild_id
        + '<figure class="wp-block-image aligncenter size-full '
          'is-resized">'
          f'<img src="{bild_url}" alt="{ALT_TEXT[sprache]}" '
          f'class="wp-image-{bild_id}" style="width:220px"/>'
          f'<figcaption class="wp-element-caption">'
          f'{BILDUNTERSCHRIFT[sprache]}</figcaption></figure>\n'
          "<!-- /wp:image -->"
    )


def einfuegen(inhalt, neuer_block):
    """Direkt hinter den Knopf. Der sichtbare Link bleibt stehen -
    einem blinden Besucher nuetzt der Strichcode nichts, er ist fuer
    die helfende Person."""
    if MARKER in inhalt:
        return re.sub(
            re.escape(MARKER) + r".*?<!-- /wp:image -->",
            neuer_block, inhalt, flags=re.S)
    ende = inhalt.find("<!-- /wp:buttons -->")
    if ende == -1:
        sys.exit("Kein Knopf-Block gefunden, an den sich der Strichcode "
                 "haengen koennte.")
    ende += len("<!-- /wp:buttons -->")
    return inhalt[:ende] + "\n\n" + neuer_block + inhalt[ende:]


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--schreiben", action="store_true",
                   help="ohne diesen Schalter nur Probelauf")
    args = p.parse_args()

    with tempfile.TemporaryDirectory() as tmp:
        for sprache, seiten_id in SEITEN.items():
            print(f"\n=== Seite {seiten_id} ({sprache})")
            inhalt = seite_lesen(seiten_id)
            adresse = apk_adresse(inhalt, seiten_id)
            print(f"  Knopf zeigt auf: {adresse}")
            datei_pruefen(adresse, summe_auf_der_seite(inhalt))

            name = adresse.rsplit("/", 1)[-1].replace(".apk", "")
            datei = Path(tmp) / f"strichcode-{name}-{sprache}.png"
            strichcode_erzeugen(adresse, datei)

            if not args.schreiben:
                print("  Probelauf - nichts hochgeladen, nichts geaendert.")
                continue

            bild_id, bild_url = hochladen(
                datei, f"dialos-mobil-strichcode-{name}-{sprache}.png")
            neu = einfuegen(inhalt, block(bild_id, bild_url, sprache))
            call("POST", f"wp/v2/pages/{seiten_id}", {"content": neu})
            print("  Seite aktualisiert.")


if __name__ == "__main__":
    main()
