#!/usr/bin/env python3
"""Veroeffentlicht die ersten Ergebnisse des geschlossenen Tests auf dialos.org.

Legt drei Dinge an bzw. aktualisiert sie (mehrfach ausfuehrbar):

  1. deutscher Beitrag  /dialos-mobil-zwei-rueckmeldungen-fuenf-fehler/
  2. englischer Beitrag /dialos-mobil-two-reports-five-bugs/
  3. Eintraege ganz oben in beiden Spalten der Seite /neuigkeiten/ (id 184)

Aufbau und Konventionen sind aus dialos-mobil-neuigkeit.py uebernommen:
Sprachumschalter als erster Absatz, englischer Beitrag ohne Kommentare und
mit eigener Meta-Zeile (deshalb wird er zweimal geschrieben).

Die beiden Testpersonen werden bewusst NICHT genannt. Sie haben Stephan
privat geschrieben, nicht der Oeffentlichkeit - ihre Rueckmeldungen sind
hier sinngemaess wiedergegeben, ohne Namen und ohne Zitate, die sie
identifizierbar machen wuerden.
"""
import sys

from wp_zugang import call

# Seit dem 25.08.2026 hat jede Sprache ihre eigene Uebersicht und ihr
# eigenes Archiv; die frueheren zwei Spalten auf einer Seite gibt es nicht
# mehr. Gepflegt wird das sonst vom taeglichen Blog-Abgleich - dieselben
# Regeln gelten hier: neuester Eintrag oben, hoechstens fuenf pro Seite,
# was hinausfaellt, wandert oben ins Archiv.
NEWS_DE, ARCHIV_DE = 184, 258
NEWS_EN, ARCHIV_EN = 363, 364
MAX_EINTRAEGE = 5

REPO = "https://github.com/Stephan-Lefty/DialOS-Mobil"

SLUG_DE = "dialos-mobil-zwei-rueckmeldungen-fuenf-fehler"
SLUG_EN = "dialos-mobil-two-reports-five-bugs"
URL_DE = f"https://dialos.org/{SLUG_DE}/"
# Englische Beitraege liegen seit dem 25.08.2026 unter /en/.
URL_EN = f"https://dialos.org/en/{SLUG_EN}/"

TITEL_DE = "Zwei Rückmeldungen, fünf Fehler"
TITEL_EN = "Two reports, five bugs"

DATUM_DE = "5. September 2026"
DATUM_EN = "5 September 2026"

# Verwandte Beitraege - bei neuen Beitraegen wird bewusst auf thematisch
# passende aeltere verwiesen, statt sie nebeneinander stehen zu lassen.
AUFRUF_DE = "https://dialos.org/dialos-mobil-tester-gesucht/"
AUFRUF_EN = "https://dialos.org/en/dialos-mobil-testers-wanted/"
SCHWEIGEN_DE = "https://dialos.org/das-schweigende-missverstaendnis/"
SCHWEIGEN_EN = "https://dialos.org/en/the-silent-misunderstanding/"
ERKLAERT_DE = "https://dialos.org/sprachsteuerung-erklaert/"
ERKLAERT_EN = "https://dialos.org/en/how-dialos-listens-and-speaks/"

BILD_ID = 203
BILD_URL = ("https://dialos.org/wp-content/uploads/2026/08/"
            "03_infos_einstellungen-scaled.png")


def p(text):
    return f"<!-- wp:paragraph -->\n<p>{text}</p>\n<!-- /wp:paragraph -->"


def h(level, text):
    attrs = f' {{"level":{level}}}' if level != 2 else ""
    return ("<!-- wp:heading" + attrs + " -->\n"
            + f'<h{level} class="wp-block-heading">{text}</h{level}>\n'
            + "<!-- /wp:heading -->")


