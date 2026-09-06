#!/usr/bin/env python3
"""Veroeffentlicht den Beitrag ueber das Diktieren von Rufnummern.

Legt an bzw. aktualisiert (mehrfach ausfuehrbar):

  1. deutscher Beitrag  /dialos-mobil-die-app-die-sich-selbst-taub-machte/
  2. englischer Beitrag /en/dialos-mobil-the-app-that-deafened-itself/
  3. Eintraege ganz oben auf /neuigkeiten/ (184) und /en/news/ (363),
     mit Ueberlauf ins jeweilige Archiv (258 bzw. 364)

Aufbau wie dialos-mobil-testbericht.py - dort steht die Begruendung der
Konventionen. Kurzfassung: Sprachmarke als erster Block (der taegliche
Blog-Abgleich erkennt daran die Sprachpaare), englischer Beitrag mit
eigener Meta-Zeile davor und ohne Kommentare, hoechstens fuenf Eintraege je
Uebersichtsseite.

Die Testperson wird NICHT genannt. Sie hat Stephan privat geschrieben,
nicht der Oeffentlichkeit.
"""
import sys

from wp_zugang import call

NEWS_DE, ARCHIV_DE = 184, 258
NEWS_EN, ARCHIV_EN = 363, 364
MAX_EINTRAEGE = 5

REPO = "https://github.com/Stephan-Lefty/DialOS-Mobil"

SLUG_DE = "dialos-mobil-die-app-die-sich-selbst-taub-machte"
SLUG_EN = "dialos-mobil-the-app-that-deafened-itself"
URL_DE = f"https://dialos.org/{SLUG_DE}/"
URL_EN = f"https://dialos.org/en/{SLUG_EN}/"

TITEL_DE = "DialOS Mobil: Die App, die sich selbst taub machte"
TITEL_EN = "DialOS Mobil: The app that deafened itself"

DATUM_DE = "6. September 2026"
DATUM_EN = "6 September 2026"

# Verwandte Beitraege - bei neuen Beitraegen wird bewusst auf thematisch
# passende aeltere verwiesen.
VORHER_DE = "https://dialos.org/dialos-mobil-zwei-rueckmeldungen-fuenf-fehler/"
VORHER_EN = "https://dialos.org/en/dialos-mobil-two-reports-five-bugs/"
SCHWEIGEN_DE = "https://dialos.org/das-schweigende-missverstaendnis/"
SCHWEIGEN_EN = "https://dialos.org/en/the-silent-misunderstanding/"
ERKLAERT_DE = "https://dialos.org/sprachsteuerung-erklaert/"
ERKLAERT_EN = "https://dialos.org/en/how-dialos-listens-and-speaks/"

BILD_ID = 201
BILD_URL = ("https://dialos.org/wp-content/uploads/2026/08/"
            "02_startseite_hoert_zu-scaled.png")


def p(text):
    return f"<!-- wp:paragraph -->\n<p>{text}</p>\n<!-- /wp:paragraph -->"


def h(level, text):
    attrs = f' {{"level":{level}}}' if level != 2 else ""
    return ("<!-- wp:heading" + attrs + " -->\n"
            + f'<h{level} class="wp-block-heading">{text}</h{level}>\n'
            + "<!-- /wp:heading -->")


def bild(alt, beschriftung):
    return (f'<!-- wp:image {{"id":{BILD_ID},"sizeSlug":"large",'
            '"linkDestination":"none","align":"center","width":"320px"} -->\n'
            '<figure class="wp-block-image aligncenter size-large '
            'is-resized">'
            f'<img src="{BILD_URL}" alt="{alt}" class="wp-image-{BILD_ID}" '
            'style="width:320px"/>'
            f'<figcaption class="wp-element-caption">{beschriftung}</figcaption>'
            "</figure>\n<!-- /wp:image -->")


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


def sprachmarke(ziel, beschriftung):
    """Erster Block jedes Beitrags - daran erkennt der Blog-Abgleich die
    zusammengehoerenden Sprachfassungen. Die Klasse muss genau so heissen."""
    return ('<!-- wp:paragraph {"className":"dialos-lang-marker"} -->\n'
            f'<p class="dialos-lang-marker"><a href="{ziel}">{beschriftung}'
            "</a></p>\n<!-- /wp:paragraph -->")


