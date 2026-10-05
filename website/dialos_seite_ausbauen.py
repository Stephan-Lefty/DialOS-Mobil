#!/usr/bin/env python3
"""Baut die Download-Seiten von DialOS Mobil zu richtigen App-Seiten aus.

Stephans Vorgabe vom 05.10.2026: Die Seite soll nach der App heissen, kurz
erklaeren, was fuer eine App das ist, und auf den Beitrag sowie die
Datenschutzerklaerung verweisen.

Was passiert:
  - Titel 'DialOS Mobil herunterladen' -> 'DialOS Mobil', Kennung
    'dialos-mobil-herunterladen' -> 'dialos-mobil' (englisch entsprechend)
  - Inhalt neu geschrieben: was die App ist, wie ein Anruf ablaeuft, was sie
    besonders macht, dann erst der Download
  - Verweise auf Tester-Aufruf, Datenschutzerklaerung und Quelltext
  - Die beiden Startseiten zeigen auf die neue Kennung
  - Der Menueeintrag folgt von selbst, er haengt an der Seiten-ID

Schreibweise: Auf der ganzen Seite heisst die App **DialOS Mobil** ohne
Bindestrich - so steht es in der Datenschutzerklaerung, im Tester-Aufruf und
auf beiden Startseiten. Deshalb hier auch ohne.

Ohne --schreiben nur Probelauf.
"""
import sys
from wp_zugang import call

DE_ID = 720
EN_ID = 721
DE_SLUG = "dialos-mobil"
EN_SLUG = "dialos-mobil"
TITEL = "DialOS Mobil"

DE_URL = f"https://dialos.org/{DE_SLUG}/"
EN_URL = f"https://dialos.org/en/{EN_SLUG}/"

VERSION = "0.6.17"
GROESSE = "54 MB"
APK_URL = ("https://github.com/Stephan-Lefty/DialOS-Mobil/releases/download/"
           "v0.6.17/DialOS-Mobil-0.6.17.apk")
RELEASE_URL = "https://github.com/Stephan-Lefty/DialOS-Mobil/releases/tag/v0.6.17"
SHA256 = "088db5857c0acea30a2961fca9f27fa0cb6b65f8594142dd64871e4f5eda2fe6"

ALT_DE_URL = "https://dialos.org/dialos-mobil-herunterladen/"
ALT_EN_URL = "https://dialos.org/en/download-dialos-mobil/"


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
    return ("<!-- wp:buttons -->\n<div class=\"wp-block-buttons\">\n"
            "<!-- wp:button -->\n"
            f'<div class="wp-block-button"><a class="wp-block-button__link '
            f'wp-element-button" href="{url}">{text}</a></div>\n'
            "<!-- /wp:button -->\n</div>\n<!-- /wp:buttons -->")


def code(text):
    return (f'<!-- wp:code -->\n<pre class="wp-block-code"><code>{text}</code>'
            "</pre>\n<!-- /wp:code -->")


def liste(punkte):
    zeilen = "\n".join(f"<!-- wp:list-item -->\n<li>{p}</li>\n"
                       "<!-- /wp:list-item -->" for p in punkte)
    return ('<!-- wp:list -->\n<ul class="wp-block-list">\n' + zeilen +
            "\n</ul>\n<!-- /wp:list -->")


DE_ABLAUF = (
    "Nutzer:  „Sprachsteuerung starten“\n"
    "App:     „Sprachsteuerung bereit. Wen möchten Sie anrufen?“\n"
    "Nutzer:  „Max Mustermann anrufen“\n"
    "App:     „Soll ich Max Mustermann auf Mobil anrufen?\n"
    "          Sagen Sie Ja oder Nein.“\n"
    "Nutzer:  „Ja“\n"
    "App:     „Ich rufe Max Mustermann an.“"
)

EN_ABLAUF = (
    "User:  “Sprachsteuerung starten”   (start voice control)\n"
    "App:   “Voice control ready. Who would you like to call?”\n"
    "User:  “Call Max Mustermann”\n"
    "App:   “Shall I call Max Mustermann on mobile?\n"
    "        Say yes or no.”\n"
    "User:  “Yes”\n"
    "App:   “Calling Max Mustermann.”"
)