def liste(punkte):
    items = "".join(f"<!-- wp:list-item -->\n<li>{x}</li>\n<!-- /wp:list-item -->"
                    for x in punkte)
    return ('<!-- wp:list -->\n<ul class="wp-block-list">' + items
            + "</ul>\n<!-- /wp:list -->")


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

    p("<em>Der geschlossene Test von DialOS Mobil läuft seit wenigen Tagen. "
      "Zwei Rückmeldungen genügten, um fünf Fehler zu finden &#8211; "
      "darunter einen, der jeden einzelnen Nutzer betraf und den ich in "
      "Wochen eigenen Testens nicht bemerkt hatte.</em>"),

    h(2, "Warum überhaupt getestet wird"),
    p("Bevor eine neue App in den Google Play Store darf, verlangt Google "
      "von Konten wie meinem einen <strong>geschlossenen Test mit "
      "mindestens zwölf Personen über vierzehn Tage</strong>. Wie es dazu "
      f'kam, steht im <a href="{AUFRUF_DE}">Aufruf vom August</a>. Die zwölf '
      "sind inzwischen beisammen, und die ersten haben angefangen."),

    p("Was dabei herauskam, ist der eigentliche Grund, warum sich diese "
      "Hürde lohnt &#8211; auch wenn sie sich vorher wie eine Schikane "
      "anfühlt."),

    h(2, "Was gemeldet wurde"),
    p("Zwei Testpersonen haben unabhängig voneinander geschrieben. Beide "
      "hatten die App nur kurz ausprobiert. Ihre Rückmeldungen gebe ich "
      "sinngemäß wieder; die Namen bleiben bei ihnen."),

    p("Die eine Rückmeldung: <em>Ich habe unter meinem Namen zwei Nummern "
      "gespeichert, eine mobile und eine private. Sage ich, dass die private "
      "angerufen werden soll, fragt die App trotzdem nach der mobilen. Sage "
      "ich dann „nein, privat“, antwortet sie: Das habe ich nicht "
      "verstanden.</em>"),

    p("Die andere: <em>Ein Vorname wurde beharrlich als ein anderer, ähnlich "
      "klingender verstanden, obwohl der richtige im Adressbuch steht. Mit "
      "Vor- und Nachnamen klappt es. Außerdem sagt die App nach einer Weile "
      "„Ich beende die Sprachsteuerung“, aber der Knopf zeigt weiterhin "
      "„ausschalten“ &#8211; ist sie nun aus oder nicht? Und die Stimme "
      "klingt unnatürlich; eine Einstellung dafür habe ich nicht "
      "gefunden.</em>"),

    h(2, "Was sich beheben ließ"),

    h(3, "„Ja bitte“ wurde nicht verstanden"),
    p("Der schwerwiegendste Fund stand in keiner der beiden Meldungen "
      "&#8211; er fiel beim Nachsehen auf. Die App verglich das Gesagte "
      "<strong>als ganzen Satz</strong> mit ihrer Liste bekannter Antworten. "
      "„Ja“ stand darin, „bitte“ auch. <strong>„Ja bitte“ stand nicht "
      "darin</strong> &#8211; und wurde deshalb nicht verstanden. Ebenso "
      "„nein danke“."),

    p("Das ist die häufigste Art, wie Menschen antworten. Es traf jeden "
      "Nutzer, bei jedem Anruf. Aufgefallen ist es mir nie, weil ich beim "
      "Testen wie ein Entwickler spreche: knapp, deutlich, in der "
      "Form, die ich selbst vorgesehen habe. Die App prüft jetzt Wort für "
      "Wort."),

    h(3, "Wenn Namen gleich klingen"),
    p("Damit die App auch dann findet, wen man meint, wenn die Erkennung "
      "sich verhört, vergleicht sie Namen nach ihrem <strong>Klang</strong> "
      "&#8211; nach einem Verfahren, das Meier, Maier, Mayer und Meyer als "
      "denselben Namen behandelt. Das ist genau richtig für Nachnamen. Bei "
      "Vornamen ging es nach hinten los: Der gemeldete Vorname und der "
      "fälschlich vorgeschlagene haben denselben Klangcode. Beide bekamen "
      "deshalb <strong>dieselbe Punktzahl</strong>."),

    p("Bei Gleichstand entschied die alphabetische Reihenfolge. Und weil die "
      "App nur die drei besten Vorschläge vorliest, verdrängten mehrere "
      "gleich klingende Kontakte den gemeinten vollständig aus der Liste "
      "&#8211; er wurde nicht einmal als Möglichkeit angeboten. Ein reiner "
      "Klangtreffer zählt jetzt weniger als ein echter. Meier und Maier "
      "findet die App weiterhin."),

    p("Wie diese Verfahren arbeiten, ist im Beitrag "
      f'<a href="{ERKLAERT_DE}">Wie hört und spricht DialOS eigentlich?</a> '
      "ohne Vorwissen erklärt."),

    h(3, "„Privat“ und „mobil“ als Befehl"),
    p("Die App kannte keine Nummerntypen. „Privat“ wanderte in den Namen "
      "und störte dort die Suche; als Antwort auf die Rückfrage war es "
      "schlicht unbekannt. Wer zwei Nummern gespeichert hatte, kam an die "
      "zweite nur, indem er sich durch alle Vorschläge „Nein“ sagte."),

    p("Jetzt versteht die App <strong>„privat“, „mobil“ und „Arbeit“</strong> "
      "&#8211; sowohl im Befehl („Anna privat anrufen“) als auch als Antwort "
      "auf die Rückfrage. Auch „nein, privat“ funktioniert, und zwar als "
      "das, was gemeint ist: nicht als Abbruch, sondern als Wunsch nach der "
      "anderen Nummer."),

    h(3, "Die Sackgasse"),
    p("Beim Reparieren kam ein Fehler zum Vorschein, den niemand gemeldet "
      "hatte. Nach „Das habe ich nicht verstanden“ sollte die App die Frage "
      "wiederholen. Sie wiederholte aber <strong>den Hinweis</strong>, weil "
      "sich die beiden gegenseitig überschrieben. Wer einmal falsch "
      "verstanden wurde, bekam die Frage nie wieder zu hören &#8211; und "
      "wusste nicht mehr, worauf er antworten sollte."),

    h(3, "Eine Ansage, die nicht stimmte"),
    p("Nach einer Weile ohne Antwort sagte die App: „Ich beende die "
      "Sprachsteuerung.“ Das war <strong>schlicht falsch</strong>. Sie "
      "beendete nur das laufende Gespräch und hörte weiter auf das "
      "Aktivierungswort. Der Knopf zeigte also zu Recht „ausschalten“ "
      "&#8211; die Ansage log, nicht die Anzeige."),

    p("Für sehende Nutzer ist das eine Kleinigkeit. Wer nicht auf den "
      "Bildschirm sehen kann, hat nur diese Ansage: Er glaubt, die App sei "
      "aus, und schaltet sie unnötig wieder ein. Über genau dieses "
      "Fehlermuster &#8211; ein System, das etwas anderes sagt, als es tut "
      f'&#8211; ging schon <a href="{SCHWEIGEN_DE}">Das schweigende '
      "Missverständnis</a>. Es ist der hartnäckigste Gegner dieses "
      "Projekts."),

    h(3, "Stimme und Tempo"),
    bild("Die Seite „Infos & Einstellungen“ von DialOS Mobil mit großen, "
         "gut lesbaren Schaltflächen.",
         "Hier sitzen die neuen Einstellungen für Stimme und Tempo."),

    p("Die Sprachausgabe klang nach dem, was Android eben mitbringt, und "
      "ließ sich nicht ändern. Jetzt gibt es <strong>vier "
      "Geschwindigkeitsstufen</strong> von langsam bis sehr schnell und die "
      "Auswahl unter den deutschen Stimmen des Geräts."),

    p("Beides sind <strong>Knöpfe zum Weiterschalten, keine "
      "Schieberegler</strong>. Ein Regler setzt voraus, dass man ihn sieht "
      "und trifft &#8211; beides kann man bei dieser Zielgruppe nicht "
      "annehmen. Und nach jedem Tippen spricht die App eine Hörprobe. Ohne "
      "sie wäre die Einstellung wertlos: Eine Stimme lässt sich nicht "
      "beurteilen, indem man ihren Namen liest."),

    h(2, "Was nicht geht"),

    h(3, "Schweizerdeutsch"),
    p("Eine der beiden Testpersonen spricht Schweizerdeutsch. Die App "
      "versteht es nicht, und daran wird sich <strong>nichts ändern</strong>. "
      "Die Spracherkennung läuft vollständig auf dem Telefon, ohne Internet "
      "&#8211; das ist der Kern dieser App. Dafür braucht es ein "
      "Sprachmodell, das mit aufs Gerät passt, und ein solches Modell "
      "existiert für Schweizerdeutsch nicht. Hochdeutsch versteht sie auch "
      "von Schweizer Sprechern; die Mundart nicht."),

    p("Das gehört gesagt, bevor jemand die App herunterlädt und enttäuscht "
      "wird. Es steht deshalb künftig in der Beschreibung im Play Store."),

    h(3, "Ein ungelöstes Rätsel"),
    p("Ein weiterer Vorname wurde als ein völlig anders klingender "
      "verstanden. Dafür habe ich <strong>keine Erklärung</strong>. Ich habe "
      "die Berechnung mit den echten Namen nachgerechnet: Der gemeinte "
      "Kontakt liegt weit vorn, der fälschlich vorgeschlagene weit "
      "dahinter, und die Klangcodes sind verschieden. Der Fehler kann dort "
      "nicht entstanden sein."),

    p("Wahrscheinlich hat schon die Spracherkennung etwas anderes gehört, "
      "bevor die Namenssuche überhaupt anfing. Ohne Protokoll wäre alles "
      "Weitere geraten. <strong>Also bleibt es offen</strong>, notiert und "
      "unerledigt, bis es jemand erneut auslöst."),

    h(2, "Was ich daraus mitnehme"),
    p("Fünf Fehler aus zwei Rückmeldungen, nach wenigen Minuten Benutzung. "
      "Keiner davon war ein Absturz, keiner wäre in einem automatischen Test "
      "aufgefallen. Alle fünf entstanden an derselben Stelle: dort, wo ich "
      "angenommen habe, wie jemand spricht &#8211; und die Annahme war die "
      "eines Entwicklers, der seine eigene App bedient."),

    p("<strong>Genau dafür sind die zwölf Personen da.</strong> Google "
      "verlangt sie als Hürde vor dem Store. Sie sind aber keine Hürde, "
      "sondern das Nützlichste, was diesem Projekt seit Wochen passiert "
      "ist."),

    p("Die Änderungen sind fertig und liegen als Version 0.6.3 "
      f'<a href="{REPO}">im Quellcode</a>. Auf die Telefone der Testenden '
      "kommen sie erst nach den vierzehn Tagen &#8211; ein Update mitten im "
      "laufenden Test würde die Zählung stören. Bis dahin testen die zwölf "
      "die Fassung mit den Fehlern. Das ist in Ordnung: Wir wissen jetzt, "
      "welche es sind."),
])

