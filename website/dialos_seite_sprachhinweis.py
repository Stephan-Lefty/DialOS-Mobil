#!/usr/bin/env python3
"""Setzt auf der englischen Seite (721) einen deutlichen Hinweis, dass die App
nur auf Deutsch funktioniert.

Stephans Vorgabe vom 05.10.2026. Der Hinweis stand vorher nur als Nebensatz
in Kursivschrift da - zu wenig fuer jemanden, der gleich 54 MB herunterlaedt
und dann eine App vor sich hat, die ihn nicht versteht.

Zwei Stellen, beide wortgenau verankert:
  1. Ein hervorgehobener Kasten gleich oben, anstelle des Nebensatzes.
  2. Eine kurze Erinnerung direkt ueber dem Download-Knopf - dort faellt die
     Entscheidung, nicht oben.

Ohne --schreiben nur Probelauf.
"""
import sys
from wp_zugang import call

SEITE = 721

ALT_HINWEIS = (
    "<!-- wp:paragraph -->\n"
    "<p><em>The app speaks and understands German only.</em></p>\n"
    "<!-- /wp:paragraph -->"
)

NEU_HINWEIS = (
    '<!-- wp:group {"style":{"spacing":{"padding":{"top":"1rem",'
    '"right":"1.25rem","bottom":"1rem","left":"1.25rem"}},'
    '"border":{"radius":"12px","width":"2px","color":"#0d4280"}},'
    '"layout":{"type":"constrained"}} -->\n'
    '<div class="wp-block-group has-border-color" style="border-color:#0d4280;'
    "border-width:2px;border-radius:12px;padding-top:1rem;"
    'padding-right:1.25rem;padding-bottom:1rem;padding-left:1.25rem">\n'
    "<!-- wp:paragraph -->\n"
    "<p><strong>Please note: the app itself is German only.</strong> The wake "
    "phrase, every spoken question and every spoken answer are in German, and "
    "so is the on-screen text. Speech recognition uses a German model; an "
    "English one is not built in. This page is in English, the app is not – "
    "please keep that in mind before downloading.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "</div>\n"
    "<!-- /wp:group -->"
)

ALT_VOR_KNOPF = (
    "<!-- wp:paragraph -->\n"
    "<p>DialOS Mobil is not publicly listed on the Play Store yet. The "
    "closed test that Google requires is still running, and without an "
    "invitation nobody can reach the app there. If you would like to try it "
    "anyway, download it directly here.</p>\n"
    "<!-- /wp:paragraph -->"
)

NEU_VOR_KNOPF = (
    ALT_VOR_KNOPF + "\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p><strong>Once more, because this is where it matters:</strong> you are "
    "about to download a 54 MB app that only speaks and understands "
    "German.</p>\n"
    "<!-- /wp:paragraph -->"
)


def main():
    schreiben = "--schreiben" in sys.argv
    d = call("GET", f"wp/v2/pages/{SEITE}?context=edit")
    inhalt = d["content"]["raw"]
    vorher = len(inhalt)

    for alt, neu, name in ((ALT_HINWEIS, NEU_HINWEIS, "Hinweiskasten oben"),
                           (ALT_VOR_KNOPF, NEU_VOR_KNOPF, "Erinnerung vor dem Knopf")):
        treffer = inhalt.count(alt)
        if treffer != 1:
            print(f"ABBRUCH: {name} - Anker {treffer}-mal gefunden, "
                  "erwartet genau 1.", file=sys.stderr)
            return 1
        inhalt = inhalt.replace(alt, neu)
        print(f"  {name}: ersetzt")

    print(f"\nLaenge {vorher} -> {len(inhalt)} Zeichen "
          f"({len(inhalt) - vorher:+d})")

    if not schreiben:
        print("\nProbelauf. Zum Schreiben mit --schreiben aufrufen.")
        return 0

    call("POST", f"wp/v2/pages/{SEITE}", {"content": inhalt})
    print("Gespeichert.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