DE = "\n\n".join([
    absatz(f'<a href="{EN_URL}">English</a>', "dialos-lang-marker"),

    absatz("<strong>DialOS Mobil ist eine Android-App, mit der man allein "
           "durch Sprechen telefonieren kann.</strong> Gedacht für blinde und "
           "für motorisch stark eingeschränkte Menschen – für alle also, die "
           "ein Telefon nicht bedienen können, aber angerufen werden wollen "
           "und selbst anrufen möchten."),

    absatz("Sie ist der Handy-Ableger von DialOS und benutzt dieselbe "
           "Spracherkennung wie der Desktop. Die läuft <strong>vollständig "
           "auf dem Gerät</strong>: Es verlässt kein Ton das Telefon, die App "
           "braucht weder Internet noch Google-Dienste – sie hat nicht einmal "
           "die Berechtigung, ins Netz zu gehen. Das lässt sich in den "
           "Android-Einstellungen nachsehen, und genau dafür ist die Liste "
           "der Berechtigungen ja da."),

    ueberschrift("So läuft ein Anruf"),

    code(DE_ABLAUF),

    absatz("Statt einen Namen zu nennen, lässt sich auch eine Nummer "
           "diktieren – Ziffer für Ziffer, und die App liest nach jeder "
           "Gruppe zurück. Jederzeit möglich sind „Abbrechen“, „Hilfe“, "
           "„Wiederholen“ und „Sprachsteuerung beenden“. Bleibt es fünfzehn "
           "Sekunden still, beendet die App das Gespräch von selbst und "
           "wartet wieder auf das Aktivierungswort."),

    ueberschrift("Was die App ausmacht"),

    liste([
        "<strong>Eine Kontaktsuche, die Verhörer verzeiht.</strong> Drei "
        "Verfahren greifen ineinander – unter anderem die Kölner Phonetik, "
        "damit <em>Meier</em>, <em>Maier</em>, <em>Mayer</em> und "
        "<em>Meyer</em> denselben Kontakt finden.",

        "<strong>Rückfrage statt Raten.</strong> Passen mehrere Kontakte, "
        "liest die App die Vorschläge nummeriert vor. Vor dem Wählen fragt "
        "sie nach – eine Fehlerkennung soll keinen Fehlanruf auslösen. Wer "
        "das nicht möchte, schaltet es ab.",

        "<strong>Ein Balken über die volle Bildschirmbreite</strong> statt "
        "eines App-Symbols. Zwischen zwanzig anderen Symbolen ist ein Symbol "
        "kein brauchbares Ziel, wenn man den Bildschirm nicht sieht oder die "
        "Hand nicht ruhig halten kann.",

        "<strong>Vier Wege, das Gespräch zu starten:</strong> das "
        "Aktivierungswort, die große Schaltfläche in der App, eine Kachel in "
        "den Schnelleinstellungen oder die Assistenten-Geste.",

        "<strong>Eine Oberfläche, die mitdenkt:</strong> sehr große "
        "Schaltflächen, eine kontraststarke Fassung, und die Statusanzeige "
        "ist als Live-Bereich ausgezeichnet – TalkBack liest jede Änderung "
        "mit vor. Der Zustand wechselt über Farbe <em>und</em> Text, denn "
        "Farbe allein reicht nicht.",
    ]),

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

    ueberschrift("Was du wissen solltest"),

    absatz("<strong>Das ist eine Testfassung.</strong> Die Versionsnummer "
           "sagt es: 0.6.17, nicht 1.0. Die App wird täglich benutzt und hat "
           "einiges hinter sich, aber die Rückmeldungen der Testerinnen und "
           "Tester verändern sie weiterhin spürbar – zuletzt zum Beispiel, "
           "weil sie sich beim Vorlesen selbst zugehört und davon aufgeweckt "
           "hat."),

    absatz("Zwei Dinge kann die App bewusst <em>nicht</em>: Kurznachrichten "
           "verschicken und sich als Standard-Telefon-App eintragen. Beides "
           "war gebaut und ist wieder herausgenommen worden; warum, steht "
           "offen in der Projektbeschreibung – zusammen mit dem, was noch nie "
           "mit einer echten Stimme geprüft wurde."),

    ueberschrift("Weiterlesen"),

    liste([
        '<a href="https://dialos.org/dialos-mobil-tester-gesucht/">DialOS '
        "Mobil sucht Testerinnen und Tester</a> – warum es den geschlossenen "
        "Test überhaupt braucht und wie man mitmacht.",

        '<a href="https://dialos.org/dialos-mobil-datenschutz/">'
        "Datenschutzerklärung für DialOS Mobil</a> – im Einzelnen, warum die "
        "App technisch gar nichts senden kann.",

        '<a href="https://dialos.org/neuigkeiten/">Neuigkeiten</a> – die '
        "Entwicklungsberichte, auch die über die Fehler.",

        '<a href="https://github.com/Stephan-Lefty/DialOS-Mobil">Quelltext '
        "und Projektbeschreibung</a> auf GitHub, unter der Apache-Lizenz 2.0.",

        f'<a href="{RELEASE_URL}">Alle Angaben zu Fassung {VERSION}</a> – was '
        "sich geändert hat und der Fingerabdruck des Signaturzertifikats.",
    ]),
])