AUSZUG_DE = ("Zwei Testpersonen, wenige Minuten Benutzung, fünf Fehler "
             "&#8211; darunter einer, der jeden Nutzer betraf: „Ja bitte“ "
             "verstand die App nicht. Was sich beheben ließ, und was nicht.")


# ---------------------------------------------------------------------------
# Englischer Beitrag
# ---------------------------------------------------------------------------
def inhalt_en(post_id):
    return "\n\n".join([
        meta_en(post_id),
        sprachmarke(URL_DE, "Deutsch"),

        p("<em>The closed test of DialOS Mobil has been running for a few "
          "days. Two reports were enough to surface five bugs &#8211; "
          "including one that affected every single user and that weeks of "
          "my own testing had never revealed.</em>"),

        h(2, "Why there is a test at all"),
        p("Before a new app may enter the Google Play Store, Google requires "
          "accounts like mine to run a <strong>closed test with at least "
          "twelve people over fourteen days</strong>. How that came about is "
          f'described in <a href="{AUFRUF_EN}">the call from August</a>. The '
          "twelve are together now, and the first of them have started."),

        p("What came out of it is the real reason this hurdle is worth "
          "having &#8211; even though it feels like red tape beforehand."),

        h(2, "What was reported"),
        p("Two testers wrote independently of each other. Both had only "
          "tried the app briefly. Their reports are paraphrased here; their "
          "names stay with them."),

        p("One report: <em>I have two numbers stored under my name, a mobile "
          "one and a private one. When I ask for the private one, the app "
          "still asks about the mobile one. If I then say “no, private”, it "
          "answers: I did not understand that.</em>"),

        p("The other: <em>A first name was persistently heard as a different, "
          "similar-sounding one, although the right one is in the address "
          "book. With first and last name it works. Also, after a while the "
          "app says “I am shutting down voice control”, but the button still "
          "reads “turn off” &#8211; so is it off or not? And the voice sounds "
          "unnatural; I could not find a setting for it.</em>"),

        h(2, "What could be fixed"),

        h(3, "“Ja bitte” was not understood"),
        p("The most serious finding was in neither report &#8211; it turned "
          "up while looking into them. The app compared what was said "
          "<strong>as a whole sentence</strong> against its list of known "
          "answers. “Ja” was in that list. “Bitte” was too. <strong>“Ja "
          "bitte” was not</strong> &#8211; and so it was not understood. "
          "Neither was “nein danke”."),

        p("That is the most common way people answer. It affected every "
          "user, on every call. I never noticed because when I test, I speak "
          "like a developer: short, clear, in the form I designed myself. "
          "The app now checks word by word."),

        h(3, "When names sound alike"),
        p("So that the app still finds who you mean when the recogniser "
          "mishears, it compares names by <strong>sound</strong> &#8211; "
          "using a method that treats Meier, Maier, Mayer and Meyer as the "
          "same name. That is exactly right for surnames. For first names it "
          "backfired: the reported name and the wrongly suggested one share "
          "the same sound code. Both therefore received <strong>the same "
          "score</strong>."),

        p("On a tie, alphabetical order decided. And because the app reads "
          "out only the three best suggestions, several similar-sounding "
          "contacts pushed the intended one out of the list entirely &#8211; "
          "it was not even offered as an option. A pure sound-alike match now "
          "counts for less than a real one. Meier and Maier are still found."),

        p("How these methods work is explained without prior knowledge in "
          f'<a href="{ERKLAERT_EN}">How does DialOS listen and speak?</a>'),

        h(3, "“Private” and “mobile” as a command"),
        p("The app knew no number types at all. “Privat” ended up inside the "
          "name and disturbed the search there; as an answer to the "
          "confirmation it was simply unknown. Anyone with two numbers stored "
          "could only reach the second by saying “no” through every "
          "suggestion."),

        p("The app now understands <strong>“privat”, “mobil” and "
          "“Arbeit”</strong> &#8211; both in the command (“Anna privat "
          "anrufen”) and as an answer to the confirmation. “Nein, privat” "
          "works too, and means what it is meant to mean: not a cancellation, "
          "but a request for the other number."),

        h(3, "The dead end"),
        p("While fixing this, a bug surfaced that nobody had reported. After "
          "“I did not understand that”, the app was supposed to repeat the "
          "question. Instead it repeated <strong>the notice</strong>, because "
          "the two overwrote each other. Anyone misheard once never heard the "
          "question again &#8211; and no longer knew what to answer."),

        h(3, "An announcement that was not true"),
        p("After a while without an answer, the app said: “I am shutting down "
          "voice control.” That was <strong>simply false</strong>. It only "
          "ended the current conversation and kept listening for the wake "
          "phrase. So the button rightly read “turn off” &#8211; the "
          "announcement was lying, not the display."),

        p("For sighted users that is a detail. Someone who cannot see the "
          "screen has nothing but that announcement: they believe the app is "
          "off and switch it back on for no reason. This exact failure "
          "pattern &#8211; a system that says something other than what it "
          f'does &#8211; was the subject of <a href="{SCHWEIGEN_EN}">The '
          "silent misunderstanding</a>. It is this project's most persistent "
          "opponent."),

        h(3, "Voice and speed"),
        bild("The “Info &amp; settings” screen of DialOS Mobil with large, "
             "clearly legible buttons.",
             "This is where the new settings for voice and speed live."),

        p("Speech output sounded like whatever Android happened to provide, "
          "and could not be changed. There are now <strong>four speed "
          "steps</strong> from slow to very fast, plus a choice among the "
          "German voices installed on the device."),

        p("Both are <strong>step-forward buttons, not sliders</strong>. A "
          "slider assumes you can see it and hit it &#8211; neither can be "
          "taken for granted with this audience. And after each tap the app "
          "speaks a sample. Without it the setting would be worthless: you "
          "cannot judge a voice by reading its name."),

        h(2, "What cannot be done"),

        h(3, "Swiss German"),
        p("One of the two testers speaks Swiss German. The app does not "
          "understand it, and that <strong>will not change</strong>. Speech "
          "recognition runs entirely on the phone, without internet &#8211; "
          "that is the core of this app. It needs a language model small "
          "enough to ship with it, and no such model exists for Swiss German. "
          "Standard German it does understand, including from Swiss speakers; "
          "the dialect it does not."),

        p("This needs saying before anyone downloads the app and is "
          "disappointed. It will therefore go into the Play Store "
          "description."),

        h(3, "One unsolved puzzle"),
        p("Another first name was heard as a completely different-sounding "
          "one. For that I have <strong>no explanation</strong>. I "
          "recalculated the scoring with the real names: the intended contact "
          "ranks far ahead, the wrongly suggested one far behind, and their "
          "sound codes differ. The fault cannot have originated there."),

        p("Most likely the speech recogniser already heard something else "
          "before the name search even began. Without a log, anything further "
          "would be guesswork. <strong>So it stays open</strong>, written "
          "down and unresolved, until someone triggers it again."),

        h(2, "What I take from this"),
        p("Five bugs from two reports, after a few minutes of use. None of "
          "them was a crash; none would have shown up in an automated test. "
          "All five arose in the same place: where I had assumed how someone "
          "speaks &#8211; and the assumption was that of a developer "
          "operating his own app."),

        p("<strong>That is exactly what the twelve people are for.</strong> "
          "Google demands them as a hurdle before the store. They are not a "
          "hurdle, though, but the most useful thing that has happened to "
          "this project in weeks."),

        p("The changes are finished and available as version 0.6.3 "
          f'<a href="{REPO}">in the source code</a>. They will only reach the '
          "testers' phones after the fourteen days &#8211; an update in the "
          "middle of a running test would disturb the count. Until then the "
          "twelve are testing the version with the bugs in it. That is fine: "
          "we now know which ones they are."),
    ])


