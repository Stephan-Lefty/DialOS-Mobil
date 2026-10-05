#!/usr/bin/env python3
"""Legt die Download-Seiten fuer DialOS Mobil auf dialos.org an - deutsch und
englisch, nach dem Muster der bestehenden Seitenpaare (Sprachmarker oben,
englische Seite als Kind von /en/, Seite 135).

Ohne --schreiben nur Probelauf.
"""
import sys
from wp_zugang import call

EN_ELTERN = 135

VERSION = "0.6.17"
APK_URL = ("https://github.com/Stephan-Lefty/DialOS-Mobil/releases/download/"
           "v0.6.17/DialOS-Mobil-0.6.17.apk")
RELEASE_URL = "https://github.com/Stephan-Lefty/DialOS-Mobil/releases/tag/v0.6.17"
SHA256 = "088db5857c0acea30a2961fca9f27fa0cb6b65f8594142dd64871e4f5eda2fe6"
GROESSE = "54 MB"

DE_SLUG = "dialos-mobil-herunterladen"
EN_SLUG = "download-dialos-mobil"

DE_TITEL = "DialOS Mobil herunterladen"
EN_TITEL = "Download DialOS Mobil"


def absatz(text, klasse=None):
    if klasse:
        return (f'<!-- wp:paragraph {{"className":"{klasse}"}} -->\n'
                f'<p class="{klasse}">{text}</p>\n<!-- /wp:paragraph -->')
    return f"<!-- wp:paragraph -->\n<p>{text}</p>\n<!-- /wp:paragraph -->"


def ueberschrift(text, stufe=2):
    if stufe == 2:
        return (f'<!-- wp:heading -->\n<h2 class="wp-block-heading">{text}</h2>\n'
                "<!-- /wp:heading -->")
    return (f'<!-- wp:heading {{"level":{stufe}}} -->\n'
            f'<h{stufe} class="wp-block-heading">{text}</h{stufe}>\n'
            "<!-- /wp:heading -->")


def knopf(text, url):
    return (
        "<!-- wp:buttons -->\n<div class=\"wp-block-buttons\">\n"
        "<!-- wp:button -->\n"
        f'<div class="wp-block-button"><a class="wp-block-button__link '
        f'wp-element-button" href="{url}">{text}</a></div>\n'
        "<!-- /wp:button -->\n</div>\n<!-- /wp:buttons -->"
    )


def code(text):
    return (f'<!-- wp:code -->\n<pre class="wp-block-code"><code>{text}</code>'
            "</pre>\n<!-- /wp:code -->")


DE = "\n\n".join([
    absatz(f'<a href="https://dialos.org/en/{EN_SLUG}/">English</a>',
           "dialos-lang-marker"),

    absatz("<strong>DialOS Mobil ist eine Android-App, mit der man allein "
           "durch Sprechen telefonieren kann.</strong> Sie richtet sich an "
           "blinde und an motorisch stark eingeschränkte Menschen – an alle "
           "also, für die ein Bildschirm voller Schaltflächen keine "
           "Bedienoberfläche ist."),

    absatz("Die Spracherkennung läuft <strong>vollständig auf dem "
           "Gerät</strong>. Nichts wird an einen Dienst geschickt, nichts "
           "wird mitgehört, nichts ausgewertet. Die App hat nicht einmal die "
           "Berechtigung, ins Internet zu gehen – das lässt sich in den "
           "Android-Einstellungen nachsehen."),

    ueberschrift("Die App herunterladen"),

    absatz("Im Play Store ist DialOS Mobil noch nicht öffentlich zu finden. "
           "Dort läuft der von Google vorgeschriebene geschlossene Test, und "
           "ohne Einladung kommt niemand an die App heran. Wer sie trotzdem "
           "ausprobieren möchte, lädt sie hier direkt herunter."),

    knopf(f"DialOS Mobil {VERSION} herunterladen ({GROESSE})", APK_URL),

    absatz("Die Datei ist groß, weil das deutsche Sprachmodell mit rund 46 MB "
           "<em>in</em> der App steckt. Das ist Absicht: Nachladen würde "
           "bedeuten, dass die App ins Internet darf – und genau das soll sie "
           "nicht."),

    absatz("Sie ist mit <strong>demselben Schlüssel signiert wie die Fassung "
           "im Play Store</strong>. Wer später über den Play Store "
           "aktualisiert, muss deshalb nichts deinstallieren und verliert "
           "keine Einstellungen."),

    ueberschrift("Vor dem Installieren", 3),

    absatz("Android fragt beim Installieren aus einer anderen Quelle als dem "
           "Play Store einmal nach, ob das in Ordnung ist. Das ist normal und "
           "lässt sich für den Browser freigeben. Wer die Datei vorher prüfen "
           "möchte, vergleicht ihre Prüfsumme:"),

    code(f"sha256sum DialOS-Mobil-{VERSION}.apk\n{SHA256}"),

    absatz(f'<a href="{RELEASE_URL}">Alle Angaben zu dieser Fassung</a> – was '
           "sich geändert hat, der Fingerabdruck des Signaturzertifikats und "
           "der vollständige Quelltext."),

    ueberschrift("Was du wissen solltest"),

    absatz("<strong>Das ist eine Testfassung.</strong> Die Versionsnummer sagt "
           "es: 0.6.17, nicht 1.0. Die App wird täglich benutzt und hat schon "
           "einiges hinter sich, aber die Rückmeldungen der Testerinnen und "
           "Tester verändern sie weiterhin spürbar – zuletzt zum Beispiel, "
           "weil sie sich beim Vorlesen selbst zugehört und davon aufgeweckt "
           "hat."),

    absatz("Was belegt funktioniert und was noch nie mit einer echten Stimme "
           "geprüft wurde, steht offen in der "
           '<a href="https://github.com/Stephan-Lefty/DialOS-Mobil">'
           "Projektbeschreibung</a>. Dort steht auch, warum die App keine "
           "Kurznachrichten verschickt und warum sie sich nicht als "
           "Standard-Telefon-App einträgt."),

    absatz("Rückmeldungen sind willkommen und ändern tatsächlich etwas. Der "
           'Weg dafür steht auf der <a href="https://dialos.org/kontakt/">'
           "Kontaktseite</a>."),
])

