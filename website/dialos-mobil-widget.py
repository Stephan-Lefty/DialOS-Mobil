#!/usr/bin/env python3
"""Veroeffentlicht den Beitrag ueber das Startbildschirm-Widget.

Legt an bzw. aktualisiert (mehrfach ausfuehrbar):

  1. die zwei Widget-Screenshots in der Mediathek
  2. deutscher Beitrag  /dialos-mobil-ein-knopf-so-breit-wie-der-bildschirm/
  3. englischer Beitrag /en/dialos-mobil-a-button-as-wide-as-the-screen/
  4. Eintraege ganz oben auf /neuigkeiten/ (184) und /en/news/ (363),
     mit Ueberlauf ins jeweilige Archiv (258 bzw. 364)

Aufbau wie dialos-mobil-diktieren.py - dort steht die Begruendung der
Konventionen. Kurzfassung: Sprachmarke als erster Block, englischer Beitrag
mit eigener Meta-Zeile davor und ohne Kommentare, hoechstens fuenf
Eintraege je Uebersichtsseite.
"""
import json
import mimetypes
import sys
import urllib.request
from pathlib import Path

from wp_zugang import AUTH, WP, call

REPO = Path(__file__).resolve().parent.parent

NEWS_DE, ARCHIV_DE = 184, 258
NEWS_EN, ARCHIV_EN = 363, 364
MAX_EINTRAEGE = 5

REPO_URL = "https://github.com/Stephan-Lefty/DialOS-Mobil"

SLUG_DE = "dialos-mobil-ein-knopf-so-breit-wie-der-bildschirm"
SLUG_EN = "dialos-mobil-a-button-as-wide-as-the-screen"
URL_DE = f"https://dialos.org/{SLUG_DE}/"
URL_EN = f"https://dialos.org/en/{SLUG_EN}/"

TITEL_DE = "DialOS Mobil: Ein Knopf, so breit wie der Bildschirm"
TITEL_EN = "DialOS Mobil: A button as wide as the screen"

DATUM_DE = "9. September 2026"
DATUM_EN = "9 September 2026"

# Verwandte Beitraege
DIKTIEREN_DE = "https://dialos.org/dialos-mobil-die-app-die-sich-selbst-taub-machte/"
DIKTIEREN_EN = "https://dialos.org/en/dialos-mobil-the-app-that-deafened-itself/"
SCHWEIGEN_DE = "https://dialos.org/das-schweigende-missverstaendnis/"
SCHWEIGEN_EN = "https://dialos.org/en/the-silent-misunderstanding/"
AUFRUF_DE = "https://dialos.org/dialos-mobil-tester-gesucht/"
AUFRUF_EN = "https://dialos.org/en/dialos-mobil-testers-wanted/"

BILDER = [
    (REPO / "screenshots" / "widget" / "05_widget_aus.png",
     "Startbildschirm eines Android-Handys. Über die volle Breite ein "
     "blauer Balken mit großer weißer Schrift: „Antippen zum Einschalten“, "
     "darunter kleiner „Ausgeschaltet“."),
    (REPO / "screenshots" / "widget" / "06_widget_an.png",
     "Derselbe Balken in Grün: „Antippen und sprechen“, darunter "
     "„Sprachsteuerung bereit. Wen möchten Sie anrufen?“."),
]


def upload(path: Path, alt: str):
    """Laedt ein Bild hoch - oder gibt das schon vorhandene zurueck."""
    slug = path.stem.replace("_", "-")
    vorhanden = call("GET", f"wp/v2/media?slug={slug}&context=edit")
    if vorhanden:
        print(f"  vorhanden: {path.name} -> id {vorhanden[0]['id']}")
        return vorhanden[0]

    if not path.is_file():
        sys.exit(f"Bild fehlt: {path}")

    body = path.read_bytes()
    req = urllib.request.Request(f"{WP}/wp-json/wp/v2/media", data=body,
                                 method="POST")
    req.add_header("Authorization", f"Basic {AUTH}")
    req.add_header("Content-Type", mimetypes.guess_type(path.name)[0])
    req.add_header("Content-Disposition", f'attachment; filename="{path.name}"')
    with urllib.request.urlopen(req, timeout=180) as r:
        media = json.loads(r.read().decode())
    # Alternativtext nachtragen - beim Upload nimmt WordPress ihn nicht an.
    media = call("POST", f"wp/v2/media/{media['id']}",
                 {"alt_text": alt, "slug": slug})
    print(f"  hochgeladen: {path.name} -> id {media['id']}")
    return media


print("Bilder:")
BILD_AUS, BILD_AN = (upload(p, a) for p, a in BILDER)


def p(text):
    return f"<!-- wp:paragraph -->\n<p>{text}</p>\n<!-- /wp:paragraph -->"


