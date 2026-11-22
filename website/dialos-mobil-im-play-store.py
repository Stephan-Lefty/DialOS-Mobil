#!/usr/bin/env python3
"""Veroeffentlicht den Launch-Beitrag zu DialOS Mobil auf dialos.org.

DialOS Mobil ist seit dem 18.11.2026 oeffentlich im Google Play Store. Dieses
Skript legt an bzw. aktualisiert (mehrfach ausfuehrbar):

  1. deutscher Beitrag  /dialos-mobil-im-play-store/
  2. englischer Beitrag /en/dialos-mobil-in-the-play-store/
  3. Eintraege oben auf /neuigkeiten/ (184) und /en/news/ (363)

Aufbau und Konventionen 1:1 aus dialos-mobil-testbericht.py uebernommen:
Sprachumschalter als erster Absatz, englischer Beitrag ohne Kommentare und
mit eigener Meta-Zeile (deshalb wird er zweimal geschrieben).
"""
import sys

from wp_zugang import call

NEWS_DE, ARCHIV_DE = 184, 258
NEWS_EN, ARCHIV_EN = 363, 364
MAX_EINTRAEGE = 5

STORE = "https://play.google.com/store/apps/details?id=org.dialos.mobil"

SLUG_DE = "dialos-mobil-im-play-store"
SLUG_EN = "dialos-mobil-in-the-play-store"
URL_DE = f"https://dialos.org/{SLUG_DE}/"
URL_EN = f"https://dialos.org/en/{SLUG_EN}/"

TITEL_DE = "DialOS Mobil ist im Play Store"
TITEL_EN = "DialOS Mobil Is in the Play Store"

DATUM_DE = "22. November 2026"
DATUM_EN = "22 November 2026"

# Verwandte Beitraege/Seiten - bewusst verlinkt statt nebeneinandergestellt.
MOBIL_DE = "https://dialos.org/dialos-mobil/"
MOBIL_EN = "https://dialos.org/en/dialos-mobil/"
TEST_DE = "https://dialos.org/dialos-mobil-zwei-rueckmeldungen-fuenf-fehler/"
TEST_EN = "https://dialos.org/en/dialos-mobil-two-reports-five-bugs/"


def p(text):
    return f"<!-- wp:paragraph -->\n<p>{text}</p>\n<!-- /wp:paragraph -->"


def h(level, text):
    attrs = f' {{"level":{level}}}' if level != 2 else ""
    return ("<!-- wp:heading" + attrs + " -->\n"
            + f'<h{level} class="wp-block-heading">{text}</h{level}>\n'
            + "<!-- /wp:heading -->")


def button(beschriftung, ziel):
    return ("<!-- wp:buttons -->\n"
            '<div class="wp-block-buttons">\n'
            "<!-- wp:button -->\n"
            '<div class="wp-block-button"><a class="wp-block-button__link '
            f'wp-element-button" href="{ziel}">{beschriftung}</a></div>\n'
            "<!-- /wp:button -->\n"
            "</div>\n"
            "<!-- /wp:buttons -->")


def meta_en(post_id):
    return ("<!-- wp:html -->\n<style>\n"
            f"body.postid-{post_id} p.meta {{ display: none; }}\n"
            "</style>\n"
            '<p class="meta"><i class="fa fa-clock-o"></i> '
            f"{DATUM_EN}\t\t\t\t\t\t"
            '<i class="fa fa-thumb-tack"></i> <strong>\t'
            '<a href="https://dialos.org/category/allgemein/" '
            'rel="category tag">General</a></strong></p>\n'
            "<!-- /wp:html -->")


def sprachmarke(ziel, beschriftung):
    return ('<!-- wp:paragraph {"className":"dialos-lang-marker"} -->\n'
            f'<p class="dialos-lang-marker"><a href="{ziel}">{beschriftung}'
            "</a></p>\n<!-- /wp:paragraph -->")