EN = "\n\n".join([
    absatz(f'<a href="https://dialos.org/{DE_SLUG}/">Deutsch</a>',
           "dialos-lang-marker"),

    absatz("<strong>DialOS Mobil is an Android app that lets you make phone "
           "calls by speaking alone.</strong> It is built for blind people and "
           "for people with severe motor impairments – for everyone, in other "
           "words, to whom a screen full of buttons is not a user interface."),

    absatz("Speech recognition runs <strong>entirely on the device</strong>. "
           "Nothing is sent to a service, nothing is listened in on, nothing "
           "is analysed. The app does not even hold the permission to access "
           "the internet – you can check that in the Android settings."),

    ueberschrift("Download the app"),

    absatz("DialOS Mobil is not publicly listed on the Play Store yet. The "
           "closed test that Google requires is still running, and without an "
           "invitation nobody can reach the app there. If you would like to "
           "try it anyway, download it directly here."),

    knopf(f"Download DialOS Mobil {VERSION} ({GROESSE})", APK_URL),

    absatz("The file is large because the German speech model, roughly 46 MB, "
           "sits <em>inside</em> the app. That is deliberate: downloading it "
           "later would mean the app needs internet access – and that is "
           "exactly what it must not have."),

    absatz("It is signed with <strong>the same key as the Play Store "
           "build</strong>. Anyone who later updates through the Play Store "
           "therefore does not have to uninstall anything and loses no "
           "settings."),

    ueberschrift("Before you install", 3),

    absatz("Android asks once whether installing from a source other than the "
           "Play Store is acceptable. That is normal and can be allowed for "
           "your browser. If you want to check the file first, compare its "
           "checksum:"),

    code(f"sha256sum DialOS-Mobil-{VERSION}.apk\n{SHA256}"),

    absatz(f'<a href="{RELEASE_URL}">Everything about this build</a> – what '
           "changed, the signing certificate fingerprint, and the full source "
           "code."),

    ueberschrift("What you should know"),

    absatz("<strong>This is a test build.</strong> The version number says so: "
           "0.6.17, not 1.0. The app is in daily use and has been through a "
           "fair amount, but feedback from testers still changes it noticeably "
           "– most recently because it listened to its own announcements and "
           "woke itself up from them."),

    absatz("What is demonstrably working, and what has never been tried with a "
           "real voice, is stated openly in the "
           '<a href="https://github.com/Stephan-Lefty/DialOS-Mobil">project '
           "description</a>. It also explains why the app sends no text "
           "messages and why it does not register itself as the default phone "
           "app."),

    absatz("Feedback is welcome and genuinely changes things. The way to send "
           'it is on the <a href="https://dialos.org/en/contact/">contact '
           "page</a>."),
])


def anlegen(titel, slug, inhalt, eltern=None):
    nutzlast = {"title": titel, "slug": slug, "content": inhalt,
                "status": "publish"}
    if eltern:
        nutzlast["parent"] = eltern
    d = call("POST", "wp/v2/pages", nutzlast)
    return d


def main():
    schreiben = "--schreiben" in sys.argv

    vorhanden = call("GET", "wp/v2/pages?per_page=100&_fields=id,slug")
    belegt = {s["slug"] for s in vorhanden}
    for slug in (DE_SLUG, EN_SLUG):
        if slug in belegt:
            print(f"ABBRUCH: Slug {slug} gibt es schon.", file=sys.stderr)
            return 1

    print(f"Deutsch: {DE_TITEL} (/{DE_SLUG}/), {len(DE)} Zeichen")
    print(f"Englisch: {EN_TITEL} (/en/{EN_SLUG}/), {len(EN)} Zeichen, "
          f"Elternseite {EN_ELTERN}")

    if not schreiben:
        print("\nProbelauf. Zum Schreiben mit --schreiben aufrufen.")
        return 0

    de = anlegen(DE_TITEL, DE_SLUG, DE)
    print(f"  deutsch angelegt: ID {de['id']}  {de['link']}")
    en = anlegen(EN_TITEL, EN_SLUG, EN, eltern=EN_ELTERN)
    print(f"  englisch angelegt: ID {en['id']}  {en['link']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