def h(level, text):
    attrs = f' {{"level":{level}}}' if level != 2 else ""
    return ("<!-- wp:heading" + attrs + " -->\n"
            + f'<h{level} class="wp-block-heading">{text}</h{level}>\n'
            + "<!-- /wp:heading -->")


def bild(media, beschriftung):
    url = media["source_url"]
    return (f'<!-- wp:image {{"id":{media["id"]},"sizeSlug":"large",'
            '"linkDestination":"media","align":"center","width":"300px"} -->\n'
            '<figure class="wp-block-image aligncenter size-large '
            'is-resized">'
            f'<a href="{url}"><img src="{url}" alt="{media["alt_text"]}" '
            f'class="wp-image-{media["id"]}" style="width:300px"/></a>'
            f'<figcaption class="wp-element-caption">{beschriftung}</figcaption>'
            "</figure>\n<!-- /wp:image -->")


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
INHALT_DE = "\n\n".join([
    sprachmarke(URL_EN, "English"),

    p("<em>DialOS Mobil hat jetzt ein Widget für den Startbildschirm. Es ist "
      "unverschämt groß, und es hat mich eine Lektion darüber gekostet, "
      "wofür Text eigentlich da ist.</em>"),

    h(2, "Warum überhaupt ein Widget"),
    p("Ein App-Symbol ist etwa einen Zentimeter groß und liegt zwischen "
      "zwanzig anderen, die genauso aussehen. Für die meisten Menschen ist "
      "das kein Problem. Für jemanden, der die Hände nicht ruhig führen kann "
      "oder nur noch Umrisse erkennt, ist es <strong>kein brauchbares "
      "Ziel</strong>."),

    p("Deshalb gibt es das Widget: ein Balken über die volle "
      "Bildschirmbreite, den man auch mit zittriger Hand trifft und den man "
      "nicht zwischen anderen Symbolen suchen muss. Ein Tippen schaltet die "
      "Sprachsteuerung ein und fragt sofort „Wen möchten Sie anrufen?“ "
      "&#8211; es öffnet nicht bloß die App."),

    bild(BILD_AUS, "Ausgeschaltet. Der Balken nimmt die volle Breite ein."),

    p("Nebenbei löst es ein zweites Problem: Vorher war <strong>nirgends auf "
      "einen Blick zu sehen, ob die Sprachsteuerung überhaupt läuft</strong>. "
      "Das stand nur in der Benachrichtigungsleiste, die man erst "
      "herunterziehen muss. Jetzt sieht man es, ohne etwas zu tun."),

    bild(BILD_AN, "Eingeschaltet. Farbe und Text wechseln gemeinsam."),

    p("Der Zustand wechselt über <strong>Farbe und Text zugleich</strong>. "
      "Nur die Farbe zu ändern wäre bequemer gewesen, hätte aber alle "
      "ausgeschlossen, die Farben schlecht unterscheiden &#8211; und das "
      "sind in dieser Zielgruppe nicht wenige."),

    h(2, "Und dann die Frage, die alles umwarf"),
    p("Ich hatte das Widget gebaut, auf dem Telefon geprüft, den Screenshot "
      "verschickt. Im eingeschalteten Zustand stand darauf: <strong>„Jetzt "
      "sprechen“</strong>. Klingt gut, dachte ich."),

    p("Die Rückfrage kam prompt: <em>Macht es Sinn, „Jetzt sprechen“ "
      "hinzuschreiben, wenn der Nutzer doch nicht sehen kann?</em>"),

    p("Zwei Dinge stimmten daran nicht, und das zweite war schlimmer."),

    h(3, "Erstens: Blinde Nutzer lesen diesen Text gar nicht"),
    p("Sie hören ihn. Android liest über die Bildschirmvorlesefunktion "
      "TalkBack nicht den sichtbaren Text vor, sondern eine eigene "
      "Beschreibung, die im Programm hinterlegt ist. Die war schon da "
      "&#8211; sie ist jetzt ausführlicher und nennt beide Wege: antippen "
      "oder das Aktivierungswort sagen."),

    p("Der sichtbare Text ist also gar nicht für Blinde gedacht. Er ist für "
      "die andere Hälfte der Zielgruppe: Menschen mit Sehrest, die große "
      "kontrastreiche Schrift durchaus lesen können, aber kein winziges "
      "Symbol treffen. <strong>Barrierefreiheit ist keine Gruppe, sondern "
      "viele</strong>, und was der einen hilft, sieht die andere nie."),

    h(3, "Zweitens: „Jetzt sprechen“ war schlicht falsch"),
    p("Das ist der Teil, der mir unangenehm ist. Wenn die Sprachsteuerung "
      "läuft, kann man <strong>nicht</strong> einfach lossprechen. Man muss "
      "erst das Aktivierungswort sagen oder das Widget antippen. „Jetzt "
      "sprechen“ hat etwas versprochen, das die App nicht hält."),

    p("Und die Zeile darunter widersprach ihm im selben Atemzug: „Ich höre "
      "auf Sprachsteuerung starten.“ Zwei Aussagen übereinander, die "
      "einander ausschließen. Wer danach nichts sagt und wartet, wartet "
      "vergeblich."),

    p("Jetzt steht dort eine <strong>Handlungsaufforderung statt einer "
      "Zustandsmeldung</strong>: „Antippen zum Einschalten“ und „Antippen "
      "und sprechen“. Man muss nicht wissen, in welchem Zustand die App ist "
      "&#8211; man muss wissen, was man tun soll."),

    h(2, "Was dabei noch herausfiel"),
    p("Weil das Telefon ohnehin am Rechner hing, habe ich auch die neue "
      "Erkennung geprüft, die merkt, wenn Android die App abgeräumt hat. Sie "
      "funktioniert &#8211; aber der Zähler dazu stand auf 1, bevor "
      "überhaupt etwas passiert war."),

    p("Der Grund war ich selbst: Beim Aufspielen einer neuen Fassung wird "
      "die App beendet und neu gestartet. Für die Erkennung sah das aus wie "
      "ein Abschuss. Technisch richtig &#8211; praktisch wertlos, denn der "
      "Zähler soll ja gerade die Frage beantworten, ob das <em>Gerät</em> "
      "die App abräumt. Zählt er jedes Update mit, kann man ihn wegwerfen. "
      "Auch das ist behoben."),

    h(2, "Die Lehre"),
    p("Ich habe den Widget-Text geschrieben, ihn angesehen, für gut befunden "
      "und veröffentlicht. Gebraucht hat es eine einzige Frage von außen, um "
      "zu sehen, dass er weder für die einen taugte noch für die anderen "
      "stimmte."),

    p("Das ist inzwischen ein Muster in diesem Projekt. Beim Diktieren war "
      f'es <a href="{DIKTIEREN_DE}">eine App, die sich selbst taub '
      "machte</a>; bei der Wartezeit "
      f'<a href="{SCHWEIGEN_DE}">eine Ansage, die etwas anderes sagte, als '
      "geschah</a>. Immer dasselbe: <strong>Ich prüfe gegen das, was ich "
      "gemeint habe, nicht gegen das, was jemand vorfindet.</strong>"),

    p("Der geschlossene Test läuft übrigens noch, und es fehlen weiterhin "
      f'Menschen. Wer mitmachen möchte: <a href="{AUFRUF_DE}">hier steht, '
      "wie es geht</a>. Man braucht ein Android-Handy mit SIM-Karte und "
      "vierzehn Tage Geduld &#8211; und muss weder blind sein noch etwas "
      "von Technik verstehen. Im Gegenteil."),

    p("Der Quelltext liegt offen: "
      f'<a href="{REPO_URL}">github.com/Stephan-Lefty/DialOS-Mobil</a>.'),
])

