#!/usr/bin/env python3
"""Veroeffentlicht den Beitrag zur Aufnahme in die App des bvkm.

Legt an bzw. aktualisiert (mehrfach ausfuehrbar):

  1. deutscher Beitrag  /dialos-mobil-beim-bvkm/
  2. englischer Beitrag /en/dialos-mobil-recommended-by-bvkm/
  3. Eintraege oben auf /neuigkeiten/ (184) und /en/news/ (363)

Aufbau und Konventionen sind aus dialos-mobil-testbericht.py uebernommen -
dem aktuellen Muster: Sprachumschalter als erster Block, englischer Beitrag
ohne Kommentare und mit eigener Meta-Zeile (deshalb wird er zweimal
geschrieben, einmal fuer die ID und einmal mit ihr).

WORUM ES IM BEITRAG GEHT, und warum er nicht als Dankesmeldung angelegt
ist: Die Nachricht ist nicht "ein Verband empfiehlt uns", sondern WER es
tut. Der bvkm arbeitet mit koerperlich und mehrfach behinderten Menschen.
DialOS Mobil ist aber fast immer ueber die Blindheit gedacht und
beschrieben worden - die motorische Seite stand daneben, nie im
Mittelpunkt. Der Beitrag nimmt die Aufnahme zum Anlass, diese zweite
Zielgruppe zum ersten Mal ausdruecklich anzusprechen.

Das Zitat aus der Zusage des bvkm steht bewusst NICHT im Beitrag. Es war
eine Mail an Stephan, keine Pressemitteilung.

Ohne --schreiben nur Probelauf.
"""
import argparse
import sys

from wp_zugang import call

NEWS_DE, ARCHIV_DE = 184, 258
NEWS_EN, ARCHIV_EN = 363, 364
MAX_EINTRAEGE = 5

SLUG_DE = "dialos-mobil-beim-bvkm"
SLUG_EN = "dialos-mobil-recommended-by-bvkm"
URL_DE = f"https://dialos.org/{SLUG_DE}/"
URL_EN = f"https://dialos.org/en/{SLUG_EN}/"

TITEL_DE = ("Der bvkm hat DialOS Mobil aufgenommen – und bringt eine "
            "zweite Zielgruppe mit")
TITEL_EN = ("The bvkm has listed DialOS Mobil – and brings a second "
            "audience with it")

DATUM_DE = "25. November 2026"
DATUM_EN = "25 November 2026"

# Alle drei am 25.11.2026 abgerufen, jeweils HTTP 200. Die Play-Adresse
# bewusst OHNE den Anhang hl=de_AT: Der wuerde jedem Leser oesterreichisches
# Deutsch aufzwingen, waehrend der Store die Sprache sonst nach dem Konto
# des Betrachters waehlt.
BVKM_APP = "https://play.google.com/store/apps/details?id=de.appack.project.bvkm"
BVKM_LEICHT = "https://leichte-sprache.bvkm.de/topics/der-bvkm-hat-eine-app/"
BVKM_HOME = "https://bvkm.de/"

PLAY_DE = "https://play.google.com/store/apps/details?id=org.dialos.mobil"
DOWNLOAD_DE = "https://dialos.org/dialos-mobil/"
DOWNLOAD_EN = "https://dialos.org/en/dialos-mobil/"

# Verwandte Beitraege - auf dialos.org wird bei neuen Beitraegen bewusst auf
# thematisch passende aeltere verwiesen.
TESTBERICHT_DE = "https://dialos.org/dialos-mobil-zwei-rueckmeldungen-fuenf-fehler/"
TESTBERICHT_EN = "https://dialos.org/en/dialos-mobil-two-reports-five-bugs/"


def p(text):
    return f"<!-- wp:paragraph -->\n<p>{text}</p>\n<!-- /wp:paragraph -->"


def h(level, text):
    attrs = f' {{"level":{level}}}' if level != 2 else ""
    return ("<!-- wp:heading" + attrs + " -->\n"
            + f'<h{level} class="wp-block-heading">{text}</h{level}>\n'
            + "<!-- /wp:heading -->")


