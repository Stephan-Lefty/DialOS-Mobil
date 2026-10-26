#!/usr/bin/env python3
"""Zieht die Seiten 720/721 auf eine neue Fassung: Link, Nummer, Pruefsumme.

Entstanden am 26.10.2026, als 0.6.18 freigegeben war und auffiel, dass es
fuer diesen Handgriff kein Werkzeug gab. dialos_seite_ausbauen.py baut die
Seiten komplett neu auf - wer das nach einem Release laufen laesst,
loescht die Installationsanleitung und den Strichcode gleich mit. Also
ein Werkzeug, das nur die drei Angaben ersetzt, die sich je Fassung
aendern:

  * die Adresse der APK im Download-Knopf
  * die Versionsnummer im sichtbaren Text
  * die SHA-256-Summe im Pruefblock

DIE PRUEFSUMME WIRD NICHT GEGLAUBT, SONDERN GEMESSEN. Das Werkzeug holt
die neue Datei von GitHub und rechnet selbst nach. Eine von Hand
eingetippte Summe waere genau die Art Angabe, die niemand nachprueft und
die irgendwann still falsch ist - auf einer Seite, deren einziger Zweck
an dieser Stelle das Nachpruefen ist.

Danach gehoert dialos_seite_strichcode.py laufen gelassen: Der liest die
Adresse aus dem Knopf und erneuert den Strichcode passend.

Aufruf:
    python3 dialos_seite_fassung.py 0.6.18            # Probelauf
    python3 dialos_seite_fassung.py 0.6.18 --schreiben
"""
import argparse
import hashlib
import re
import sys
import urllib.error
import urllib.request

from wp_zugang import call

SEITEN = {"de": 720, "en": 721}
BASIS = "https://github.com/Stephan-Lefty/DialOS-Mobil/releases/download"


def adresse(fassung):
    return f"{BASIS}/v{fassung}/DialOS-Mobil-{fassung}.apk"


def datei_messen(url):
    """Holen und selbst nachrechnen - nicht abschreiben."""
    print(f"hole {url}")
    h = hashlib.sha256()
    laenge = 0
    try:
        with urllib.request.urlopen(url, timeout=300) as a:
            while stueck := a.read(1 << 20):
                h.update(stueck)
                laenge += len(stueck)
    except urllib.error.HTTPError as f:
        sys.exit(f"HTTP {f.code} - liegt die Fassung schon als Release auf "
                 "GitHub? Erst das Release anlegen, dann diese Seite.")
    mb = round(laenge / 1_000_000)
    print(f"  {laenge} Bytes (~{mb} MB)\n  SHA-256 {h.hexdigest()}")
    return h.hexdigest(), mb


def alte_fassung(inhalt):
    treffer = re.findall(r"DialOS-Mobil-(\d+\.\d+\.\d+)\.apk", inhalt)
    if not treffer:
        sys.exit("Keine bisherige Fassung auf der Seite gefunden.")
    return sorted(set(treffer))[-1]


def ersetzen(inhalt, alt, neu, summe, mb):
    """Reihenfolge wichtig: erst die lange Adresse, dann die nackte
    Nummer - sonst zerlegt der zweite Schritt die Adresse."""
    zaehler = {}

    adr_alt, adr_neu = adresse(alt), adresse(neu)
    zaehler["Adresse"] = inhalt.count(adr_alt)
    inhalt = inhalt.replace(adr_alt, adr_neu)

    zaehler["Nummer"] = len(re.findall(rf"\b{re.escape(alt)}\b", inhalt))
    inhalt = re.sub(rf"\b{re.escape(alt)}\b", neu, inhalt)

    alte_summen = set(re.findall(r"\b[0-9a-f]{64}\b", inhalt))
    zaehler["Pruefsumme"] = len(alte_summen)
    for s in alte_summen:
        inhalt = inhalt.replace(s, summe)

    # "(54 MB)" im Knopftext - nur, wenn die Angabe sich geaendert hat.
    def mb_ersetzen(m):
        return f"({mb} MB)"
    inhalt, n = re.subn(r"\((\d{2}) MB\)", mb_ersetzen, inhalt)
    zaehler["Groesse"] = n

    return inhalt, zaehler


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("fassung", help="neue Fassung, z. B. 0.6.18")
    p.add_argument("--schreiben", action="store_true",
                   help="ohne diesen Schalter nur Probelauf")
    args = p.parse_args()

    if not re.fullmatch(r"\d+\.\d+\.\d+", args.fassung):
        sys.exit(f"'{args.fassung}' sieht nicht wie eine Fassung aus.")

    summe, mb = datei_messen(adresse(args.fassung))

    for sprache, seiten_id in SEITEN.items():
        print(f"\n=== Seite {seiten_id} ({sprache})")
        inhalt = call("GET", f"wp/v2/pages/{seiten_id}?context=edit")
        inhalt = inhalt["content"]["raw"]
        alt = alte_fassung(inhalt)
        if alt == args.fassung:
            print(f"  steht schon auf {alt} - nichts zu tun.")
            continue
        print(f"  {alt} -> {args.fassung}")
        neu, zaehler = ersetzen(inhalt, alt, args.fassung, summe, mb)
        print("  ersetzt: " + ", ".join(f"{k} {v}×"
                                        for k, v in zaehler.items()))
        if not args.schreiben:
            print("  Probelauf - nichts geaendert.")
            continue
        call("POST", f"wp/v2/pages/{seiten_id}", {"content": neu})
        print("  Seite aktualisiert.")

    print("\nDanach nicht vergessen:  python3 dialos_seite_strichcode.py "
          "--schreiben")


if __name__ == "__main__":
    main()
