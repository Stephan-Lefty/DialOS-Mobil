#!/usr/bin/env python3
"""Ersetzt den einen Satz "Vor dem Installieren" auf den Seiten 720 und 721
durch eine echte Schritt-fuer-Schritt-Anleitung.

Stephans Frage vom 06.10.2026: "Haben wir auf beiden Seiten auch eine
Installationsanleitung fuer den Direkt-Download auf dem Handy?" - Nein,
hatten wir nicht. Da stand ein Satz ("Android fragt beim Installieren
einmal nach"), und die Pruefsumme kam mit einem Befehl daher, den es auf
dem Telefon gar nicht gibt. Genau dort wird die Seite aber aufgerufen.

ALLE Geraetetexte in dieser Anleitung sind nachgesehen, nicht erinnert.
Quelle: Motorola edge 50 neo, Android 16, Patchstand 2026-07-01.

  * Dialogtexte aus den Ressourcen von
    /system/priv-app/GooglePackageInstaller/GooglePackageInstaller.apk,
    ausgelesen mit aapt2 - deutsche und englische Fassung aus derselben
    Datei, deshalb passen beide Seiten zum jeweiligen Telefon.
  * "Unbekannte Apps installieren" sowie "Zulaessig"/"Nicht zulaessig"
    aus einem uiautomator-Abzug des echten Bildschirms. Die Settings-
    Ressourcen sagen an dieser Stelle "Zugelassen"/"Nicht zugelassen" -
    angezeigt wird aber die andere Fassung. Wer das nachbaut: dem Abzug
    glauben, nicht den Ressourcen.
  * Mindest-Android aus dem ausgelieferten APK selbst (uses-sdk
    minSdkVersion=26), nicht aus der build.gradle.kts.
  * Platzbedarf gemessen: du -sk auf dem Installationsverzeichnis ergab
    59.847 KB. Daraus die Angabe "rund 60 MB", und waehrend der
    Installation zusaetzlich die 54 MB der Datei selbst.

Die Anleitung richtet sich bewusst auch an eine helfende Person. Das
Installieren ist der einzige Teil, der Blicke auf den Bildschirm
verlangt - und die Zielgruppe der App sind Menschen, die genau das nicht
koennen.

Ohne --schreiben nur Probelauf.
"""
import sys
from wp_zugang import call

DATEI = "DialOS-Mobil-0.6.17.apk"
SUMME = "088db5857c0acea30a2961fca9f27fa0cb6b65f8594142dd64871e4f5eda2fe6"

# ---------------------------------------------------------------- deutsch

ALT_DE = (
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">Vor dem Installieren</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>Android fragt beim Installieren aus einer anderen Quelle als dem "
    "Play Store einmal nach, ob das in Ordnung ist. Das ist normal und "
    "lässt sich für den Browser freigeben. Wer die Datei vorher "
    "prüfen möchte, vergleicht ihre Prüfsumme:</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:code -->\n"
    f"<pre class=\"wp-block-code\"><code>sha256sum {DATEI}\n"
    f"{SUMME}</code></pre>\n"
    "<!-- /wp:code -->"
)