def sprachmarke(ziel, beschriftung):
    """Erster Block jedes Beitrags - daran erkennt der Blog-Abgleich die
    zusammengehoerenden Sprachfassungen. Die Klasse muss genau so heissen."""
    return ('<!-- wp:paragraph {"className":"dialos-lang-marker"} -->\n'
            f'<p class="dialos-lang-marker"><a href="{ziel}">{beschriftung}'
            "</a></p>\n<!-- /wp:paragraph -->")


def meta_en(post_id):
    """Englische Meta-Zeile; blendet die deutsche des Themes aus."""
    return ("<!-- wp:html -->\n<style>\n"
            f"body.postid-{post_id} p.meta {{ display: none; }}\n"
            "</style>\n"
            '<p class="meta"><i class="fa fa-clock-o"></i> '
            f"{DATUM_EN}\t\t\t\t\t\t"
            '<i class="fa fa-thumb-tack"></i> <strong>\t'
            '<a href="https://dialos.org/category/allgemein/" '
            'rel="category tag">General</a></strong></p>\n'
            "<!-- /wp:html -->")


# ---------------------------------------------------------------------------
# Deutscher Beitrag
# ---------------------------------------------------------------------------
INHALT_DE = "\n\n".join([
    sprachmarke(URL_EN, "English"),

    p("Der Bundesverband für körper- und mehrfachbehinderte Menschen hat "
      "DialOS Mobil in seine App <strong>bvkm aktiv</strong> aufgenommen, "
      "unter der Rubrik Tipps. Angefragt hatten wir Anfang Oktober; die "
      "Zusage kam sieben Wochen später."),

    p("Für dieses Projekt ist das eine Premiere, und zwar eine andere, als "
      "sie zunächst aussieht."),

    p("Bisher hat sich jeder, der von DialOS Mobil erfuhr, bei uns gemeldet "
      "oder wurde von uns angesprochen: die zwölf Testpersonen über ein "
      "Formular, die Rückmeldungen über unsere Kontaktadresse, die Leser "
      "einer Mailingliste über einen Beitrag, den wir selbst geschrieben "
      "haben. Jetzt empfiehlt zum ersten Mal <em>jemand anderes</em> die "
      "App – an Menschen, die uns nicht kennen."),

    h(2, "Warum gerade dieser Verband besonders ist"),

    p("DialOS Mobil ist für blinde Menschen gebaut worden. So ist sie "
      "gedacht, so ist sie beschrieben, und die allermeisten Rückmeldungen "
      "kamen bisher von Menschen, die den Bildschirm nicht sehen."),

    p("Der bvkm arbeitet aber mit Menschen, bei denen nicht das Sehen das "
      "Problem ist, sondern das Greifen. Und für die ist die App aus einem "
      "anderen Grund da."),

    p("Ein App-Symbol ist etwa so groß wie eine Fingerkuppe. Wer die Hand "
      "nicht ruhig führen kann, trifft es nicht zuverlässig – und je öfter "
      "man danebentippt, desto mehr passiert, was man nicht wollte. Deshalb "
      "gibt es in DialOS Mobil einen Balken für den Startbildschirm, der "
      "über die volle Breite geht. Einmal antippen genügt, und die App "
      "fragt sofort, wen sie anrufen soll. Danach läuft alles über die "
      "Stimme: Name sagen, mit Ja bestätigen, das Gespräch beginnt."),

    p("Dieser Balken ist nie das Hauptmerkmal gewesen. Für einen Teil der "
      "Menschen, die jetzt über bvkm aktiv von der App erfahren, ist er "
      "wahrscheinlich das Wichtigste daran."),

    h(2, "Was die App kann und was nicht"),

    p("Sie wählt auf Zuruf, aus den Kontakten oder nach einer gesprochenen "
      "Nummer. Die Spracherkennung läuft vollständig auf dem Gerät; die App "
      "hat nicht einmal die Berechtigung, ins Internet zu gehen. Kein "
      "Konto, keine Werbung, keine Auswertung, keine Kosten."),

    p("Sie ersetzt <strong>keinen Notruf</strong> – dafür bleibt die "
      "eingebaute Funktion des Telefons zuständig. Sie spricht und versteht "
      "nur Deutsch. Und es gibt sie bisher nur für Android."),

    p("Eine Grenze, die gerade für diese Zielgruppe zählt: Ein laufendes "
      "Gespräch lässt sich nicht per Sprache beenden. Sobald der Anruf "
      "steht, gehört das Mikrofon dem Telefon. Wer den roten Knopf schlecht "
      "trifft, kann aber in den Android-Einstellungen die Ein/Aus-Taste zum "
      "Auflegen umwidmen – eine Taste, die man ertasten kann."),

    p("Wie solche Grenzen gefunden werden, haben wir "
      f'<a href="{TESTBERICHT_DE}">an anderer Stelle beschrieben</a>: '
      "Fast jede Verbesserung der letzten Monate geht auf eine Rückmeldung "
      "zurück, und keine davon wäre uns allein aufgefallen."),

    h(2, "Die App des bvkm"),

    p(f'<a href="{BVKM_APP}">bvkm aktiv</a> ist kostenlos, in einfacher '
      "Sprache gehalten und bietet Informationen, Austausch und Beiträge "
      "rund um das Leben mit einer Körperbehinderung."),

    p("Der bvkm erklärt seine App auch "
      f'<a href="{BVKM_LEICHT}">in Leichter Sprache</a>. Mehr über den '
      f'Verband steht auf <a href="{BVKM_HOME}">bvkm.de</a>.'),

    h(2, "DialOS Mobil ausprobieren"),

    p(f'Im <a href="{PLAY_DE}">Google Play Store</a> – oder '
      f'<a href="{DOWNLOAD_DE}">direkt herunterladen</a>, ohne '
      "Google-Konto, mit Anleitung und Prüfsumme."),

    p("Wenn Sie die App benutzen und etwas nicht so läuft, wie Sie es "
      "erwarten: Schreiben Sie uns. Das ist keine Höflichkeitsformel – es "
      "ist der Weg, auf dem diese App besser geworden ist."),
])

