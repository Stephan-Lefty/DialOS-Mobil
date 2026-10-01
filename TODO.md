[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – DialOS Mobil

## Offen

### Dringend

- [x] ~~**Nach jedem App-Update stürzte die App ab.**~~ **Gefunden und
      behoben am 01.10.2026**, nebenbei beim Prüfen der
      Unterbrechungsansage – ohne den Gerätetest wäre es niemand
      aufgefallen. Der `BootReceiver` stößt auf `MY_PACKAGE_REPLACED` den
      Dienst über `startForegroundService()` an; der Dienst erkennt den
      Hintergrundstart, zeigt die Benachrichtigung und ruft `stopSelf()` –
      aber nie `startForeground()`. Android hält ihn am Vertrag fest und
      beendet den Prozess mit `ForegroundServiceDidNotStartInTimeException`.
      **Das traf jeden Tester bei jedem Play-Update**, nicht nur den
      Entwickler. Behoben, indem der Vordergrundstart trotzdem versucht
      wird: Er scheitert sicher an einer `SecurityException` (Mikrofon-Typ
      aus dem Hintergrund), aber der gefangene Fehlschlag löst den Vertrag.
      Übrig bleibt die antippbare Benachrichtigung – was der Zweig immer
      erreichen wollte.

- [x] ~~**Test- und Play-Fassung sind am Icon nicht zu unterscheiden.**~~
      **Behoben am 01.10.2026.** Zum zweiten Mal hatte das einen Testlauf
      verfälscht: Am 01.10. sprach die parallel laufende 0.6.14 ihre
      Unterbrechungsansage, während die 0.6.15-Testfassung zuhörte und sie
      als Spracheingabe verarbeitete. Der Name unterscheidet sich seit
      0.6.15 („DialOS Mobil (Test)"), das Symbol nicht – und im
      Startbildschirm fällt der Name weit weniger auf als die Farbe.
      Die Testfassung hat jetzt einen Amber-Hintergrund
      (`app/src/debug/res/values/colors.xml`, überschreibt
      `ic_launcher_background`). Amber, weil es sich von Weiß am weitesten
      entfernt und das blau-grüne Symbol darauf lesbar bleibt; die Blau-
      und Grüntöne der Palette scheiden aus, weil sie unterscheiden sollen
      statt ähneln. Am Gerät nachgesehen.

      Nebenbefund daraus, der kein Fehler ist, sondern eine Eigenschaft:
      **Zwei DialOS-Instanzen in Hörweite wecken sich gegenseitig.** Das
      betrifft auch DialOS am PC, das auf dasselbe Wort hört, und steht
      schon als offener Punkt beim zweiten Aktivierungswort.

- [ ] **Die App aktiviert sich selbst – sie hört ihre eigene Ansage.**
      Lydias dritte Meldung (01.10.2026) nennt endlich den Wortlaut: „Sie
      redet, dass das Gerät die App abgeschaltet hat und nun wieder
      funktioniert, sie fragt dann wem möchten sie anrufen? Keine
      Aktivierung von mir." Damit ist die Ursache im Code lesbar, und es
      ist dieselbe wie bei ihrem „Eigenleben" vom 27.09.

      Die Kette:

      1. `VoiceService.onEngineReady()` ruft in Zeile 264 `engine.start()` –
         das Mikrofon läuft ab hier.
      2. Zeile 268 `sorgeFuerHoerbarkeit()` hebt die Lautstärke an.
      3. Zeile 276 bzw. 278 sprechen **direkt über `speaker.speak()`**, ohne
         das Mikrofon anzuhalten. Die Pause sitzt ausschließlich in
         `DialogController.say()` (Zeile 798) – die beiden Ansagen des
         Dienstes gehen daran vorbei.
      4. Und die Texte enthalten das Aktivierungswort wörtlich:
         `say_started_contacts` endet auf „**Sagen Sie: Sprachsteuerung
         starten.**", `say_after_interruption` beginnt mit „Die
         **Sprachsteuerung** wurde vom Telefon unterbrochen".
      5. Vosk hört mit, erkennt das Aktivierungswort aus dem eigenen
         Lautsprecher und aktiviert → „Sprachsteuerung bereit. Wen möchten
         Sie anrufen?" Genau Lydias Satz.

      **Gemessen am 01.10.2026 – und die Messung hat mehr gefunden als
      erwartet.** `SelbstausloeserTest` schickt die Ansagetexte durch
      `CommandParser.isWakePhrase`:

      | Ansage | Ähnlichkeit | weckt? |
      |---|---|---|
      | `say_started_contacts` | enthält „sprachsteuerung starten" wörtlich | ja |
      | `say_after_interruption` (alt) | „sprachsteuerung wurde" = **0,783** | **ja** |
      | `say_after_interruption` (neu) | – | nein |

      Der zweite Wert war die Überraschung: Die Unterbrechungsansage
      enthält gar kein „starten", und trotzdem weckte sie die App. Das
      Wortpaar „sprachsteuerung **wurde**" liegt gegen „sprachsteuerung
      **starten**" bei 0,783, die Schwelle steht auf 0,70. **Die
      Mikrofonpause allein hätte den Fehler also nicht behoben** – der
      Wortlaut war für sich genommen schon gefährlich.

      Die Schwelle anzuheben wäre der falsche Weg: Der einzige verpasste
      echte Ruf aus der Messung vom 21.09. („sprachstörungen starten") lag
      bei 0,78, also gleichauf. Dort ist keine Lücke.

      - [x] **Mikrofonpause nachgerüstet.** Neue Hilfsfunktion
            `VoiceService.sagOhneMitzuhoeren()`, beide Ansagen laufen jetzt
            darüber – dieselbe Logik wie `DialogController.say()`.
      - [x] **Wortlaut entschärft.** „Die Sprachsteuerung wurde vom Telefon
            unterbrochen" → „Ich wurde vom Telefon kurz unterbrochen und
            bin jetzt wieder da." Beide Sprachdateien.
      - [x] **Vier Tests dazu** (`SelbstausloeserTest`), darunter der alte
            Wortlaut als Mahnmal. 65 Tests gesamt, grün, Lint sauber.
      - [ ] **Am Gerät gegenprüfen** (01.10. mittags): einschalten, still
            sein, und hören, ob sie noch von selbst fragt „Wen möchten Sie
            anrufen?". Das Protokoll muss zeigen, dass Vosk während der
            Ansage gar nichts mehr erkennt.
      - [ ] **Offen: die Startansage bleibt riskant.** Sie sagt bewusst
            „Sagen Sie: Sprachsteuerung starten" – als Anleitung für
            Blinde ist das wertvoll. Mit der Pause ist sie gedeckt, aber
            Nachhall oder ein zweites Gerät in Hörweite bleiben denkbar,
            und DialOS am PC hört auf dasselbe Wort. Nach der Gerätemessung
            entscheiden.

- [ ] **Die App dreht ungefragt die Lautstärke hoch.** Lydias zweiter Punkt
      vom 01.10.: „Es ist zwar gut, dass sich die App die Lautstärke
      einstellt, aber sie geht automatisch auf laut, wenn man die App nicht
      öffnen möchte." `sorgeFuerHoerbarkeit()` läuft bei **jedem**
      `onEngineReady`, also auch beim stillen Wiederanlauf nach einem
      Abschuss durch Android – ohne jede Nutzeraktion. Der Grund dafür ist
      gut (eine unhörbare Ansage ist wertlos, siehe `VolumeController`
      Zeile 39–43), die Nebenwirkung ist es nicht: Wer das Telefon bewusst
      leise gestellt hat, wird überfahren.

      Hängt am Punkt darüber: Wenn die App nach einem Wiederanlauf gar
      nicht mehr ungefragt redet, braucht sie dort auch keine Lautstärke
      anzuheben. Dann löst sich das von selbst.

- [ ] **Entscheiden, ob die Unterbrechungsansage überhaupt bleiben soll.**
      `say_after_interruption` erklärt den Akku-Optimierungs-Punkt in den
      Einstellungen. Für Lydia ist dieser Rat nicht umsetzbar, und die
      Ansage kommt unangekündigt aus einem stillen Telefon. Denkbar wäre:
      ganz weglassen und nur in der Mitteilung vermerken, oder einmal pro
      Tag statt bei jedem Wiederanlauf. Das ist eine Produktentscheidung –
      Stephan fragen, nicht selbst entscheiden.

- [x] ~~**Der ausgeschaltete Hotword-Schalter schweigt.**~~ **Gefunden und
      behoben am 21.09.2026 (0.6.14).** Die App erkannte „sprachsteuerung
      starten" zweimal einwandfrei – im Protokoll wörtlich nachzulesen – und
      tat nichts, weil in den Einstellungen „Auf ‚Sprachsteuerung starten'
      hören" ausgeschaltet war. Keine Ansage, kein Ton, keine Reaktion.
      Derselbe Fehlertyp wie 0.6.11, nur eine Ebene höher: Wer den Bildschirm
      nicht sehen kann, hat keine Möglichkeit, „hört mich nicht" von
      „ignoriert mich absichtlich" zu unterscheiden, und sucht den Fehler bei
      der eigenen Aussprache. Genau so ist die Meldung entstanden, das
      Aktivierungswort funktioniere nicht. Die Ansage beginnt deshalb mit
      „Ich habe Sie verstanden" und kommt einmal je Aus-Phase.

- [ ] **Anbieten, den Schalter per Sprache wieder einzuschalten.** Der
      Hinweis aus 0.6.14 nennt den Weg zum Schalter – aber das Navigieren
      dorthin ist genau das, was dieser Zielgruppe schwerfällt. Besser wäre:
      „Soll ich es wieder einschalten?" Das braucht einen neuen
      Dialogzustand und ist deshalb bewusst nicht mehr in 0.6.14 gegangen,
      das ohnehin 0.6.13 hinterherlief.

- [ ] **Überlegen, ob der Schalter so leicht erreichbar sein soll**, wenn
      sein Ausschalten die Kernfunktion der App stilllegt.

- [ ] **Lydia gezielt danach fragen**, ob bei ihr dieser Schalter aus ist.
      Ihre Meldung passt genau darauf, und es wäre die einfachste Erklärung.

- [ ] **Der Zähler der vergeblichen Versuche ist nur am Gerät prüfbar.**
      Der Kern von 0.6.15 – nach drei Fehlschlägen hört die App auf – hat
      **keinen** Unit-Test, weil `DialogController` einen Android-`Context`
      braucht und das Projekt nur `junit` als Testabhängigkeit hat. Der
      `CommandParser`-Teil derselben Änderung ist dagegen getestet
      (`EigenlebenTest`). Am Gerät zu prüfen: dreimal Unsinn sagen → Ansage
      und Ende; zweimal Unsinn, dann ein richtiger Name → Zählung beginnt von
      vorn; im Ziffernmodus dreimal Unsinn → die Ziffern bleiben erhalten und
      werden zur Bestätigung vorgelesen.

- [ ] **`DialogController` testbar machen.** Er ist das Herz der App und das
      einzige größere Stück ohne Test. Robolectric wäre der Weg; das ist eine
      eigene Aufgabe und keine, die man neben einer Fehlerbehebung mitmacht.

- [ ] **Es gibt keine CI.** Tests sind vorhanden, aber `.github/workflows/`
      existiert nicht – jeder Release hängt damit an einer Prüfung auf einem
      einzigen Rechner. Beim 0.6.13-Release hat sich gezeigt, wie schnell das
      schiefgeht: Ein durch `tail` geleiteter Gradle-Aufruf meldete Erfolg,
      und das begutachtete Bundle war fünf Wochen alt. Das Hindernis ist
      bekannt: `prepareVoskModel` lädt 46 MB, das braucht im Workflow einen
      Cache. Für `testDebugUnitTest` ist zu prüfen, ob das Modell überhaupt
      nötig ist – dann wäre ein schlanker Test-Workflow ohne Download möglich.

- [x] ~~**„MA40" wird nicht gefunden**~~ **Behoben in 0.6.15.** Zahlwörter
      werden jetzt zu Ziffern gemacht und zerlegte Namen wieder zusammen-
      gesetzt, beides zusätzlich und als Maximum. Offen bleibt allein „Heli",
      siehe unten.

- [ ] **„Heli" wird nicht gefunden** (Bericht Lydia Oberländer,
      27.09.2026). Ausdrücklich **nicht** die Lautstärke: „Beidemale war es
      still in meiner Umgebung." Der Zähler aus 0.6.15 hilft hier also nicht –
      er verhindert nur das endlose Nachfragen, nicht das Nichtfinden.

      **Der Fall ist anders gelagert als MA40 – und deshalb offen.** Dort war
      nachweisbar, dass es nie funktionieren konnte. Hier trafen fast alle
      plausiblen Verhörer schon vor 0.6.15 (Schwelle ist
      `NameMatcher.THRESHOLD` = 0,62):

      | gesprochen | gegen „Heli" | |
      |---|---|---|
      | `heli`, `helli`, `helly`, `eli`, `heil`, `helle`, `hely` | 0,825–1,000 | Treffer |
      | `geli` | 0,750 | Treffer |
      | `he li` | 0,800 / zusammengezogen 1,000 | Treffer |
      | `hell ich` | 0,500 / zusammengezogen 0,571 | **daneben** |
      | `heidi` | 0,600 | daneben |

      Es müsste also ein deutlicher Verhörer gewesen sein – etwa eine
      Zerlegung in zwei vollwertige Wörter wie „hell ich". Das ist eine
      Vermutung, keine Messung. **Nicht ins Blaue ändern:** Die Schwelle zu
      senken, um 0,571 einzufangen, würde Falschtreffer erzeugen, und
      Kontaktnamen mit vier Buchstaben sind gegenüber Levenshtein ohnehin
      empfindlich (ein Fehler kostet dort 0,25).

      **Was fehlt, ist der Wortlaut.** Die App nennt vor „habe ich in den
      Kontakten nicht gefunden" genau das, was sie gehört hat – die Bitte,
      das einmal zu notieren, steht in der Mail an Lydia.

- [x] ~~**Eine im Namenszustand gesagte Rufnummer wird als Name gesucht.**~~
      **Behoben in 0.6.15:** Findet die App keinen Kontakt und ergeben die
      Wörter mindestens sechs Ziffern, liest sie die Nummer zur Bestätigung
      vor. Ursprünglicher Befund:
      Lydia hat die Nummer der MA40 angesagt und „das habe ich in den
      Kontakten nicht gefunden" gehört. Wer eine Nummer sprechen will, muss
      derzeit erst „Nummer wählen" sagen – ein Zauberwort vor der
      natürlichsten Handlung. `GermanNumbers.toDigits` erkennt die Ziffern
      bereits sicher (gemessen: „null eins sieben sieben drei vier fünf sechs
      sieben acht" → `0177345678`, zehn Ziffern), während „ma vierzig" nur
      zwei ergibt – eine Schwelle von etwa sechs Ziffern trennt beides sauber.

### Zu prüfen

- [ ] **Vosk verhört die Zahlwörter – und `GermanNumbers` kennt die Verhörer
      nicht.** Am 28.09.2026 am Gerät gemessen, zwei Versuche mit derselben
      gesprochenen Folge „null eins sieben acht vier sechs":

      | gesprochen | erkannt | fehlt |
      |---|---|---|
      | null eins sieben acht vier sechs | `null ein sieben acht vier sex` | eins, sechs |
      | null eins sieben acht vier sechs | `nur eins sieben acht viel sechs` | null, vier |

      Statt sechs Ziffern kamen beide Male nur vier zusammen – unter
      `DialogController.MIN_RUFNUMMER_ZIFFERN`. Die Folge wurde deshalb als
      Name gesucht („nur eins sieben acht viel sechs habe ich in den
      Kontakten nicht gefunden"), und der anschließende Test der
      Korrigieren-Funktion lief ins Leere, weil es gar keine Ziffern gab.

      **Betrifft auch das reguläre Diktieren**, nicht nur den neuen Weg: Dort
      wird jede verhörte Ziffer mit „Das war keine Ziffer" quittiert.

      Vor dem Beheben messen, nicht raten – so wie beim Aktivierungswort am
      21.09.2026:

      - Welche Verhörer kommen **systematisch** vor? Bisher belegt: `ein`→1,
        `sex`→6, `viel`→4, `nur`→0.
      - Welche davon sind **sicher genug**? `sex` und `viel` sind im
        Adressbuchkontext harmlos, `ein` und vor allem `nur` sind
        Alltagswörter. `GermanNumbers.toDigits` läuft seit 0.6.15 auch über
        gesprochene Namen – ein zu großzügiges `nur`→0 macht aus „nur ein
        Moment" eine Rufnummer.
      - Möglicher Ausweg statt einer längeren Wortliste: Verhörer nur
        akzeptieren, wenn sie **zwischen** sicher erkannten Ziffern stehen.
        Dann ist der Kontext der Schutz, nicht das Wort.

- [ ] **Eine am Stück gesprochene Ziffernfolge erkennt Vosk nicht
      verlässlich.** Am 28.09.2026 am Gerät gemessen, dieselbe Folge „null
      eins sieben acht vier sechs", vier Versuche:

      | erkannt | brauchbar? |
      |---|---|
      | `null ein sieben acht vier sex` | ja, mit Verhörerliste |
      | `nur eins sieben acht viel sechs` | ja, mit Verhörerliste |
      | `nun alles sie beim auch für sechs` | **nein** |
      | `nun nun als sieben acht für sechs` | **nein** |

      Die letzten beiden sind keine Verhörer einzelner Zahlwörter mehr – die
      Erkennung bricht ganz zusammen. Dagegen hilft keine Wortliste.

      **Vermutete Ursache:** Vosk baut aus dem Gehörten einen sinnvollen
      Satz, und eine Ziffernfolge ist sprachlich unsinnig. Beim regulären
      Diktieren nach „Nummer wählen" werden die Ziffern einzeln mit Pausen
      gesprochen; jede Äußerung ist kurz, und das Sprachmodell kann sie nicht
      zu Wörtern verbiegen. **Ungeprüft** – zu klären wäre, ob die Erkennung
      mit Pausen auch im Namenszustand zuverlässig ist.

      **Folge für 0.6.15:** Der neue Weg „Nummer einfach sagen" ist damit
      nicht verlässlich. Er steht bereits in beiden Hilfen. Entweder er wird
      belastbar (etwa: nur mit Pausen gesprochene Folgen, oder eine eigene
      Grammatik für Ziffern), oder er muss samt Hilfetext wieder raus. Eine
      Funktion, die in der Hilfe etwas verspricht und im Alltag scheitert,
      ist schlimmer als keine – das ist derselbe Fehlertyp wie der
      schweigende Hotword-Schalter aus 0.6.14.

      **Stephans Vorschlag vom 28.09.2026, als Erster zu prüfen:** einen
      einleitenden Satz verlangen – „wähle die Nummer 0 1 7 6 8 0". Das passt
      zur vermuteten Ursache: Das Sprachmodell bekommt einen sprachlich
      sinnvollen Rahmen und muss die Ziffern nicht zu Wörtern verbiegen.
      Messen, bevor gebaut wird – dieselbe Folge mehrfach mit und ohne
      Einleitung sprechen und die Vosk-Ausgaben vergleichen. Fällt die
      Erkennung damit nicht deutlich besser aus, ist der Weg tot und die
      Funktion muss samt Hilfetext zurück.

      **Nicht veröffentlichen, bevor das entschieden ist.**

- [ ] **Handy und PC hören auf dasselbe Aktivierungswort.** Am 28.09.2026 von
      Stephan bemerkt: [DialOS](https://github.com/Stephan-Lefty/DialOS) auf
      dem Rechner und DialOS Mobil reagieren beide auf „Sprachsteuerung
      starten". Wer beides nutzt – und das ist die erklärte Zielgruppe –
      startet mit einem Satz zwei Geräte, die dann gleichzeitig sprechen.

      **Das kann schon jetzt Testpersonen betreffen** und wäre eine
      Erklärung für Berichte über unerwartetes Verhalten. Bei der nächsten
      Rundmail danach fragen, wer DialOS auch auf dem Rechner hat.

      Randbedingungen für die Lösung:

      - Das bisherige Wort **darf nicht ersetzt werden**. Zwölf Testpersonen
        sind darauf eingespielt, und eine stille Änderung wäre genau der
        Fehlertyp, den wir schon zweimal hatten: Die App hört, tut aber
        nichts, und niemand kann sich das erklären.
      - Also eine **Auswahl in den Einstellungen**, Voreinstellung bleibt
        „Sprachsteuerung starten".
      - Der zweite Ruf muss **lang genug** sein. „Handy" allein fällt im
        Alltag zu häufig; die gemessene Sicherheit von 0,70 in
        `CommandParser.WAKE_MIN_RATIO` beruht darauf, dass „sprachsteuerung
        starten" ein langes, im Gespräch seltenes Gebilde ist. Ein kurzer
        Ruf braucht eine eigene Messung, keine Übernahme dieser Schwelle.
      - Kandidaten, noch nicht geprüft: „Telefon starten", „Mobil starten",
        „Sprachsteuerung Telefon". Vor der Entscheidung mit echtem
        Raumgeräusch messen, wie beim Aktivierungswort am 21.09.2026.

- [ ] **„Abschalten" und „Sprachsteuerung ausschalten" meinen Verschiedenes.**
      Am 28.09.2026 beim Gerätetest aufgefallen: Nach dem Sprachkommando
      „abschalten" stand auf dem Bildschirm weiterhin der Knopf
      „Sprachsteuerung ausschalten" – scheinbar ein Widerspruch.

      Es ist keiner. Das Kommando beendet das **Gespräch** (zurück ins Warten
      auf das Aktivierungswort), der Knopf beendet den **Dienst**. Dass der
      Dienst weiterläuft, ist richtig und darf nicht geändert werden: Wer den
      Bildschirm nicht sehen kann, käme sonst per Sprache nicht mehr zurück.

      Falsch ist nur die Sprache drumherum. „Abschalten" weckt die Erwartung
      „ganz aus", und die Ansage danach („Abgebrochen. Sagen Sie
      Sprachsteuerung starten …") benennt den Unterschied nicht. Zu prüfen,
      ob die Ansage nach einem ausdrücklichen Abschaltwort deutlicher sein
      sollte – etwa „Ich höre jetzt nur noch auf das Aktivierungswort" – und
      ob der Knopf anders heißen müsste.

- [ ] **Funktioniert die App mit Kopfhörern?** Am 28.09.2026 von Stephan
      gefragt, als es um den Gerätetest ging – und die Frage ist größer als
      sie klingt: Viele blinde Menschen haben den ganzen Tag ein Headset auf,
      weil der Screenreader sonst mithörbar wäre. Wenn die App damit nicht
      umgeht, trifft das einen erheblichen Teil der Zielgruppe.

      **Im Code steht dazu nichts** – kein `startBluetoothSco`, keine
      Audio-Route, kein `AudioSource`. Die App überlässt Android also die
      Wahl des Mikrofons. Was dabei tatsächlich passiert, ist ungeprüft, und
      zwar getrennt für zwei Fälle: Kabel-Headset mit Mikrofon und
      Bluetooth-Headset. Beim Bluetooth-Fall ist zusätzlich offen, ob die
      Sprachausgabe im Ohr landet und das Mikrofon trotzdem am Telefon
      bleibt – dann hörte die App sich selbst nicht, was gut wäre, oder der
      Nutzer die Ansage nicht, was schlecht wäre.

      Der reguläre Gerätetest läuft bewusst **ohne** Headset: Die Testpersonen
      halten das Telefon in der Hand, und alles andere wäre nicht vergleichbar.

- [ ] **Woher kamen die vier Zählungen am 21.09.2026?** Der Zähler stieg im
      Lauf des Tages von 18 auf 23, bei drei `installDebug`-Läufen und drei
      `force-stop`.

      **Am selben Tag gemessen, und zwei Vermutungen sind damit vom Tisch:**

      - Ein `install -r` zählt **nicht** – Zähler blieb bei 23. Aber nicht,
        weil die Ausnahme aus 0.6.7 greift, sondern weil der Dienst vorher
        aussteigt: Nach dem Update startet er aus dem Hintergrund, bekommt
        kein Mikrofon und beendet sich, bevor `noteStartCause` erreicht ist
        (Protokoll: „Hintergrundstart erkannt, Mikrofon laut System
        erlaubt: false"). Die dokumentierte Begründung stimmt also nicht mit
        dem tatsächlichen Weg überein.
      - Ein `force-stop` zählt – belegt (22 → 23). Das ist richtig so.

      Offen bleibt die Rechnung: drei `force-stop` erklären drei Zählungen,
      gezählt wurden fünf. Zwei sind unerklärt. Verdacht bleibt der Pfad
      über `START_STICKY`, bei dem `intent == null` ist – dann kann
      `expected` gar nicht true werden
      (`intent?.getBooleanExtra(…) == true` ergibt bei null immer false),
      und ein erwarteter Neustart würde als Unterbrechung gelten. Ob dieser
      Pfad je erreicht wird, ist ungeprüft: Die Hintergrundstart-Sperre aus
      0.6.11 könnte ihn abfangen, bevor gezählt wird.

      Warum das zählt: Der Zähler ist die einzige Zahl, mit der sich belegen
      lässt, dass ein Hersteller die App abräumt (siehe Michaelas Xiaomi).
      Zählt er zu großzügig, hält man ein Android-Problem für dringender als
      es ist.

- [x] ~~Kommt nach einem App-Update wirklich eine hörbare Aufforderung?~~
      **Am 21.09.2026 belegt.** Nach `installDebug` stand die
      Benachrichtigung mit `channel=voice_control_boot`, **`importance=4`**
      (also mit Ton und Einblendung) und `category=reminder` im System. Die
      Kernkorrektur aus 0.6.11 greift damit nachweislich – bisher war nur
      der Kanal belegt, nicht die Zustellung im Update-Fall.

      Dabei bestätigt, was für die Testpersonen praktisch wichtig ist: **Nach
      jedem Update ist die Sprachsteuerung aus** und muss über die
      Benachrichtigung neu eingeschaltet werden. Das ist kein Fehler,
      sondern Androids Hintergrundstart-Sperre – aber es gehört in die
      Ankündigung künftiger Updates.

- [ ] **Der Stop-Intent von außen greift nicht.** `am start-foreground-service
      -a org.dialos.mobil.action.STOP` beendete den Dienst am 21.09.2026
      nicht; es blieb nur `force-stop`. Für den Alltag belanglos (der Knopf
      in der App und die Benachrichtigung funktionieren), beim Testen am
      Gerät aber lästig, weil `force-stop` den Unterbrechungszähler
      verfälscht. Nachsehen, ob `onStartCommand` die Aktion beim Start aus
      dem Hintergrund überhaupt erreicht.

### Noch mit der Stimme zu prüfen

- [ ] **Kartenwahl im Gespräch** – die Erkennung der Karten ist auf dem
      Gerät bestätigt („1&1“ und „YELLLOW“), die gesprochene Abfrage selbst
      noch nicht. Prüfen, ob „Eins“ und der Anbietername beide greifen und
      der Anruf über die richtige Karte geht.
- [x] ~~**Aktivierungswort messen.**~~ **Erledigt am 21.09.2026** (Motorola
      edge 50 neo): fünf von sechs Rufen erkannt, der verpasste kam als
      „sprachstörungen starten“ mit 0,78 durch – knapp unter der geratenen
      Schwelle 0,82. Aus einer Viertelstunde Raumgeräusch kein einziger
      Fehlalarm, höchster Wert 0,26. Schwelle deshalb auf 0,70 gesenkt
      (`CommandParser.WAKE_MIN_RATIO`), die echten Sätze liegen als
      Regressionsfälle in `CommandParserTest`. Erschien in 0.6.13.

### Auf echter Hardware prüfen

- [x] ~~Erkennungsrate des Aktivierungsworts messen.~~ **Erledigt am
      21.09.2026**, siehe oben. Offen bleibt die Gegenprobe über einen
      längeren Zeitraum: Eine Viertelstunde Raumgeräusch ist eine Stichprobe,
      kein Alltag. Wenn aus dem Test Fehlalarme gemeldet werden, gehört die
      Schwelle noch einmal auf den Prüfstand.
- [ ] Prüfen, ob die Erkennung während der eigenen Sprachausgabe wirklich
      still ist (Echo-Problem) – besonders über Lautsprecher.
- [ ] Akkuverbrauch über einen ganzen Tag messen.
- [ ] Verhalten mit Bluetooth-Headset testen (dasselbe AIRHUG 01 wie bei
      DialOS?) – nimmt Vosk dann das Headset-Mikrofon?
- [x] ~~Autostart nach Neustart auf Android 14/15 prüfen.~~ **Geklärt am
      20.09.2026, und es war schlimmer als erwartet:** Der Dienst startet
      gar nicht (`ForegroundServiceStartNotAllowedException`) oder startet
      ohne Mikrofonzugriff. Die Ausweich-Benachrichtigung lief über den
      leisen Kanal und war damit unauffindbar. Behoben in 0.6.11.
- [ ] **Den Flugmodus-Weg am Gerät durchspielen** (0.6.12, ungetestet):
      Flugmodus an, Anruf per Sprache versuchen. Kommt die Ansage sofort?
      Erscheint die Benachrichtigung? Führt ein Tippen wirklich in die
      Flugmodus-Einstellungen? Braucht eine Stimme, ging beim Bauen nicht.
- [ ] **Die Lautstärke-Anhebung im Dienst isoliert prüfen.** Belegt ist nur
      der gemeinsame Weg (Startseite + Dienst). Ob der Dienst allein sie
      anhebt – also beim Aktivierungswort ohne Bildschirm –, ist ungeprüft.
- [ ] Überlegen, was bei aktivem „Bitte nicht stören" geschehen soll. Dann
      verweigert Android die Lautstärkeänderung, und ein Hinweis darüber
      wäre genauso unhörbar wie die Ansage, um die es geht. Vibration?
- [ ] **Am Gerät hören, ob die neue Benachrichtigung wirklich auffällt.**
      Kanal und Zustellung sind belegt, der Ton noch nicht – beim Test lief
      ein Videocall, deshalb ohne Audio geprüft.
- [ ] **Erkennen, wenn die App im laufenden Betrieb das Mikrofon verliert.**
      Der jetzige Schutz greift beim Start. Belegt ist aber auch der Fall,
      dass ein Dienst läuft und taub ist (Warnung „started from background
      can not have microphone access"). Denkbar: Wenn über längere Zeit kein
      einziges Erkennungsergebnis eintrifft, nachfragen statt schweigen.

### Funktionen

- [ ] Anruf annehmen per Sprache („Abheben“ / „Annehmen“) – aktuell kann
      die App nur anrufen, nicht abnehmen. Braucht `ANSWER_PHONE_CALLS`.
- [ ] Auflegen per Sprache.
- [ ] Lautsprecher automatisch einschalten (`EXTRA_START_CALL_WITH_SPEAKERPHONE`),
      damit ein blinder Nutzer das Telefon nicht ans Ohr halten muss –
      als Einstellung.
- [ ] Kurzwahl / Favoriten („Ruf meine Tochter an“) mit eigenen
      Bezeichnungen, die auf einen Kontakt zeigen.
- [ ] **Anrufweg wählbar machen: WhatsApp, Signal, Telegram statt Mobilfunk.**
      Gewünscht aus dem geschlossenen Test (10.09.2026): Im Büro schlechter
      Mobilfunkempfang, gutes WLAN – dort wird ohnehin über WhatsApp
      telefoniert. Denkbar als Voreinstellung oder als Rückfrage vor jedem
      Anruf.
      **Was dafür zu klären ist:** Diese Apps bieten Anrufe über einen
      Kontakt-Eintrag an (eigener MIME-Typ in `ContactsContract.Data`), nicht
      über eine offene Schnittstelle. Es funktioniert also nur für Kontakte,
      die dort auch verknüpft sind – und erfordert eine `<queries>`-Angabe im
      Manifest, um die Apps überhaupt zu sehen. **Nicht verwechseln mit dem
      SMS-Befund von 0.6.0:** Dort ging es ums Nachrichtenversenden, das
      tatsächlich keine Schnittstelle hat. Anrufe sind eine andere Frage und
      ungeprüft.
      Das Versprechen "keine Internetberechtigung" bleibt unberührt – den
      Anruf führt die andere App aus, nicht DialOS Mobil.
      Samuel selbst sieht es als Wunsch für eine spätere Version.
- [ ] Piep-Ton vor dem Zuhören (wie `dialos-start-ansage.py` bei DialOS –
      dort war ein fehlendes Startsignal ein echter Bug).

### Aus dem geschlossenen Test (ab 2026-09-05)

- [x] ~~Widget auf einem Gerät prüfen.~~ **Erledigt am 09.09.2026** auf dem
      Motorola edge 50 neo: volle Breite, Farbe und Text wechseln
      zuverlässig, ein Tippen startet den Dialog (nicht nur die App),
      TalkBack liest die richtige Beschreibung. Dabei fiel auf, dass der
      Text „Jetzt sprechen" sachlich falsch war – behoben in 0.6.7.
- [ ] Widget auf einem **schmalen** Gerät prüfen. Getestet ist bisher nur
      ein 1200 px breiter Bildschirm.
- [x] ~~Das Anbieten des Widgets am Gerät prüfen.~~ **Erledigt am
      09.09.2026** auf dem Motorola: Knopf erscheint nur ohne vorhandenes
      Widget, der Launcher zeigt den Bestätigungsdialog mit Vorschau,
      „Hinzufügen“ legt den Balken hin, danach ist der Knopf weg.
- [ ] Dasselbe **auf einem Xiaomi** prüfen. Dort stand
      „Startbildschirmverknüpfungen“ auf rot – möglicherweise scheitert die
      Anfrage genau daran, und dann greift der Rückfallhinweis.
- [x] ~~Unterbrechungserkennung am Gerät gegenprüfen.~~ **Erledigt am
      09.09.2026:** Mit `adb shell am force-stop` ausgelöst, der Startgrund
      wurde korrekt als `AFTER_INTERRUPTION` erkannt und gezählt. Dabei fiel
      auf, dass auch App-Updates mitgezählt wurden – behoben in 0.6.7, weil
      der Zähler sonst als Diagnosewerkzeug wertlos wäre.
- [ ] Die **Ansage** nach einer Unterbrechung noch hören. Bisher ist nur
      belegt, dass der Fall erkannt und gezählt wird; dass die App es auch
      ausspricht, ist ungeprüft (beim Test lief die Erkennung noch nicht,
      als der Dienst neu startete).
- [x] ~~Klären, ob Michaelas Beobachtung der Fehlansage entspricht oder einem
      echten Abschuss.~~ **Geklärt am 09.09.2026: echter Abschuss.** Die App
      sagt nichts und wird einfach still, die Benachrichtigung verschwindet.
      Gerät ist ein **Xiaomi Redmi 13C**, die Akku-Optimierung war bereits
      ausgenommen – es ist also die MIUI-eigene Prozessverwaltung, nicht der
      Android-Standardmechanismus.
- [x] ~~Xiaomi/MIUI-Hinweis aufnehmen.~~ Steht seit 09.09.2026 in
      [docs/xiaomi-einstellungen.md](xiaomi-einstellungen.md), **abgelesen
      von einem echten Gerät**, nicht geraten. Die entscheidende Einstellung
      heißt „Hintergrund-Autostart“ und ist ab Werk für fast alle Apps aus.
- [ ] **Den Xiaomi-Hinweis in die App holen.** Die Datei hilft nur, wer sie
      liest – die Zielgruppe liest kein GitHub. Kurzfassung in „Infos &
      Einstellungen“, sinnvollerweise nur auf Xiaomi-Geräten eingeblendet
      (`Build.MANUFACTURER`), damit sie andere nicht verwirrt.
- [ ] **Prüfen, ob das Widget auf Xiaomi überhaupt platziert werden kann.**
      Auf dem geprüften Gerät steht „Startbildschirmverknüpfungen“ auf rot.
      Ob das auch Widgets betrifft, ist unklar – am Gerät nachsehen.
- [ ] Andere Hersteller ergänzen (Samsung, Huawei, Oppo, OnePlus), sobald
      jemand mit so einem Gerät seine Einstellungen zeigt. Nach demselben
      Verfahren: abgelesen, nicht geraten.
- [ ] Prüfen, ob die App die herstellereigene Einschränkung selbst erkennen
      kann, statt nur allgemein zu warnen. Wenn der Zähler in „Infos &
      Einstellungen“ hochläuft, obwohl die Akku-Optimierung ausgenommen ist,
      liegt genau dieser Fall vor – daraus ließe sich ein gezielter Hinweis
      ableiten.

- [ ] **Nachfragen, ob eine Nadine im Adressbuch steht.** Ein Tester meldete,
      „Nadine anrufen“ habe Martins vorgeschlagen. Der Code erklärt das
      nicht: Nadine bekommt 0,97, Martin 0,32, und die Klangcodes sind
      verschieden (626 gegen 6726). Vermutlich hat schon Vosk etwas anderes
      verstanden. Ohne Protokoll ist es Raten.
- [ ] **Erkennung von Vornamen mit Schweizer Akzent.** Derselbe Tester
      spricht Schweizerdeutsch und hatte auch auf Hochdeutsch
      Erkennungsprobleme bei Vornamen. Prüfen, ob das ein Akzent- oder ein
      Modellproblem ist.
- [ ] **Schweizerdeutsch klar in die Store-Beschreibung schreiben.** Wird
      nicht verstanden, das Vosk-Modell ist auf Hochdeutsch trainiert. Lieber
      vorher sagen als hinterher enttäuschen.
- [ ] Prüfen, ob die vier Tempostufen die richtigen sind – 1,6 könnte für
      geübte Sprachausgabe-Nutzer immer noch zu langsam sein.

### Technik

- [ ] **Native Bibliotheken auf 16-KB-Speicherseiten ausrichten.** Die Play
      Console meldet das unter „Für deinen nächsten Release" als *Erfordert
      Aktion*: Auf Geräten mit 16-KB-Arbeitsspeicher-Seitengröße kann die App
      abstürzen oder sich gar nicht erst installieren. Für die Zielgruppe wäre
      ein stiller Absturz beim Start das schlimmste Fehlerbild.

      **Am 21.09.2026 gemessen statt geraten** (`readelf -lW` auf die
      `.so`-Dateien im Bundle, Architektur arm64-v8a):

      - `libjnidispatch.so` aus jna 5.13.0: `LOAD align 0x10000` = 64 KB.
        **Schon in Ordnung.** Das bisher hier vermutete JNA-Problem gibt es
        nicht, ein jna-Upgrade ist dafür nicht nötig.
      - `libvosk.so` aus vosk-android 0.3.47: `LOAD align 0x1000` = 4 KB.
        **Der alleinige Übeltäter.**
      - Zum Vergleich heruntergeladen und nachgemessen: vosk-android 0.3.75
        liefert `0x4000` = 16 KB. **Der Fix ist eine Zeile in
        `app/build.gradle.kts`.**

      Ebenfalls am 21.09.2026 richtiggestellt: Hier stand, das müsse „bewusst
      erst nach den 14 Tagen" geschehen, weil ein Release mitten im Test die
      Zählung störe. Das stimmt nicht. Google zählt, wie lange genug
      angemeldete Tester die App installiert haben, nicht wie lange eine
      bestimmte Version liegt. Der wirkliche Grund zu warten ist ein anderer,
      siehe nächster Punkt.
- [ ] `vosk-android` von 0.3.47 auf 0.3.75 heben. Behebt das 16-KB-Thema
      (siehe oben), ist aber **kein Einzeiler zum Nebenbei-Mitnehmen**: Es
      sind 28 Versionen der Spracherkennung selbst. Zwei Dinge gehören
      danach am Gerät geprüft, nicht angenommen:

      1. Ob das mitgelieferte deutsche Modell unverändert passt.
      2. Ob sich die Erkennungsqualität verschiebt. Die am 21.09.2026
         gemessenen Schwellwerte des Aktivierungsworts
         (`CommandParser.WAKE_MIN_RATIO`, Regressionsfälle in
         `CommandParserTest`) stammen aus Ausgaben von **0.3.47**. Mit einer
         neuen Erkennungs-Bibliothek ist diese Messung hinfällig und muss
         wiederholt werden.

      Dringend wird es, wenn Produktionszugriff beantragt wird; für den
      geschlossenen Test bleibt es eine Warnung.
- [ ] Prüfen, ob ein grammatikbeschränkter Erkenner im Wartezustand Akku
      spart, ohne beim Umschalten Sprache zu verlieren (siehe Begründung
      im README).
- [ ] Release-Build signieren und den Signaturschlüssel sichern.
- [ ] Entscheiden, ob die App in den Play Store soll. Falls ja:
      `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` und `CALL_PHONE` brauchen eine
      Begründung, und der Play Store verlangt für `CALL_PHONE` eine
      Kernfunktions-Erklärung.
- [ ] Verhalten prüfen, wenn während des Dialogs ein Anruf hereinkommt.

### Zusammenspiel mit DialOS

- [ ] Klären, ob Handy und Laptop dieselben Kontakte teilen sollen und wie
      (Nextcloud-Kontakte über CardDAV?).
- [ ] Einheitliche Befehlssprache zwischen DialOS-Desktop und DialOS Mobil –
      der Desktop nutzt hassil, hier ist die Erkennung handgeschrieben.
      Perspektivisch sollten beide dieselben Sätze verstehen.
- [ ] Im DialOS-Repo (README + `docs/`) auf DialOS Mobil verweisen.

## ✅ Erledigt

- [x] **Rufnummern diktieren ist brauchbar geworden** (0.6.4) – 2026-09-06.
      Die App las nach jedem Ziffernblock die ganze bisherige Nummer vor und
      war dabei taub; wer weiterdiktierte, verlor genau diese Ziffern. Dazu:
      45 statt 15 Sekunden Wartezeit beim Diktieren, keine kommentarlos
      verworfene Nummer mehr bei Zeitablauf, eine Anleitung an der Stelle,
      wo sie gebraucht wird, und „letzte Ziffer löschen“ statt alles oder
      nichts. Einzelheiten im [Änderungsprotokoll](README.md#änderungsprotokoll).

- [x] **Fünf Befunde aus dem geschlossenen Test behoben** (0.6.3) –
      2026-09-05. „Ja bitte“ und „nein danke“ werden verstanden, gleich
      klingende Namen verdrängen den gemeinten nicht mehr, Nummerntypen
      („privat“, „mobil“, „Arbeit“) funktionieren im Befehl und als Antwort,
      die Sackgasse nach „Das habe ich nicht verstanden“ ist weg, und die
      Ansage nach der Wartezeit behauptet nicht mehr, die App höre auf.
      Einzelheiten im [Änderungsprotokoll](README.md#änderungsprotokoll).

- [x] Sprechgeschwindigkeit und Stimme der Sprachausgabe einstellbar –
      2026-09-05. Vier Tempostufen und die deutschen Stimmen des Geräts,
      über Knöpfe zum Weiterschalten statt Schieberegler, mit Hörprobe nach
      jedem Tippen.

- [x] Lizenz festgelegt: Apache 2.0, mit NOTICE-Datei für Vosk, das
      Sprachmodell, JNA und AndroidX – 2026-08-19

- [x] **Erster vollständiger Anruf per Sprache auf echter Hardware**
      (Motorola edge 50 neo, Android 16) – 2026-08-19. Aktivierungswort,
      Namenserkennung („carola stören" → Carola Stern), Bestätigung,
      Kartenwahl und Gesprächsaufbau. Zwei Fehler dafür behoben, siehe
      Änderungsprotokoll 0.6.1.
- [x] Kartenwahl per Sprache bei zwei SIM/eSIM, Version und
      GitHub-Link in den Einstellungen, größeres Logo – 2026-08-19
- [x] **Kurznachrichten per Sprache** (SMS) – 2026-08-19. WhatsApp bewusst
      verworfen: keine Sende-Schnittstelle, auf dem Gerät nachgeprüft.
- [x] Kontrast-Umschalter (schwarz/gelb) und Lautstärke-Umschalter
      (50 %/100 %) in der Kopfzeile, auf dem Gerät geprüft – 2026-08-19
- [x] Schaltfläche „Jetzt sprechen“ entfernt (redundant zu Hauptschalter
      und Kachel) – 2026-08-19
- [x] **Erster Lauf auf echter Hardware** (Motorola edge 50 neo, Android 16)
      – 2026-08-19. Stephan hat den Ablauf durchgesprochen, er hat
      funktioniert. Belegt im Log: Vosk-Modell entpackt (91 MB im externen
      App-Ordner), `Background started FGS: Allowed` für den
      Mikrofon-Dienst, acht Zustandswechsel des Dialogs, sauberes
      Ausschalten.
- [x] Material-You-Farben (`DynamicColors`) wieder entfernt – sie hatten
      das DialOS-Blau durch vom Hintergrundbild abgeleitete Olivtöne
      ersetzt und den Kontrast dem Zufall überlassen – 2026-08-19
- [x] Projekt aufgesetzt (Kotlin, Gradle 8.14.3, AGP 8.13, minSdk 26,
      targetSdk 36) – 2026-08-19
- [x] Vosk offline eingebunden, deutsches Modell wird beim Build geladen
      und liegt nicht im Repo – 2026-08-19
- [x] Aktivierungswort „Sprachsteuerung starten“ – 2026-08-19
- [x] Kontaktsuche mit Kölner Phonetik, Levenshtein und wortweisem
      Vergleich, inklusive Rückfrage bei mehreren Treffern – 2026-08-19
- [x] Deutsche Zahlwörter → Ziffern, Nummer diktieren – 2026-08-19
- [x] Bestätigung vor dem Wählen, abschaltbar – 2026-08-19
- [x] Wählen über `TelecomManager.placeCall` statt `ACTION_CALL` – 2026-08-19
- [x] Schnelleinstellungs-Kachel, Startsymbol-Kurzbefehl, Assistenten-Aufruf
      als zusätzliche Startwege – 2026-08-19
- [x] Barrierefreie Oberfläche (große Schaltflächen, Live-Region) – 2026-08-19
- [x] Logo und Farben aus DialOS übernommen – 2026-08-19
- [x] 22 Unit-Tests für Namensvergleich, Zahlwörter und Befehlserkennung,
      alle grün – 2026-08-19
- [x] Debug-APK gebaut (63 MB), Lint ohne Fehler – 2026-08-19