AUSZUG_DE = ("Das neue Widget ist ein Balken über die volle Bildschirmbreite "
             "&#8211; und der Text darauf war falsch. Eine einzige Rückfrage "
             "hat es gezeigt.")


def inhalt_en(post_id):
    return "\n\n".join([
        meta_en(post_id),
        sprachmarke(URL_DE, "Deutsch"),

        p("<em>DialOS Mobil now has a home screen widget. It is outrageously "
          "large, and it cost me a lesson about what text is actually "
          "for.</em>"),

        h(2, "Why a widget at all"),
        p("An app icon is about a centimetre across and sits among twenty "
          "others that look just like it. For most people that is no "
          "problem. For someone whose hands shake, or who only makes out "
          "shapes, it is <strong>not a usable target</strong>."),

        p("Hence the widget: a bar spanning the full screen width that you "
          "can hit with an unsteady hand and do not have to find among other "
          "icons. One tap turns voice control on and immediately asks „Wen "
          "möchten Sie anrufen?“ &#8211; it does not merely open the app."),

        bild(BILD_AUS, "Off. The bar takes the full width."),

        p("It also solves a second problem: previously there was "
          "<strong>nowhere to see at a glance whether voice control was "
          "running at all</strong>. That lived only in the notification "
          "shade, which you have to pull down first. Now you see it without "
          "doing anything."),

        bild(BILD_AN, "On. Colour and text change together."),

        p("The state changes through <strong>colour and text at once</strong>. "
          "Changing only the colour would have been easier, but it would "
          "have excluded everyone who cannot distinguish colours well "
          "&#8211; and in this audience that is not a small group."),

        h(2, "And then the question that upended it"),
        p("I had built the widget, checked it on the phone, sent the "
          "screenshot. In the running state it read: <strong>„Jetzt "
          "sprechen“</strong> (speak now). Sounds good, I thought."),

        p("The reply came straight back: <em>Does it make sense to write "
          "„speak now“ when the user cannot see it?</em>"),

        p("Two things were wrong with it, and the second was worse."),

        h(3, "First: blind users do not read that text at all"),
        p("They hear it. Through the TalkBack screen reader, Android does "
          "not read out the visible text but a separate description stored "
          "in the program. That was already there &#8211; it is now fuller "
          "and names both routes: tap, or say the wake phrase."),

        p("So the visible text is not for blind users at all. It is for the "
          "other half of the audience: people with residual vision who can "
          "read large high-contrast type perfectly well but cannot hit a "
          "tiny icon. <strong>Accessibility is not one group but many</strong>, "
          "and what helps one is never seen by the other."),

        h(3, "Second: „speak now“ was simply untrue"),
        p("This is the part I find uncomfortable. When voice control is "
          "running you <strong>cannot</strong> just start speaking. You have "
          "to say the wake phrase first, or tap the widget. „Speak now“ "
          "promised something the app does not deliver."),

        p("And the line beneath contradicted it in the same breath: „I am "
          "listening for: start voice control.“ Two statements stacked on "
          "top of each other that rule each other out. Anyone who then says "
          "nothing and waits, waits in vain."),

        p("It now shows a <strong>call to action rather than a status "
          "report</strong>: „Antippen zum Einschalten“ (tap to turn on) and "
          "„Antippen und sprechen“ (tap and speak). You do not need to know "
          "which state the app is in &#8211; you need to know what to do."),

        h(2, "What else fell out of it"),
        p("Since the phone was plugged in anyway, I also checked the new "
          "detection that notices when Android has torn the app down. It "
          "works &#8211; but its counter stood at 1 before anything had "
          "happened at all."),

        p("The cause was me: installing a new build stops and restarts the "
          "app. To the detection that looked like a kill. Technically "
          "correct &#8211; practically useless, because the counter exists "
          "to answer whether the <em>device</em> is tearing the app down. If "
          "it counts every update, you may as well throw it away. Fixed too."),

        h(2, "The lesson"),
        p("I wrote the widget text, looked at it, found it good and shipped "
          "it. It took a single question from outside to reveal that it "
          "served neither group and was untrue besides."),

        p("By now that is a pattern in this project. With dictation it was "
          f'<a href="{DIKTIEREN_EN}">an app that deafened itself</a>; with '
          f'the timeout it was <a href="{SCHWEIGEN_EN}">an announcement that '
          "said something other than what happened</a>. Always the same "
          "thing: <strong>I check against what I meant, not against what "
          "someone actually encounters.</strong>"),

        p("The closed test is still running, by the way, and people are "
          f'still missing. If you would like to join: <a href="{AUFRUF_EN}">'
          "here is how</a>. You need an Android phone with a SIM card and "
          "fourteen days of patience &#8211; and you need to be neither "
          "blind nor technical. Quite the opposite."),

        p("The source is open: "
          f'<a href="{REPO_URL}">github.com/Stephan-Lefty/DialOS-Mobil</a>.'),
    ])