NEU_DE = (
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">Schritt für Schritt installieren</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>Das Installieren ist der einzige Teil, der Blicke auf den Bildschirm "
    "und eine ruhige Hand verlangt – danach nicht mehr. Wer die App für "
    "jemanden einrichtet, der nicht sehen oder nicht zielsicher tippen kann, "
    "übernimmt deshalb besser diese fünf Schritte.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p><strong>Das Telefon braucht:</strong> Android 8.0 oder neuer und "
    "einen ARM-Prozessor – das haben alle gängigen Telefone. Dazu rund "
    "120 MB freien Speicher während der Installation; danach belegt die "
    "App rund 60 MB, die heruntergeladene Datei kann weg.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:list {\"ordered\":true} -->\n"
    "<ol class=\"wp-block-list\">\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Datei laden.</strong> Oben auf den Knopf tippen. Der Browser "
    "legt sie in den Ordner „Downloads“ und fragt dabei womöglich "
    "nach, ob eine solche Datei wirklich behalten werden soll – ja, "
    "soll sie.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Datei antippen.</strong> Entweder gleich in der Meldung des "
    f"Browsers oder später in der App „Dateien“ unter "
    f"„Downloads“. Sie heißt <code>{DATEI}</code>.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Die Quelle einmal freigeben.</strong> Beim ersten Mal meldet "
    "Android: <em>„Aus Sicherheitsgründen kannst du auf deinem "
    "Smartphone zurzeit keine unbekannten Apps aus dieser Quelle "
    "installieren. Das kannst du in den Einstellungen ändern.“</em> "
    "Auf <strong>Einstellungen</strong> tippen. Es öffnet sich "
    "„Unbekannte Apps installieren“, und zwar direkt beim richtigen "
    "Browser. Den Schalter von „Nicht zulässig“ auf "
    "„Zulässig“ stellen und zurückgehen.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Installieren.</strong> Jetzt fragt Android „Diese App "
    "installieren?“ – auf <strong>Installieren</strong> tippen. Nach "
    "ein paar Sekunden steht da „App installiert“.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Die Freigabe zurücknehmen.</strong> Nicht nötig, "
    "aber sauber: denselben Schalter aus Schritt 3 wieder auf „Nicht "
    "zulässig“ stellen. Die installierte App bleibt davon "
    "unberührt.</li>\n"
    "<!-- /wp:list-item -->\n"
    "</ol>\n"
    "<!-- /wp:list -->\n"
    "\n"
    "<!-- wp:paragraph {\"fontSize\":\"small\"} -->\n"
    "<p class=\"has-small-font-size\">Diese Formulierungen sind an einem "
    "Motorola mit Android 16 abgelesen. Bei anderen Herstellern und "
    "älteren Android-Fassungen heißen die Knöpfe etwas anders – "
    "der Weg ist derselbe.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">Wenn es nicht klappt</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:list -->\n"
    "<ul class=\"wp-block-list\">\n"
    "<!-- wp:list-item -->\n"
    "<li><em>„Die App wurde nicht installiert, da sie nicht mit deinem "
    "Smartphone kompatibel ist.“</em> – Android ist älter als "
    "8.0, oder das Gerät hat keinen ARM-Prozessor. Letzteres betrifft in "
    "der Praxis nur Emulatoren.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><em>„Die App wurde nicht installiert, da das Paket in Konflikt "
    "mit einem bestehenden Paket steht.“</em> – Es ist schon ein "
    "DialOS Mobil installiert, das aus einer anderen Quelle stammt. Das muss "
    "erst deinstalliert werden, und dabei gehen seine Einstellungen verloren. "
    "Bei der Fassung von dieser Seite und der aus dem Play Store passiert "
    "das nicht: Beide sind mit demselben Schlüssel signiert.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><em>„App-Entwickler nicht überprüft“</em> – "
    "dazu der letzte Absatz.</li>\n"
    "<!-- /wp:list-item -->\n"
    "</ul>\n"
    "<!-- /wp:list -->\n"
    "\n"
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">Die Prüfsumme</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>Der Fingerabdruck der Datei lautet:</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:code -->\n"
    f"<pre class=\"wp-block-code\"><code>{SUMME}</code></pre>\n"
    "<!-- /wp:code -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>Vergleichen ließe er sich am Rechner mit <code>sha256sum "
    f"{DATEI}</code>. Auf dem Telefon selbst gibt es dafür nichts "
    "Eingebautes – man bräuchte dazu eine weitere App, was den Zweck "
    "verfehlt. Wer direkt auf dem Telefon installiert, verlässt sich "
    "also auf die Quelle. Deshalb steht dieser Link nur hier und auf "
    "GitHub.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">Was sich 2027 ändert</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>Google baut eine Entwicklerbestätigung in Android ein: Apps "
    "sollen sich nur noch installieren lassen, wenn der Entwickler bei "
    "Google hinterlegt ist. Seit Ende September 2026 gilt das in vier "
    "Ländern, 2027 soll es auf zertifizierten Geräten überall "
    "greifen. Vorbereitet ist es längst – in Android 16 stecken die "
    "Texte dafür schon drin, samt der Wahl „Ohne Überprüfung "
    "installieren“. DialOS Mobil ist über den Play Store "
    "bestätigt; dass der Weg über diese Seite davon unberührt "
    "bleibt, ist aber nicht zugesagt. Eine Pointe am Rande: Diese "
    "Prüfung braucht Internet. Eine App, die ausdrücklich ohne Netz "
    "arbeitet, wäre dann nur mit Netz zu installieren.</p>\n"
    "<!-- /wp:paragraph -->"
)

# ---------------------------------------------------------------- englisch

ALT_EN = (
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">Before you install</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>Android asks once whether installing from a source other than the "
    "Play Store is acceptable. That is normal and can be allowed for your "
    "browser. If you want to check the file first, compare its "
    "checksum:</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:code -->\n"
    f"<pre class=\"wp-block-code\"><code>sha256sum {DATEI}\n"
    f"{SUMME}</code></pre>\n"
    "<!-- /wp:code -->"
)