# ---------------------------------------------------------------------------
# Deutscher Beitrag
# ---------------------------------------------------------------------------
INHALT_DE = "\n\n".join([
    sprachmarke(URL_EN, "English"),

    p("<em>DialOS Mobil ist jetzt öffentlich im Google Play Store &#8211; die "
      "App, mit der man allein durch Sprechen telefonieren kann. Kostenlos, "
      "ohne Werbung, und ohne dass eine Sprachaufnahme das Telefon "
      "verlässt.</em>"),

    h(2, "Der Weg dorthin"),
    p("Hinter dem einen „Installieren“-Knopf liegt ein längerer Weg, als man "
      "ihm ansieht. Google verlangt von neuen Entwicklerkonten einen "
      "geschlossenen Test, bevor eine App öffentlich werden darf: zwölf "
      "Menschen, die sie vierzehn Tage lang installiert lassen. Zwölf "
      "Testerinnen und Tester zu finden, die blind sind oder die Hände nicht "
      "ruhig genug führen, um ein Telefon zu bedienen &#8211; und die dann "
      "auch noch einen Einladungslink am Handy bestätigen &#8211;, hat Wochen "
      "gedauert."),

    p("Ihnen gehört der größte Dank. Jede ihrer Rückmeldungen hat die App "
      "besser gemacht, oft an Stellen, die beim eigenen Testen unsichtbar "
      "bleiben. Wie das konkret aussah, steht im Beitrag "
      f'<a href="{TEST_DE}">Zwei Rückmeldungen, fünf Fehler</a> &#8211; dort '
      "wird greifbar, warum echte Menschen am Gerät durch nichts zu ersetzen "
      "sind."),

    h(2, "Die letzte Hürde"),
    p("Danach kam Googles Prüfung für den öffentlichen Zugang, und die hatte "
      "eine eingebaute Tücke. DialOS Mobil ist ein Dienst, der im Hintergrund "
      "auf das Aktivierungswort wartet &#8211; man öffnet die App im Alltag "
      "fast nie. Genau das wertet Googles Statistik aber leicht als „zu wenig "
      "genutzt“. Dass die seltene Öffnung kein Mangel, sondern der Sinn der "
      "App ist, stand darum ausdrücklich im Antrag &#8211; und hat getragen. "
      "Seit dem 18. November ist DialOS Mobil für alle da."),

    h(2, "Worum es geht"),
    p("Wer blind ist oder die Hände nicht ruhig führt, scheitert am "
      "Touchscreen &#8211; nicht am Telefonieren. DialOS Mobil dreht das um: "
      "Man sagt, wen man anrufen möchte, und die App ruft an. Kein Tippen, "
      "kein Zielen, kein Hinsehen. Die Spracherkennung läuft vollständig auf "
      "dem Gerät; die App hat nicht einmal die Berechtigung, ins Internet zu "
      "gehen."),

    p("Wie ein Anruf abläuft, was die App im Detail kann und wie man sie "
      f'einrichtet, steht auf der <a href="{MOBIL_DE}">Seite zu DialOS '
      "Mobil</a>."),

    button("Jetzt bei Google Play", STORE),

    p("Eine Bitte zum Schluss: Wenn Sie jemanden kennen, der ein Telefon "
      "nicht mehr bedienen kann, erzählen Sie ihm davon. Für genau diese "
      "Menschen ist die App gebaut."),
])

AUSZUG_DE = ("DialOS Mobil ist öffentlich im Google Play Store. Der Weg vom "
             "geschlossenen Test mit zwölf Personen über Googles Prüfung bis "
             "zur Freigabe &#8211; und worum es bei der App geht.")


# ---------------------------------------------------------------------------
# Englischer Beitrag
# ---------------------------------------------------------------------------
def inhalt_en(post_id):
    return "\n\n".join([
        meta_en(post_id),
        sprachmarke(URL_DE, "Deutsch"),

        p("<em>DialOS Mobil is now publicly available on the Google Play Store "
          "&#8211; the app that lets you make phone calls using your voice "
          "alone. Free, no ads, and no voice recording ever leaves the "
          "phone.</em>"),

        h(2, "The road here"),
        p("Behind that single “Install” button lies a longer road than it "
          "suggests. Google requires new developer accounts to run a closed "
          "test before an app may go public: twelve people who keep it "
          "installed for fourteen days. Finding twelve testers who are blind, "
          "or whose hands aren’t steady enough to operate a phone &#8211; and "
          "who then confirm an invitation link on that phone &#8211; took "
          "weeks."),

        p("They deserve the greatest thanks. Every piece of their feedback "
          "made the app better, often in places that stay invisible when you "
          f'only test it yourself. What that looked like is in the post '
          f'<a href="{TEST_EN}">Two reports, five bugs</a> &#8211; it makes '
          "tangible why real people on real devices cannot be replaced by "
          "anything else."),

        h(2, "The last hurdle"),
        p("Then came Google’s review for public access, with a built-in catch. "
          "DialOS Mobil is a service that waits in the background for its wake "
          "word &#8211; you almost never open it in daily use. Google’s "
          "statistics can easily read that as “not used enough”. That the rare "
          "opening is not a flaw but the whole point was therefore stated "
          "explicitly in the application &#8211; and it held. Since 18 "
          "November, DialOS Mobil is there for everyone."),

        h(2, "What it’s about"),
        p("If you’re blind or your hands aren’t steady, you fail at the "
          "touchscreen &#8211; not at making calls. DialOS Mobil turns that "
          "around: you say who you want to call, and the app calls. No typing, "
          "no aiming, no looking. Speech recognition runs entirely on the "
          "device; the app doesn’t even have permission to go online."),

        p("How a call works, what the app can do in detail and how to set it "
          f'up is on the <a href="{MOBIL_EN}">DialOS Mobil page</a>.'),

        button("Get it on Google Play", STORE),

        p("And one request to close: if you know someone who can no longer "
          "operate a phone, tell them about it. This app was built for exactly "
          "those people."),
    ])