# ---------------------------------------------------------------------------
# Deutscher Beitrag
# ---------------------------------------------------------------------------
INHALT_DE = "\n\n".join([
    sprachmarke(URL_EN, "English"),

    p("<em>Eine Testperson hat gemeldet, beim Diktieren einer Telefonnummer "
      "seien zweimal einzelne Ziffern verschwunden. Die Spracherkennung war "
      "unschuldig. Die App hatte sich in dem Moment selbst das Mikrofon "
      "abgeschaltet &#8211; und ich hatte es gebaut.</em>"),

    h(2, "Worum es geht"),
    p("DialOS Mobil ruft Menschen aus dem Adressbuch an, wenn man ihren Namen "
      "sagt. Steht jemand nicht darin, kann man ihm die Nummer auch "
      "diktieren: „Nummer wählen“, dann Ziffer für Ziffer sprechen. Für "
      "blinde Nutzer ist das der einzige Weg zu einer fremden Nummer &#8211; "
      "eine Tastatur können sie nicht bedienen."),

    p("Der geschlossene Test von DialOS Mobil läuft seit ein paar Tagen. Über "
      "die ersten beiden Rückmeldungen habe ich "
      f'<a href="{VORHER_DE}">gestern geschrieben</a>. Die dritte betraf '
      "einen Teil, den bis dahin niemand ausprobiert hatte."),

    h(2, "Die Meldung"),
    p("Sinngemäß, ohne Namen: <em>Der Aktivierungsbefehl funktioniert "
      "zuverlässig. Beim Diktieren von Telefonnummern habe ich aber Probleme. "
      "Einmal habe ich wohl zu lange gebraucht, und die Sprachsteuerung hat "
      "sich abgeschaltet. Zweimal wurde je eine Ziffer vergessen. Und das "
      "Wichtigste: Nachdem die Ziffern wiederholt wurden, wird nicht gesagt, "
      "was nun zu tun ist. Muss ich „richtig“ sagen oder „anrufen“?</em>"),

    p("Drei Beobachtungen. Dahinter steckten vier Fehler &#8211; und der "
      "schwerwiegendste stand nicht in der Meldung, sondern erklärt sie."),

    h(2, "Das Loch, das die App selbst aufreißt"),
    bild("Der Startbildschirm von DialOS Mobil im Zustand „hört zu“.",
         "Zuhören und Sprechen schließen sich aus. Das ist der Kern des "
         "Problems."),

    p("Eine Sprachsteuerung kann nicht gleichzeitig reden und zuhören. Täte "
      "sie es, würde sie ihre eigene Stimme erkennen und sich selbst "
      "antworten. Deshalb schaltet DialOS Mobil das Mikrofon ab, solange sie "
      "spricht, und erst danach wieder ein. Das ist richtig so."),

    p("Beim Diktieren las die App nach jedem gehörten Ziffernblock die "
      "<strong>gesamte bisherige Nummer</strong> zur Kontrolle vor. Bei einer "
      "elfstelligen Handynummer heißt das gegen Ende: „null eins sieben "
      "sieben drei vier fünf sechs sieben“ &#8211; mehrere Sekunden, in denen "
      "die App nichts hört."),

    p("Wer eine Nummer herunterspricht, macht dabei aber keine Pause. Er "
      "redet weiter. Und alles, was in diese Sekunden fällt, ist "
      "<strong>weg</strong>. Nicht falsch erkannt, nicht verhört &#8211; nie "
      "angekommen."),

    p("Genau das beschreibt die Meldung: zweimal je eine Ziffer. Und es "
      "passiert erst weiter hinten in der Nummer, weil die Ansage vorne noch "
      "kurz ist. Am Anfang funktioniert alles, gegen Ende bröckelt es. Das "
      "ist die unangenehmste Sorte Fehler: Sie sieht aus wie schlechte "
      "Erkennung, ist aber ein Konstruktionsfehler."),

    p("<strong>Die Lösung war einfach, sobald die Ursache klar war.</strong> "
      "Die App bestätigt jetzt nur noch die neu hinzugekommenen Ziffern. "
      "Statt der ganzen Nummer sagt sie „vier“. Das Loch schrumpft von "
      "mehreren Sekunden auf den Bruchteil einer &#8211; kleiner geht es "
      "nicht, denn ganz ohne Rückmeldung wüsste ein blinder Nutzer nie, ob "
      "die Ziffer angekommen ist."),

    h(2, "Zehn Ziffern gesprochen, alles weg"),
    p("Die zweite Beobachtung führte zu einem Fehler, der mich mehr ärgert. "
      "Die App wartete pauschal <strong>fünfzehn Sekunden</strong> auf die "
      "nächste Eingabe. Für eine Ja/Nein-Frage ist das reichlich. Für eine "
      "Telefonnummer, die man erst nachschlagen oder von einem Zettel ablesen "
      "muss, ist es nichts."),

    p("Und wenn die Zeit ablief, hat die App die <strong>bereits "
      "gesprochenen Ziffern kommentarlos weggeworfen</strong>. Wer zehn "
      "Stellen diktiert hatte, stand wieder am Anfang und erfuhr nicht "
      "einmal, warum. Das ist dasselbe Muster, über das ich schon einmal "
      f'geschrieben habe: <a href="{SCHWEIGEN_DE}">ein System, das etwas '
      "anderes tut, als es sagt</a> &#8211; nur diesmal mit Datenverlust."),

    p("Beim Diktieren sind es jetzt fünfundvierzig Sekunden. Und läuft die "
      "Zeit ab, während Ziffern dastehen, wirft die App sie nicht weg, "
      "sondern liest sie vor und fragt, ob sie anrufen soll."),

    h(2, "„Muss ich richtig sagen oder anrufen?“"),
    p("Diese Frage ist die beste Fehlerbeschreibung, die ich in diesem "
      "Projekt bekommen habe. Sie zeigt genau den Moment, in dem jemand "
      "alleingelassen wird."),

    p("Der Hinweis, dass man am Ende „fertig“ sagen muss, stand in der "
      "Aufforderung ganz am Anfang. Die Testperson hat es selbst bemerkt und "
      "dazugeschrieben: Die Anweisung <em>war</em> gekommen &#8211; und bis "
      "nach dem Diktieren längst wieder vergessen. Danach las die App die "
      "Ziffern vor und schwieg. Sie wartete auf ein Wort, das niemand mehr "
      "wusste."),

    p("Für sehende Nutzer ist so etwas selten schlimm; sie sehen einen Knopf. "
      "Wer nur zuhört, hat nichts als die Ansage. <strong>Was nicht gesagt "
      "wird, existiert nicht.</strong> Die Anleitung steht jetzt nach dem "
      "ersten Ziffernblock noch einmal &#8211; dort, wo sie gebraucht wird. "
      "Danach nicht mehr, sonst stünde sie bei jeder weiteren Ziffer im Weg."),

    h(2, "Und wenn doch eine Ziffer fehlt?"),
    p("Auch diese Frage stand in der Meldung, und sie hatte bis dahin keine "
      "gute Antwort. Es gab nur „löschen“ &#8211; und das warf die ganze "
      "Nummer weg. Bei elf Stellen und einer verschluckten Ziffer hieß das: "
      "alles noch einmal."),

    p("Jetzt genügt <strong>„letzte Ziffer löschen“</strong>, auch „eine "
      "zurück“ oder „rückgängig“. Die App nimmt genau eine Stelle zurück und "
      "liest vor, was übrig bleibt."),

    p("Nebenbei war dabei eine Feinheit zu beachten: „zurück“ allein bedeutet "
      "in dieser App <em>abbrechen</em>. „Eine zurück“ bedeutet das Gegenteil "
      "&#8211; weitermachen, nur einen Schritt kleiner. Beide Fälle sind "
      "jetzt durch je einen automatischen Test abgesichert, damit sie sich "
      "nicht irgendwann gegenseitig auffressen."),

    h(2, "Was ich daraus lerne"),
    p("Ich habe das Diktieren wochenlang selbst benutzt und den Fehler nie "
      "ausgelöst. Der Grund ist unangenehm: <strong>Ich habe nach jeder "
      "Ansage gewartet</strong> &#8211; weil ich weiß, dass die App dann "
      "nicht zuhört. Ich habe mein Sprechen an die Technik angepasst, ohne es "
      "zu merken."),

    p("Wer das nicht weiß, spricht einfach weiter. Und hat damit recht. Eine "
      "App für Menschen, die keine andere Wahl haben, darf nicht verlangen, "
      "dass man ihre Innereien kennt. <strong>Sie muss sich nach dem "
      "Menschen richten, nicht umgekehrt.</strong>"),

    p("Das ist inzwischen die dritte Rückmeldung aus dem Test und der neunte "
      "behobene Fehler. Keiner davon war ein Absturz. Keiner wäre in einem "
      "automatischen Test aufgefallen. Alle entstanden dort, wo ich "
      "angenommen habe, wie jemand spricht."),

    p("Wie Spracherkennung und Sprachausgabe überhaupt funktionieren, ist "
      f'ohne Vorwissen in <a href="{ERKLAERT_DE}">diesem Beitrag</a> erklärt. '
      "Die Änderungen sind fertig und liegen als Version 0.6.4 "
      f'<a href="{REPO}">im Quellcode</a>. Auf die Telefone der Testenden '
      "kommen sie erst nach den vierzehn Tagen &#8211; ein Update mitten im "
      "laufenden Test würde die Zählung stören."),
])