NEU_EN = (
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">Installing, step by step</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>Installing is the one part that needs eyes on the screen and a "
    "steady hand – after that it does not. If you are setting the app up "
    "for someone who cannot see the screen or cannot tap accurately, it is "
    "better that you do these five steps yourself.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p><strong>The phone needs:</strong> Android 8.0 or newer and an ARM "
    "processor – every common phone has one. Plus roughly 120 MB of free "
    "storage while installing; afterwards the app takes about 60 MB and the "
    "downloaded file can go.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:list {\"ordered\":true} -->\n"
    "<ol class=\"wp-block-list\">\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Download the file.</strong> Tap the button above. Your "
    "browser puts it in the “Downloads” folder and may ask whether "
    "you really want to keep a file like this – you do.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Tap the file.</strong> Either straight from the browser's "
    "notification or later in the “Files” app under "
    f"“Downloads”. It is called <code>{DATEI}</code>.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Allow the source, once.</strong> The first time, Android "
    "says: <em>“For your security, your phone currently isn't allowed to "
    "install unknown apps from this source. You can change this in "
    "Settings.”</em> Tap <strong>Settings</strong>. “Install unknown "
    "apps” opens, already showing the right browser. Switch "
    "“Allow from this source” from “Not allowed” to "
    "“Allowed” and go back.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Install.</strong> Android now asks “Install this "
    "app?” – tap <strong>Install</strong>. A few seconds later it "
    "says “App installed”.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><strong>Take the permission back.</strong> Not required, but tidy: "
    "set the same switch from step 3 back to “Not allowed”. The app "
    "you just installed is not affected.</li>\n"
    "<!-- /wp:list-item -->\n"
    "</ol>\n"
    "<!-- /wp:list -->\n"
    "\n"
    "<!-- wp:paragraph {\"fontSize\":\"small\"} -->\n"
    "<p class=\"has-small-font-size\">These wordings were read off a Motorola "
    "running Android 16. Other manufacturers and older Android versions "
    "label the buttons slightly differently – the path is the same.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">If it does not work</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:list -->\n"
    "<ul class=\"wp-block-list\">\n"
    "<!-- wp:list-item -->\n"
    "<li><em>“App not installed as app isn't compatible with your "
    "phone.”</em> – Android is older than 8.0, or the device has no "
    "ARM processor. In practice the latter only affects emulators.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><em>“App not installed as package conflicts with an existing "
    "package.”</em> – There is already a DialOS Mobil on the phone "
    "that came from somewhere else. It has to be uninstalled first, and its "
    "settings go with it. Between the build on this page and the one from "
    "the Play Store this does not happen: both are signed with the same "
    "key.</li>\n"
    "<!-- /wp:list-item -->\n"
    "<!-- wp:list-item -->\n"
    "<li><em>“App developer unverified”</em> – see the last "
    "paragraph.</li>\n"
    "<!-- /wp:list-item -->\n"
    "</ul>\n"
    "<!-- /wp:list -->\n"
    "\n"
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">The checksum</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>The file's fingerprint is:</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:code -->\n"
    f"<pre class=\"wp-block-code\"><code>{SUMME}</code></pre>\n"
    "<!-- /wp:code -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>On a computer you could compare it with <code>sha256sum "
    f"{DATEI}</code>. On the phone itself there is nothing built in for that "
    "– it would take yet another app, which rather defeats the purpose. "
    "So if you install straight from the phone, you are trusting the source. "
    "That is why this link exists only here and on GitHub.</p>\n"
    "<!-- /wp:paragraph -->\n"
    "\n"
    "<!-- wp:heading {\"level\":3} -->\n"
    "<h3 class=\"wp-block-heading\">What changes in 2027</h3>\n"
    "<!-- /wp:heading -->\n"
    "\n"
    "<!-- wp:paragraph -->\n"
    "<p>Google is building developer verification into Android: apps will "
    "only install if the developer is registered with Google. Since late "
    "September 2026 this applies in four countries, and in 2027 it is meant "
    "to take effect on certified devices everywhere. The groundwork is "
    "already laid – Android 16 carries the texts for it, including the "
    "option to “Install without verifying”. DialOS Mobil is verified "
    "through the Play Store; whether the route via this page stays unaffected "
    "has not been promised. One aside worth noting: that check needs an "
    "internet connection. An app that deliberately works without a network "
    "would then only be installable with one.</p>\n"
    "<!-- /wp:paragraph -->"
)

EINGRIFFE = [
    (720, "deutsch", ALT_DE, NEU_DE),
    (721, "englisch", ALT_EN, NEU_EN),
]


def main():
    schreiben = "--schreiben" in sys.argv
    for pid, name, alt, neu in EINGRIFFE:
        r = call("GET", f"wp/v2/pages/{pid}?context=edit")
        d = r[0] if isinstance(r, tuple) else r
        inhalt = d["content"]["raw"]

        if neu in inhalt:
            print(f"  {name} ({pid}): Anleitung steht schon")
            continue
        treffer = inhalt.count(alt)
        if treffer != 1:
            print(f"ABBRUCH: {name} ({pid}) - Anker {treffer}-mal gefunden, "
                  "erwartet genau 1. Nichts geschrieben.", file=sys.stderr)
            return 1
        print(f"  {name} ({pid}): Anker passt, {len(neu) - len(alt):+d} Zeichen")
        if schreiben:
            call("POST", f"wp/v2/pages/{pid}",
                 {"content": inhalt.replace(alt, neu)})
            print("    gespeichert")

    if not schreiben:
        print("\nProbelauf. Zum Schreiben mit --schreiben aufrufen.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