AUSZUG_DE = ("Der Bundesverband für körper- und mehrfachbehinderte Menschen "
             "hat DialOS Mobil in seine App aufgenommen &#8211; der erste "
             "Kanal, über den die App Menschen erreicht, die uns nicht "
             "kennen. Und der erste, der eine zweite Zielgruppe mitbringt.")


# ---------------------------------------------------------------------------
# Englischer Beitrag
# ---------------------------------------------------------------------------
def inhalt_en(post_id):
    return "\n\n".join([
        sprachmarke(URL_DE, "Deutsch"),
        meta_en(post_id),

        p("The German federal association for people with physical and "
          "multiple disabilities (bvkm) has listed DialOS Mobil in its app "
          "<strong>bvkm aktiv</strong>, under “tips”. We wrote to them at "
          "the beginning of October; the answer came seven weeks later."),

        p("For this project that is a first – and a different one than it "
          "looks at first glance."),

        p("Until now, everyone who heard about DialOS Mobil either "
          "contacted us or was contacted by us: the twelve testers through "
          "a form, the feedback through our contact address, the readers of "
          "a mailing list through an article we wrote ourselves. Now, for "
          "the first time, <em>somebody else</em> is recommending the app – "
          "to people who do not know us."),

        h(2, "Why this particular association matters"),

        p("DialOS Mobil was built for blind people. That is how it is "
          "designed, how it is described, and nearly all feedback so far "
          "has come from people who cannot see the screen."),

        p("The bvkm, however, works with people whose difficulty is not "
          "seeing but gripping. For them the app exists for a different "
          "reason."),

        p("An app icon is about the size of a fingertip. Anyone who cannot "
          "hold their hand steady will not hit it reliably – and the more "
          "often you miss, the more happens that you did not intend. That "
          "is why DialOS Mobil offers a bar for the home screen that spans "
          "the full width. One tap is enough, and the app immediately asks "
          "who to call. Everything after that runs through the voice: say "
          "the name, confirm with yes, the call begins."),

        p("That bar has never been the headline feature. For some of the "
          "people now learning about the app through bvkm aktiv, it is "
          "probably the most important thing about it."),

        h(2, "What the app can and cannot do"),

        p("It dials on spoken command, from your contacts or from a spoken "
          "number. Recognition runs entirely on the device; the app does "
          "not even hold the permission to go online. No account, no ads, "
          "no analytics, no cost."),

        p("It is <strong>not a replacement for emergency calls</strong> – "
          "the phone's built-in function remains responsible for those. It "
          "speaks and understands German only. And so far it exists for "
          "Android only."),

        p("One limit that matters especially for this audience: a call in "
          "progress cannot be ended by voice. Once the call is up, the "
          "microphone belongs to the phone. Anyone who struggles to hit the "
          "red button can, however, repurpose the power button to hang up "
          "in the Android settings – a key you can feel."),

        p("How limits like these come to light we have "
          f'<a href="{TESTBERICHT_EN}">described elsewhere</a>: almost '
          "every improvement of recent months goes back to a piece of "
          "feedback, and none of them would have occurred to us alone."),

        h(2, "The bvkm's app"),

        p(f'<a href="{BVKM_APP}">bvkm aktiv</a> is free, written in plain '
          "language, and offers information, exchange and articles about "
          "living with a physical disability. The association explains its "
          f'app <a href="{BVKM_LEICHT}">in easy-read German</a> as well; '
          f'more about the association is at <a href="{BVKM_HOME}">bvkm.de'
          "</a>."),

        h(2, "Trying DialOS Mobil"),

        p(f'On the <a href="{PLAY_DE}">Google Play Store</a> – or '
          f'<a href="{DOWNLOAD_EN}">downloaded directly</a>, without a '
          "Google account, with instructions and a checksum."),

        p("If you use the app and something does not work the way you "
          "expect: write to us. That is not a politeness – it is the route "
          "by which this app got better."),
    ])