AUSZUG_EN = ("The new widget is a bar spanning the full screen width "
             "&#8211; and the text on it was wrong. A single question "
             "revealed it.")


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
                 "offenbar umgebaut worden. Abbruch.")
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
    """Traegt den Eintrag oben ein - oder frischt ihn auf, wenn er schon da
    ist. Auffrischen statt Ueberspringen, damit ein geaenderter Titel auch
    in der Uebersicht ankommt."""
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


print("Deutscher Beitrag:")
de = schreibe_beitrag(SLUG_DE, TITEL_DE, INHALT_DE, AUSZUG_DE)

print("Englischer Beitrag:")
en = schreibe_beitrag(SLUG_EN, TITEL_EN, inhalt_en(0), AUSZUG_EN,
                      kommentare="closed")
en = call("POST", f"wp/v2/posts/{en['id']}", {"content": inhalt_en(en["id"])})
print(f"  Meta-Zeile auf postid-{en['id']} gesetzt")

print("Uebersichtsseiten:")
pflege(NEWS_DE, ARCHIV_DE, URL_DE,
       eintrag(URL_DE, TITEL_DE, DATUM_DE,
               "Ein Balken über die volle Bildschirmbreite &#8211; und der "
               "Text darauf war falsch. Eine einzige Rückfrage hat es "
               "gezeigt."),
       "deutsch")
pflege(NEWS_EN, ARCHIV_EN, URL_EN,
       eintrag(URL_EN, TITEL_EN, DATUM_EN,
               "A bar spanning the full screen width &#8211; and the text on "
               "it was wrong. A single question revealed it."),
       "englisch")

print(f"\nFertig.\n  DE: {URL_DE}\n  EN: {URL_EN}")