AUSZUG_DE = ("Beim Diktieren verschwanden Ziffern. Schuld war nicht die "
             "Erkennung: Die App las nach jedem Block die ganze Nummer vor "
             "und schaltete dabei ihr eigenes Mikrofon ab.")


# ---------------------------------------------------------------------------
# Englischer Beitrag
# ---------------------------------------------------------------------------
def inhalt_en(post_id):
    return "\n\n".join([
        meta_en(post_id),
        sprachmarke(URL_DE, "Deutsch"),

        p("<em>A tester reported that single digits kept vanishing while "
          "dictating a phone number. Speech recognition was innocent. The app "
          "had switched off its own microphone at that very moment &#8211; "
          "and I had built it that way.</em>"),

        h(2, "What this is about"),
        p("DialOS Mobil calls people from your address book when you say "
          "their name. If someone is not in it, you can dictate the number "
          "instead: say “Nummer wählen”, then speak it digit by digit. For "
          "blind users that is the only route to an unknown number &#8211; "
          "they cannot use a keypad."),

        p("The closed test has been running for a few days. I wrote about the "
          f'first two reports <a href="{VORHER_EN}">yesterday</a>. The third '
          "concerned a part nobody had tried until then."),

        h(2, "The report"),
        p("Paraphrased, without a name: <em>The wake phrase works reliably. "
          "But I have trouble dictating phone numbers. Once I apparently took "
          "too long and voice control switched itself off. Twice a single "
          "digit was dropped. And most importantly: after the digits are read "
          "back, nothing tells me what to do next. Am I supposed to say "
          "“richtig”, or “anrufen”?</em>"),

        p("Three observations. Behind them were four bugs &#8211; and the "
          "most serious one was not in the report, but explains it."),

        h(2, "The gap the app tears open itself"),
        bild("The DialOS Mobil home screen in the “listening” state.",
             "Listening and speaking are mutually exclusive. That is the core "
             "of the problem."),

        p("A voice assistant cannot speak and listen at the same time. If it "
          "did, it would recognise its own voice and answer itself. So DialOS "
          "Mobil switches the microphone off while it speaks and back on "
          "afterwards. That part is correct."),

        p("While dictating, the app read back the <strong>entire number so "
          "far</strong> after every block of digits it heard. Towards the end "
          "of an eleven-digit mobile number that means several seconds during "
          "which the app hears nothing at all."),

        p("But someone reciting a number does not pause. They keep going. And "
          "everything spoken into those seconds is <strong>gone</strong>. Not "
          "misheard, not misrecognised &#8211; never received."),

        p("That is exactly what the report describes: twice, one digit each. "
          "And it only happens further into the number, because early on the "
          "announcement is still short. It works at the start and falls apart "
          "towards the end. That is the nastiest kind of bug: it looks like "
          "poor recognition but is a design fault."),

        p("<strong>The fix was simple once the cause was clear.</strong> The "
          "app now confirms only the newly added digits. Instead of the whole "
          "number it says “four”. The gap shrinks from several seconds to a "
          "fraction of one &#8211; it cannot go to zero, because without any "
          "feedback a blind user would never know whether the digit arrived."),

        h(2, "Ten digits spoken, all lost"),
        p("The second observation led to a bug that annoys me more. The app "
          "waited a flat <strong>fifteen seconds</strong> for the next input. "
          "For a yes/no question that is generous. For a phone number you "
          "first have to look up or read off a note, it is nothing."),

        p("And when the time ran out, the app <strong>silently discarded the "
          "digits already spoken</strong>. Anyone who had dictated ten of "
          "them was back at the start without even being told why. That is "
          "the same pattern I have written about before: "
          f'<a href="{SCHWEIGEN_EN}">a system that does something other than '
          "what it says</a> &#8211; only this time with data loss."),

        p("Dictation now allows forty-five seconds. And if the time runs out "
          "while digits are present, the app does not throw them away: it "
          "reads them back and asks whether it should dial."),

        h(2, "“Am I supposed to say richtig or anrufen?”"),
        p("That question is the best bug report I have received on this "
          "project. It pinpoints the exact moment someone is left alone."),

        p("The hint that you finish by saying “fertig” was in the prompt right "
          "at the beginning. The tester noticed this himself and added it: the "
          "instruction <em>had</em> been given &#8211; and was long forgotten "
          "by the time dictation was over. After that the app read the digits "
          "back and fell silent. It was waiting for a word nobody remembered."),

        p("For sighted users that is rarely serious; they can see a button. "
          "Someone who only listens has nothing but the announcement. "
          "<strong>What is not said does not exist.</strong> The instruction "
          "is now repeated after the first block of digits &#8211; where it is "
          "needed. Not after that, or it would be in the way at every "
          "subsequent digit."),

        h(2, "And if a digit is missing after all?"),
        p("That question was in the report too, and until now it had no good "
          "answer. There was only “löschen” &#8211; and that discarded the "
          "whole number. With eleven digits and one swallowed, it meant "
          "starting over."),

        p("Now <strong>“letzte Ziffer löschen”</strong> is enough, as are "
          "“eine zurück” and “rückgängig”. The app takes back exactly one "
          "digit and reads out what remains."),

        p("One subtlety had to be respected along the way: “zurück” on its own "
          "means <em>cancel</em> in this app. “Eine zurück” means the "
          "opposite &#8211; carry on, just one step smaller. Both cases are "
          "now covered by a test each, so they cannot quietly eat each other "
          "later."),

        h(2, "What I take from this"),
        p("I used dictation myself for weeks and never triggered the bug. The "
          "reason is uncomfortable: <strong>I paused after every "
          "announcement</strong> &#8211; because I know the app is not "
          "listening then. I had adapted my speech to the technology without "
          "noticing."),

        p("Someone who does not know that simply keeps talking. And they are "
          "right to. An app for people who have no alternative must not "
          "require you to know its internals. <strong>It has to adapt to the "
          "human, not the other way round.</strong>"),

        p("This is the third report from the test and the ninth bug fixed. "
          "None of them was a crash. None would have shown up in an automated "
          "test. All of them arose where I had assumed how someone speaks."),

        p("How speech recognition and speech output actually work is explained "
          f'without prior knowledge in <a href="{ERKLAERT_EN}">this post</a>. '
          "The changes are finished and available as version 0.6.4 "
          f'<a href="{REPO}">in the source code</a>. They will only reach the '
          "testers' phones after the fourteen days &#8211; an update in the "
          "middle of a running test would disturb the count."),
    ])