AUSZUG_EN = ("The German federal association for people with physical and "
             "multiple disabilities has listed DialOS Mobil in its app "
             "&#8211; the first channel reaching people who do not know us, "
             "and the first to bring a second audience with it.")


# ---------------------------------------------------------------------------
def schreibe_beitrag(slug, titel, inhalt, auszug, kommentare="open"):
    """kommentare: "open" fuer deutsche, "closed" fuer englische Beitraege.

    Die Website laeuft auf de_DE; unter einem englischen Beitrag kaemen
    sonst alle WordPress-Texte auf Deutsch heraus.
    """
    vorhanden = call("GET", f"wp/v2/posts?slug={slug}&status=any&context=edit")
    nutzlast = {"title": titel, "slug": slug, "content": inhalt,
                "excerpt": auszug, "status": "publish", "categories": [1],
                "comment_status": kommentare, "ping_status": kommentare}
    if vorhanden:
        beitrag = call("POST", f"wp/v2/posts/{vorhanden[0]['id']}", nutzlast)
        print(f"  aktualisiert: {beitrag['link']}")
    else:
        beitrag = call("POST", "wp/v2/posts", nutzlast)
        print(f"  angelegt: {beitrag['link']}")
    return beitrag


# ---------------------------------------------------------------------------
# Eintraege auf den Uebersichtsseiten
# ---------------------------------------------------------------------------
LISTE_AUF = '<div class="dialos-news-list">'


def eintrag(url, titel, datum, text):
    return ('<div class="dialos-news-entry">\n'
            f'<h3><a href="{url}">{titel}</a></h3>\n'
            f"<p><em>{datum}</em> — {text}</p>\n"
            "</div>")


def eintraege(inhalt):
    teile = inhalt.split('<div class="dialos-news-entry">')
    return ['<div class="dialos-news-entry">' + t.split("</div>", 1)[0]
            + "</div>" for t in teile[1:]]


def oben_einfuegen(inhalt, neuer):
    if LISTE_AUF not in inhalt:
        sys.exit("Die Eintragsliste wurde nicht gefunden - die Seite ist "
                 "offenbar umgebaut worden. Abbruch, damit nichts "
                 "zerschossen wird.")
    return inhalt.replace(LISTE_AUF, LISTE_AUF + "\n\n" + neuer, 1)


def ueberschuss_abschneiden(inhalt):
    alle = eintraege(inhalt)
    if len(alle) <= MAX_EINTRAEGE:
        return inhalt, []
    zuviel = alle[MAX_EINTRAEGE:]
    for alt in zuviel:
        inhalt = inhalt.replace("\n\n" + alt, "", 1)
        inhalt = inhalt.replace(alt, "", 1)
    return inhalt, zuviel