AUSZUG_EN = ("Two testers, a few minutes of use, five bugs &#8211; including "
             "one that affected every user: the app did not understand “ja "
             "bitte”. What could be fixed, and what could not.")


# ---------------------------------------------------------------------------
def schreibe_beitrag(slug, titel, inhalt, auszug, kommentare="open"):
    """kommentare: "open" fuer deutsche, "closed" fuer englische Beitraege.

    Begruendung siehe dialos-mobil-neuigkeit.py: Die Website laeuft auf
    de_DE, unter einem englischen Beitrag kaemen sonst alle
    WordPress-Texte auf Deutsch heraus.
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


print("Deutscher Beitrag:")
de = schreibe_beitrag(SLUG_DE, TITEL_DE, INHALT_DE, AUSZUG_DE)

print("Englischer Beitrag:")
# Erst anlegen (ohne Meta-Zeile), um die ID zu bekommen ...
en = schreibe_beitrag(SLUG_EN, TITEL_EN, inhalt_en(0), AUSZUG_EN,
                      kommentare="closed")
# ... dann mit eingesetzter ID nachziehen, damit das CSS greift.
en = call("POST", f"wp/v2/posts/{en['id']}", {"content": inhalt_en(en["id"])})
print(f"  Meta-Zeile auf postid-{en['id']} gesetzt")

# ---------------------------------------------------------------------------
# Eintraege auf /neuigkeiten/
# ---------------------------------------------------------------------------
LISTE_AUF = '<div class="dialos-news-list">'


def eintrag(url, titel, datum, text):
    return ('<div class="dialos-news-entry">\n'
            f'<h3><a href="{url}">{titel}</a></h3>\n'
            f"<p><em>{datum}</em> — {text}</p>\n"
            "</div>")


def eintraege(inhalt):
    """Zerlegt eine Uebersichtsseite in ihre einzelnen Eintraege.

    Bis zum ERSTEN </div> je Teilstueck, nicht bis zum letzten: Beim letzten
    Eintrag folgt sonst noch das schliessende </div> der ganzen Liste und
    landet mit im Eintrag.
    """
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
    """Gibt (gekuerzter Inhalt, herausgefallene Eintraege) zurueck."""
    alle = eintraege(inhalt)
    if len(alle) <= MAX_EINTRAEGE:
        return inhalt, []
    zuviel = alle[MAX_EINTRAEGE:]
    for alt in zuviel:
        inhalt = inhalt.replace("\n\n" + alt, "", 1)
        inhalt = inhalt.replace(alt, "", 1)
    return inhalt, zuviel


def pflege(news_id, archiv_id, url, neuer, name):
    seite = call("GET", f"wp/v2/pages/{news_id}?context=edit")
    inhalt = seite["content"]["raw"]

    if url in inhalt:
        print(f"  {name}: Eintrag steht schon drin, uebersprungen.")
        return

    inhalt = oben_einfuegen(inhalt, neuer)
    inhalt, verdraengt = ueberschuss_abschneiden(inhalt)
    call("POST", f"wp/v2/pages/{news_id}", {"content": inhalt})
    print(f"  {name}: Eintrag oben eingefuegt.")

    if not verdraengt:
        return
    # Was aus der Uebersicht faellt, kommt oben ins Archiv - sonst waere der
    # aelteste Beitrag von der Website aus nicht mehr erreichbar.
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
               "Die ersten Rückmeldungen aus dem geschlossenen Test &#8211; "
               "fünf Fehler, einer davon traf jeden Nutzer."),
       "deutsch")
pflege(NEWS_EN, ARCHIV_EN, URL_EN,
       eintrag(URL_EN, TITEL_EN, DATUM_EN,
               "The first reports from the closed test &#8211; five bugs, "
               "one of which affected every user."),
       "englisch")

print(f"\nFertig.\n  DE: {URL_DE}\n  EN: {URL_EN}\n"
      "  Übersicht: https://dialos.org/neuigkeiten/ und "
      "https://dialos.org/en/news/")
