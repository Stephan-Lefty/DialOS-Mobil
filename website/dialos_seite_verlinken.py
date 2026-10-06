#!/usr/bin/env python3
"""Verlinkt die Download-Seiten von DialOS Mobil auf dialos.org.

Drei Eingriffe:

1. Menueeintrag "DialOS Mobil" unter "Sprachsteuerung" (Menue 7, Elterneintrag
   35). Stephans Vorgabe vom 05.10.2026.
2. Startseite deutsch (Seite 2): der bestehende DialOS-Mobil-Absatz bekommt
   einen Satz dazu.
3. Startseite englisch (Seite 135): dasselbe.

Alles wortgenau verankert - laeuft ein Anker ins Leere oder passt er
mehrfach, bricht das Skript ab, statt die Seite halb fertig zu speichern.

Bewusst NICHT angefasst: die Tester-Aufrufe (Beitraege 208 und 209). Dort
einen Direkt-Download anzubieten wuerde dem Zweck der Seite zuwiderlaufen -
es fehlen noch Tester fuer den geschlossenen Test, und wer die App einfach
herunterladen kann, meldet sich mit einiger Wahrscheinlichkeit nicht mehr an.

Ohne --schreiben nur Probelauf.
"""
import sys
from wp_zugang import call

MENUE = 7
ELTERNEINTRAG = 35          # "Sprachsteuerung"
DE_SEITE_ID = 720
# Nachgezogen am 06.10.2026: Die Seiten hiessen zuerst
# /dialos-mobil-herunterladen/ und /en/download-dialos-mobil/ und wurden von
# dialos_seite_ausbauen.py auf die kurzen Slugs umbenannt. Die alten geben
# jetzt 404. Stehen hier die alten URLs, findet menueeintrag_fehlt() den
# vorhandenen Eintrag nicht wieder und legt bei einem zweiten Lauf einen
# doppelten an - deshalb muessen sie mit den Seiten mitwandern.
DE_URL = "https://dialos.org/dialos-mobil/"
EN_URL = "https://dialos.org/en/dialos-mobil/"

EINGRIFFE = [
    (
        "pages", 2, "Startseite deutsch",
        '<a href="https://dialos.org/dialos-mobil-tester-gesucht/">'
        "Testerinnen und Tester gesucht</a>.</p>",
        '<a href="https://dialos.org/dialos-mobil-tester-gesucht/">'
        "Testerinnen und Tester gesucht</a>. Wer nicht warten moechte, kann "
        f'die App auch <a href="{DE_URL}">direkt herunterladen</a>.</p>',
    ),
    (
        "pages", 135, "Startseite englisch",
        '<a href="https://dialos.org/en/dialos-mobil-testers-wanted/">'
        "Testers are still wanted</a> for the closed test on the Play "
        "Store.</p>",
        '<a href="https://dialos.org/en/dialos-mobil-testers-wanted/">'
        "Testers are still wanted</a> for the closed test on the Play "
        f'Store. If you would rather not wait, you can <a href="{EN_URL}">'
        "download the app directly</a>.</p>",
    ),
]

# Das Skript wird in einer Datei gespeichert, in der Umlaute als Escapes
# stehen muessen, damit der Anker byte-genau passt - die Seiten selbst
# enthalten echte Umlaute.
EINGRIFFE[0] = (
    EINGRIFFE[0][0], EINGRIFFE[0][1], EINGRIFFE[0][2],
    EINGRIFFE[0][3],
    EINGRIFFE[0][4].replace("moechte", "möchte"),
)


def menueeintrag_fehlt():
    eintraege = call("GET", f"wp/v2/menu-items?menus={MENUE}&per_page=50"
                            "&_fields=id,title,url,parent,menu_order")
    for e in eintraege:
        if DE_URL.rstrip("/") in e["url"].rstrip("/"):
            return None, e
    return eintraege, None


def main():
    schreiben = "--schreiben" in sys.argv

    eintraege, schon_da = menueeintrag_fehlt()
    if schon_da:
        print(f"  Menue: Eintrag gibt es schon (ID {schon_da['id']})")
    else:
        geschwister = [e["menu_order"] for e in eintraege
                       if e["parent"] == ELTERNEINTRAG]
        platz = max(geschwister) + 1 if geschwister else 1
        print(f"  Menue: neuer Eintrag 'DialOS Mobil' unter Eintrag "
              f"{ELTERNEINTRAG}, Platz {platz}")
        if schreiben:
            neu = call("POST", "wp/v2/menu-items", {
                "title": "DialOS Mobil",
                "menus": MENUE,
                "parent": ELTERNEINTRAG,
                "menu_order": platz,
                "type": "post_type",
                "object": "page",
                "object_id": DE_SEITE_ID,
                "status": "publish",
            })
            print(f"    angelegt: ID {neu['id']}")

    for art, pid, name, alt, neu in EINGRIFFE:
        d = call("GET", f"wp/v2/{art}/{pid}?context=edit")
        inhalt = d["content"]["raw"]
        if neu in inhalt:
            print(f"  {name}: Verweis steht schon")
            continue
        treffer = inhalt.count(alt)
        if treffer != 1:
            print(f"ABBRUCH: {name} - Anker {treffer}-mal gefunden, "
                  "erwartet genau 1.", file=sys.stderr)
            return 1
        print(f"  {name}: Anker passt, {len(neu) - len(alt):+d} Zeichen")
        if schreiben:
            call("POST", f"wp/v2/{art}/{pid}",
                 {"content": inhalt.replace(alt, neu)})
            print("    gespeichert")

    if not schreiben:
        print("\nProbelauf. Zum Schreiben mit --schreiben aufrufen.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