def pflege(news_id, archiv_id, url, neuer, name, schreiben):
    for seiten_id, wo in ((news_id, "Übersicht"), (archiv_id, "Archiv")):
        seite = call("GET", f"wp/v2/pages/{seiten_id}?context=edit")
        inhalt = seite["content"]["raw"]
        alt = next((e for e in eintraege(inhalt) if url in e), None)
        if alt is None:
            continue
        if alt.strip() == neuer.strip():
            print(f"  {name}: Eintrag steht schon aktuell in der {wo}.")
            return
        if schreiben:
            call("POST", f"wp/v2/pages/{seiten_id}",
                 {"content": inhalt.replace(alt, neuer, 1)})
        print(f"  {name}: Eintrag in der {wo} aufgefrischt"
              f"{'' if schreiben else ' (Probelauf)'}.")
        return

    seite = call("GET", f"wp/v2/pages/{news_id}?context=edit")
    inhalt = seite["content"]["raw"]
    inhalt = oben_einfuegen(inhalt, neuer)
    inhalt, verdraengt = ueberschuss_abschneiden(inhalt)
    if schreiben:
        call("POST", f"wp/v2/pages/{news_id}", {"content": inhalt})
    print(f"  {name}: Eintrag oben eingefuegt"
          f"{'' if schreiben else ' (Probelauf)'}.")

    if not verdraengt:
        return
    archiv = call("GET", f"wp/v2/pages/{archiv_id}?context=edit")
    archiv_inhalt = archiv["content"]["raw"]
    for alt in verdraengt:
        archiv_inhalt = oben_einfuegen(archiv_inhalt, alt)
    if schreiben:
        call("POST", f"wp/v2/pages/{archiv_id}", {"content": archiv_inhalt})
    print(f"  {name}: {len(verdraengt)} Eintrag/Eintraege ins Archiv "
          f"verschoben{'' if schreiben else ' (Probelauf)'}.")


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--schreiben", action="store_true",
                    help="ohne diesen Schalter nur Probelauf")
    args = ap.parse_args()

    if not args.schreiben:
        print("PROBELAUF - es wird nichts veröffentlicht.\n")
        print(f"Deutsch:  {TITEL_DE}\n          {URL_DE}")
        print(f"          {len(INHALT_DE)} Zeichen Inhalt")
        print(f"Englisch: {TITEL_EN}\n          {URL_EN}")
        print(f"          {len(inhalt_en(0))} Zeichen Inhalt")
        print()
        pflege(NEWS_DE, ARCHIV_DE, URL_DE,
               eintrag(URL_DE, TITEL_DE, DATUM_DE, EINTRAG_DE), "deutsch",
               False)
        pflege(NEWS_EN, ARCHIV_EN, URL_EN,
               eintrag(URL_EN, TITEL_EN, DATUM_EN, EINTRAG_EN), "englisch",
               False)
        return

    print("Deutscher Beitrag:")
    schreibe_beitrag(SLUG_DE, TITEL_DE, INHALT_DE, AUSZUG_DE)

    print("Englischer Beitrag:")
    # Erst anlegen (ohne Meta-Zeile), um die ID zu bekommen ...
    en = schreibe_beitrag(SLUG_EN, TITEL_EN, inhalt_en(0), AUSZUG_EN,
                          kommentare="closed")
    # ... dann mit eingesetzter ID nachziehen, damit das CSS greift.
    en = call("POST", f"wp/v2/posts/{en['id']}",
              {"content": inhalt_en(en["id"])})
    print(f"  Meta-Zeile auf postid-{en['id']} gesetzt")

    print("Uebersichtsseiten:")
    pflege(NEWS_DE, ARCHIV_DE, URL_DE,
           eintrag(URL_DE, TITEL_DE, DATUM_DE, EINTRAG_DE), "deutsch", True)
    pflege(NEWS_EN, ARCHIV_EN, URL_EN,
           eintrag(URL_EN, TITEL_EN, DATUM_EN, EINTRAG_EN), "englisch", True)


EINTRAG_DE = ("Der Bundesverband für körper- und mehrfachbehinderte Menschen "
              "empfiehlt die App &#8211; und bringt eine Zielgruppe mit, für "
              "die nicht das Sehen das Problem ist, sondern das Greifen.")
EINTRAG_EN = ("The German association for people with physical disabilities "
              "recommends the app &#8211; bringing an audience whose "
              "difficulty is not seeing but gripping.")


if __name__ == "__main__":
    main()