EN = "\n\n".join([
    absatz(f'<a href="{DE_URL}">Deutsch</a>', "dialos-lang-marker"),

    absatz("<strong>DialOS Mobil is an Android app that lets you make phone "
           "calls by speaking alone.</strong> It is built for blind people and "
           "for people with severe motor impairments – for everyone, in other "
           "words, who cannot operate a phone but wants to be called and to "
           "call others."),

    absatz("It is the phone companion to DialOS and uses the same speech "
           "recognition as the desktop. That runs <strong>entirely on the "
           "device</strong>: no audio leaves the phone, the app needs neither "
           "the internet nor Google services – it does not even hold the "
           "permission to go online. You can check that in the Android "
           "settings, which is precisely what the permission list is for."),

    absatz("<em>The app speaks and understands German only.</em>"),

    ueberschrift("How a call works"),

    code(EN_ABLAUF),

    absatz("Instead of naming a contact you can dictate a number, digit by "
           "digit, and the app reads each group back. “Cancel”, “Help”, "
           "“Repeat” and “Stop voice control” work at any point. After "
           "fifteen seconds of silence the app ends the dialogue by itself "
           "and goes back to waiting for the wake phrase."),

    ueberschrift("What makes it different"),

    liste([
        "<strong>Contact matching that forgives mishearing.</strong> Three "
        "methods work together – among them Cologne phonetics, so that "
        "<em>Meier</em>, <em>Maier</em>, <em>Mayer</em> and <em>Meyer</em> all "
        "find the same contact.",

        "<strong>Asking instead of guessing.</strong> If several contacts "
        "match, the app reads the suggestions out, numbered. Before dialling "
        "it asks for confirmation – a misrecognition must not trigger a wrong "
        "call. Anyone who finds that tedious can switch it off.",

        "<strong>A bar across the full width of the screen</strong> instead of "
        "an app icon. Among twenty other icons, an icon is not a usable "
        "target if you cannot see the screen or hold your hand steady.",

        "<strong>Four ways to start a dialogue:</strong> the wake phrase, the "
        "large button in the app, a tile in the quick settings, or the "
        "assistant gesture.",

        "<strong>An interface that thinks along:</strong> very large buttons, "
        "a high-contrast variant, and the status display is marked up as a "
        "live region – TalkBack reads out every change. The state changes "
        "through colour <em>and</em> text, because colour alone is not enough.",
    ]),

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

    ueberschrift("What you should know"),

    absatz("<strong>This is a test build.</strong> The version number says so: "
           "0.6.17, not 1.0. The app is in daily use and has been through a "
           "fair amount, but feedback from testers still changes it noticeably "
           "– most recently because it listened to its own announcements and "
           "woke itself up from them."),

    absatz("Two things the app deliberately cannot do: send text messages, and "
           "register itself as the default phone app. Both were built and then "
           "taken out again; the reasons are stated openly in the project "
           "description – together with what has never been tried with a real "
           "voice."),

    ueberschrift("Further reading"),

    liste([
        '<a href="https://dialos.org/en/dialos-mobil-testers-wanted/">DialOS '
        "Mobil is looking for testers</a> – why the closed test is needed at "
        "all, and how to take part.",

        '<a href="https://dialos.org/en/dialos-mobil-privacy-policy/">Privacy '
        "policy for DialOS Mobil</a> – in detail, why the app is technically "
        "unable to send anything.",

        '<a href="https://dialos.org/en/news/">News</a> – the development '
        "reports, including the ones about the mistakes.",

        '<a href="https://github.com/Stephan-Lefty/DialOS-Mobil">Source code '
        "and project description</a> on GitHub, under the Apache 2.0 licence.",

        f'<a href="{RELEASE_URL}">Everything about build {VERSION}</a> – what '
        "changed and the signing certificate fingerprint.",
    ]),
])

STARTSEITEN = [
    (2, "Startseite deutsch", ALT_DE_URL, DE_URL),
    (135, "Startseite englisch", ALT_EN_URL, EN_URL),
]


def main():
    schreiben = "--schreiben" in sys.argv

    for pid, titel, slug, inhalt, name in (
        (DE_ID, TITEL, DE_SLUG, DE, "deutsch"),
        (EN_ID, TITEL, EN_SLUG, EN, "englisch"),
    ):
        d = call("GET", f"wp/v2/pages/{pid}?context=edit")
        print(f"  Seite {name} ({pid}): „{d['title']['raw']}“ /{d['slug']}/ "
              f"-> „{titel}“ /{slug}/, {len(inhalt)} Zeichen")
        if schreiben:
            r = call("POST", f"wp/v2/pages/{pid}",
                     {"title": titel, "slug": slug, "content": inhalt})
            print(f"    gespeichert: {r['link']}")

    for pid, name, alt, neu in STARTSEITEN:
        d = call("GET", f"wp/v2/pages/{pid}?context=edit")
        inhalt = d["content"]["raw"]
        treffer = inhalt.count(alt)
        if treffer != 1:
            print(f"ABBRUCH: {name} - alte Adresse {treffer}-mal gefunden, "
                  "erwartet genau 1.", file=sys.stderr)
            return 1
        print(f"  {name}: Verweis {alt} -> {neu}")
        if schreiben:
            call("POST", f"wp/v2/pages/{pid}",
                 {"content": inhalt.replace(alt, neu)})
            print("    gespeichert")

    if not schreiben:
        print("\nProbelauf. Zum Schreiben mit --schreiben aufrufen.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