AUSZUG_EN = ("DialOS Mobil is publicly available on the Google Play Store. The "
             "road from the closed test with twelve people, through Google’s "
             "review, to release &#8211; and what the app is about.")


# ---------------------------------------------------------------------------
def schreibe_beitrag(slug, titel, inhalt, auszug, kommentare="open"):
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


print("Deutscher Beitrag:")
de = schreibe_beitrag(SLUG_DE, TITEL_DE, INHALT_DE, AUSZUG_DE)

print("Englischer Beitrag:")
en = schreibe_beitrag(SLUG_EN, TITEL_EN, inhalt_en(0), AUSZUG_EN,
                      kommentare="closed")
en = call("POST", f"wp/v2/posts/{en['id']}", {"content": inhalt_en(en["id"])})
print(f"  Meta-Zeile auf postid-{en['id']} gesetzt")

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
        sys.exit("Die Eintragsliste wurde nicht gefunden - Abbruch.")
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


def pflege(news_id, archiv_id, url, neuer, name):
    for seiten_id, wo in ((news_id, "Übersicht"), (archiv_id, "Archiv")):
        seite = call("GET", f"wp/v2/pages/{seiten_id}?context=edit")
        inhalt = seite["content"]["raw"]
        alt = next((e for e in eintraege(inhalt) if url in e), None)
        if alt is None:
            continue
        if alt.strip() == neuer.strip():
            print(f"  {name}: Eintrag steht schon aktuell in der {wo}.")
            return
        call("POST", f"wp/v2/pages/{seiten_id}",
             {"content": inhalt.replace(alt, neuer, 1)})
        print(f"  {name}: Eintrag in der {wo} aufgefrischt.")
        return

    seite = call("GET", f"wp/v2/pages/{news_id}?context=edit")
    inhalt = seite["content"]["raw"]
    inhalt = oben_einfuegen(inhalt, neuer)
    inhalt, verdraengt = ueberschuss_abschneiden(inhalt)
    call("POST", f"wp/v2/pages/{news_id}", {"content": inhalt})
    print(f"  {name}: Eintrag oben eingefuegt.")

    if not verdraengt:
        return
    archiv = call("GET", f"wp/v2/pages/{archiv_id}?context=edit")
    archiv_inhalt = archiv["content"]["raw"]
    for alt in verdraengt:
        archiv_inhalt = oben_einfuegen(archiv_inhalt, alt)
    call("POST", f"wp/v2/pages/{archiv_id}", {"content": archiv_inhalt})
    print(f"  {name}: {len(verdraengt)} Eintrag/Eintraege ins Archiv "
          "verschoben.")


print("Uebersichtsseiten:")
pflege(NEWS_DE, ARCHIV_DE, URL_DE,
       eintrag(URL_DE, TITEL_DE, DATUM_DE,
               "DialOS Mobil ist öffentlich im Play Store &#8211; der Weg vom "
               "geschlossenen Test bis zur Freigabe, und worum es geht."),
       "deutsch")
pflege(NEWS_EN, ARCHIV_EN, URL_EN,
       eintrag(URL_EN, TITEL_EN, DATUM_EN,
               "DialOS Mobil is public on the Play Store &#8211; the road from "
               "the closed test to release, and what it’s about."),
       "englisch")

print(f"\nFertig.\n  DE: {URL_DE}\n  EN: {URL_EN}")