AUSZUG_EN = ("Digits vanished while dictating. The recogniser was not to "
             "blame: the app read back the whole number after every block and "
             "switched off its own microphone while doing so.")


# ---------------------------------------------------------------------------
def schreibe_beitrag(slug, titel, inhalt, auszug, kommentare="open"):
    """kommentare: "open" fuer deutsche, "closed" fuer englische Beitraege."""
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
    """Bis zum ERSTEN </div> je Teilstueck - beim letzten Eintrag folgt sonst
    noch das schliessende </div> der ganzen Liste."""
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


def pflege(news_id, archiv_id, url, neuer, name):
    """Traegt den Eintrag oben ein - oder frischt ihn auf, wenn er schon da
    ist. Auffrischen statt Ueberspringen, damit ein geaenderter Titel auch
    in der Uebersicht ankommt und nicht der alte Linktext stehen bleibt."""
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
               "Beim Diktieren verschwanden Ziffern &#8211; nicht wegen der "
               "Erkennung, sondern weil die App ihr eigenes Mikrofon "
               "abschaltete."),
       "deutsch")
pflege(NEWS_EN, ARCHIV_EN, URL_EN,
       eintrag(URL_EN, TITEL_EN, DATUM_EN,
               "Digits vanished while dictating &#8211; not because of the "
               "recogniser, but because the app switched off its own "
               "microphone."),
       "englisch")

print(f"\nFertig.\n  DE: {URL_DE}\n  EN: {URL_EN}\n"
      "  Übersicht: https://dialos.org/neuigkeiten/ und "
      "https://dialos.org/en/news/")
