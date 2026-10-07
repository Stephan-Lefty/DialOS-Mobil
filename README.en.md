[Deutsch](README.md) | [English](README.en.md) | [Changelog](#changelog) | [TODO](TODO.en.md)

<img src="assets/logo.png" alt="DialOS logo" width="200">

# DialOS Mobile

The phone companion to [DialOS](https://github.com/Stephan-Lefty/DialOS):
an Android app that lets you **place calls using nothing but your voice**.
Built for blind people and people with severe motor impairments who cannot
operate a phone by touch – but who want to call and be called.

Recognition runs **entirely offline** on the device, using the same engine
as the DialOS desktop (Vosk). No audio ever leaves the phone, and the app
works without an internet connection and without Google services.

The spoken interface is German, because the offline model is a German one.

This project was created together with [Claude](https://claude.com).

## Screenshots

Click to enlarge.

<table>
<tr>
<td align="center" width="25%">
<a href="screenshots/01_startseite_aus.png"><img src="screenshots/01_startseite_aus.png" width="170" alt="Start screen showing the status „Ausgeschaltet“ (off) and a large blue button „Sprachsteuerung einschalten“ (turn voice control on)"></a><br>
<sub>Off</sub>
</td>
<td align="center" width="25%">
<a href="screenshots/02_startseite_hoert_zu.png"><img src="screenshots/02_startseite_hoert_zu.png" width="170" alt="Start screen mid-dialogue, the status reads „Sprachsteuerung bereit. Wen möchten Sie anrufen?“ (voice control ready, who would you like to call?)"></a><br>
<sub>Mid-dialogue</sub>
</td>
<td align="center" width="25%">
<a href="screenshots/03_infos_einstellungen.png"><img src="screenshots/03_infos_einstellungen.png" width="170" alt="The „Infos &amp; Einstellungen“ page with a back button, permissions, continuous operation and three switches"></a><br>
<sub>Settings</sub>
</td>
<td align="center" width="25%">
<a href="screenshots/04_kontrastansicht.png"><img src="screenshots/04_kontrastansicht.png" width="170" alt="The same start screen in the high contrast variant: black background, yellow buttons"></a><br>
<sub>High contrast</sub>
</td>
</tr>
</table>

## The home screen widget

A bar spanning the full screen width – for this audience an app icon among
twenty others is not a usable target. One tap turns it on and immediately
asks „Wen möchten Sie anrufen?“ (who would you like to call?).

<table>
<tr>
<td align="center" width="50%">
<a href="screenshots/widget/05_widget_aus.png"><img src="screenshots/widget/05_widget_aus.png" width="200" alt="Home screen with a wide blue bar reading „Antippen zum Einschalten“ (tap to turn on), below it „Ausgeschaltet“ (off)"></a><br>
<sub>Off</sub>
</td>
<td align="center" width="50%">
<a href="screenshots/widget/06_widget_an.png"><img src="screenshots/widget/06_widget_an.png" width="200" alt="The same bar in green reading „Antippen und sprechen“ (tap and speak), below it „Sprachsteuerung bereit. Wen möchten Sie anrufen?“"></a><br>
<sub>On</sub>
</td>
</tr>
</table>

The state changes through **colour and text** – colour alone is not enough
if someone cannot distinguish colours well. Blind users hear a separate,
fuller description through TalkBack.

## What a call sounds like

```
User: „Sprachsteuerung starten“          (start voice control)
App:  „Sprachsteuerung bereit. Wen möchten Sie anrufen?“
User: „Max Mustermann anrufen“
App:  „Soll ich Max Mustermann auf Mobil anrufen?
       Sagen Sie Ja oder Nein.“
User: „Ja“
App:  „Ich rufe Max Mustermann an.“      → the call starts
```

Dictating a number instead of naming a contact:

```
User: „Nummer wählen“
App:  „Bitte sprechen Sie die Nummer, Ziffer für Ziffer.
       Sagen Sie fertig, wenn Sie durch sind.“
User: „null eins sieben neun …“
App:  „0 1 7 9 …“                        (reads back after every group)
User: „fertig“
App:  „Soll ich die Nummer 0 1 7 9 … anrufen?
       Sagen Sie Ja oder Nein.“
User: „Ja“
```

Available at any point: **„Abbrechen“** (cancel), **„Hilfe“** (help),
**„Wiederholen“** (repeat), **„Sprachsteuerung beenden“** (shut down).
After 15 seconds of silence the app ends the dialogue by itself and goes
back to waiting for the wake phrase.

## Features

- **Wake phrase „Sprachsteuerung starten“** – the app listens continuously
  in the background, no need to wake the screen.
- **Contact matching that forgives mishearing.** Three methods combined:
  token comparison (a surname on its own is enough), Levenshtein similarity
  (`Musterman` → `Mustermann`) and the **Cologne phonetic algorithm**, so
  `Meier`, `Maier`, `Mayer` and `Meyer` all resolve to the same contact.
- **Disambiguation:** with several plausible matches the app reads them out
  numbered and the user says „eins“, „zwei“ – or repeats the name.
- **Several numbers per contact:** mobile first, then home, then work.
  Saying „Nein“ moves to the next number instead of cancelling.
- **Dictating digits** in German, including compound numerals
  („einundzwanzig“ → `21`), `plus` for the country code and
  „doppel sieben“ for `77`.
- **Confirmation before dialling** (can be switched off) – a misrecognition
  should never turn into a wrong call.
- **Four ways to start the dialogue:** wake phrase, a large button in the
  app, a quick settings tile, or the assistant gesture (the app can be set
  as the default digital assistant).
- **Restarts automatically** after the phone reboots.
- **Accessible UI:** very large buttons, high contrast, status shown as a
  live region so TalkBack announces every change.
- **Contrast can be switched** (button top right): black background with
  yellow buttons for people with severe visual impairment.
- **Volume can be switched** (button top left): 50 % or full. The app always
  sets 50 % on startup – a phone turned down too far otherwise only becomes
  apparent when the app seems to go silent.

## How it is built

| Component | Responsibility |
|---|---|
| [`VoiceService`](app/src/main/java/org/dialos/mobil/VoiceService.kt) | Foreground service, keeps microphone and dialogue alive, dials via `TelecomManager` |
| [`VoiceEngine`](app/src/main/java/org/dialos/mobil/VoiceEngine.kt) | Vosk binding: unpack the model, listen, pause |
| [`DialogController`](app/src/main/java/org/dialos/mobil/DialogController.kt) | The conversation as a state machine – no Android dependency at its core |
| [`CommandParser`](app/src/main/java/org/dialos/mobil/CommandParser.kt) | Recognises the wake phrase and commands in the transcript |
| [`NameMatcher`](app/src/main/java/org/dialos/mobil/NameMatcher.kt) | Name comparison including Cologne phonetics |
| [`GermanNumbers`](app/src/main/java/org/dialos/mobil/GermanNumbers.kt) | German numerals → digits |
| [`ContactRepository`](app/src/main/java/org/dialos/mobil/ContactRepository.kt) | Reads and searches the address book |

Two design decisions that are not obvious:

- **Free vocabulary rather than a grammar for the wake phrase.** A grammar
  restricted to the wake phrase would use less power, but switching to
  command mode means releasing the microphone for a moment – exactly when
  the user is still speaking.
- **Dialling through `TelecomManager.placeCall` instead of `ACTION_CALL`.**
  Since Android 10 a background service may no longer start an activity;
  the telecom service accepts the call reliably.

While the app is speaking, recognition is paused – otherwise it hears its
own voice. During a call it pauses as well and watches the call: if none
materialises within twelve seconds, it says so out loud instead of
silently falling back.

## Building

Requirements: Android SDK (platform 36, build tools 36) and a JDK 17.

```bash
./gradlew assembleDebug
```

The German Vosk model (~46 MB) is deliberately **not** in the repository.
Gradle downloads it from [alphacephei.com](https://alphacephei.com/vosk/models)
on the first build and unpacks it into the assets. A different model can be
supplied:

```bash
./gradlew assembleDebug -PvoskModelZip=/path/to/model.zip
```

Tests (name matching, numerals, command parsing – no device needed):

```bash
./gradlew test
```

The resulting APK is at `app/build/outputs/apk/debug/app-debug.apk`, around
63 MB – mostly the speech model.

## Setting it up on the phone

1. Install the APK (allow installation from unknown sources).
2. Open the app and tap **„Berechtigungen erteilen“**: microphone,
   contacts, phone calls and notifications.
3. Tap **„Akku-Optimierung ausnehmen“** – otherwise the service is put to
   sleep after a while and stops hearing the wake phrase.
4. Tap **„Sprachsteuerung einschalten“**. The app confirms out loud.

**On Xiaomi devices step 3 is not enough.** MIUI terminates background
services independently of battery optimisation; what matters there is the
**„Hintergrund-Autostart“** (background autostart) list, which is set to
"not allowed" for almost every app out of the box. Details in
[docs/xiaomi-einstellungen.md](docs/xiaomi-einstellungen.md) – read off an
actual device, not guessed.

If you are taking part in the closed test,
[docs/pruefliste-test.en.md](docs/pruefliste-test.en.md) holds a
read-aloud-friendly list of what we are hoping to learn.

Optional but worthwhile for the target audience:

- Under *Apps → Default apps → Digital assistant*, select **DialOS Mobil**.
  The assistant gesture then starts the dialogue directly.
- Add the **„Sprachsteuerung“** tile to the quick settings.

## Known limits

- **Autostart on Android 14 and newer:** a service with microphone access
  may not be started from the background. After a reboot the app therefore
  posts a tappable notification instead of listening straight away.
- **Continuous listening costs battery.** With battery optimisation
  disabled, expect a noticeable increase; the small Vosk model was chosen
  deliberately to limit this.
- **The small German model** (`vosk-model-small-de-0.15`) is built for
  commands, not dictation. Unusual proper names are recognised less well –
  the name matcher compensates for a good part of that.
- **Speaking emphatically loudly breaks recognition.** Measured on
  2026-10-07, the same nine-digit number spoken seven times: 3 out of 3
  correct at normal volume, 1 out of 4 when spoken emphatically loudly.
  Distance, by contrast, is uncritical – recognition was flawless even
  from a metre away.

  This matters because it contradicts the obvious: someone who is not
  understood speaks louder and holds the device closer to their face –
  and thereby does precisely the wrong thing. In the loud case “eins
  sieben” fell apart into “alles selber” or “einsehen sehen”; those are no
  longer mishearings that could be added to a lookup table. The in-app
  guidance now says so explicitly; figures in `NummerMitEinleitungTest`.
- **This app is not an emergency call feature.** An emergency call should
  never depend on speech recognition.

## Credits

- [Vosk](https://alphacephei.com/vosk/) by Alpha Cephei – offline
  recognition and the German model (Apache License 2.0).
- Logo and colours from the [DialOS](https://github.com/Stephan-Lefty/DialOS) project.

## Licence

[Apache License 2.0](LICENSE) – Copyright 2026 Stephan Rösner.

The same licence as Vosk, the German speech model and the Android
libraries in use; chosen deliberately so that a single licence covers the
whole stack and the included patent grant applies. Anyone redistributing
the app must ship the [NOTICE](NOTICE) file – it names the authors of the
bundled components.

## Changelog

### 0.6.17 (2026-10-07)

**Speaking louder makes it worse — measured, and it is the most common
operating mistake.**

A tester wrote: “She doesn’t understand me, even though I hold the device
in my hand and speak loudly and clearly.” That read like an apology for it
still not working. It was the cause.

The same nine-digit number spoken seven times, Motorola edge 50 neo:

| How it was spoken | Recognised correctly |
|---|---|
| normal, 30 cm | ✓ |
| normal, 1 metre | ✓ |
| emphatically loud, 5 cm | ✓ |
| **emphatically loud, 30 cm (4×)** | **1 out of 4** |

Distance barely matters — from a metre away it was flawless. Volume does:
of four emphatically loud attempts, one was correct. The app now says so
explicitly in both the spoken help and the help text, because nobody
arrives on their own at the idea of speaking *more quietly* when they are
not being understood.

Why this cannot be fixed in the mishearing table: in the loud case “eins
sieben” fell apart into **“alles selber”** and **“einsehen sehen”**. Until
today the mishearings always sat close to the number word (“nun” for
“null”, “sieden” for “sieben”) and could simply be added. Treating “sehen”
or “alles” as a digit, by contrast, would be indefensible — and the stem of
“sieben” is “sie”, a pronoun. The limit of that method has been reached;
what remains is guidance and a level warning.

**Three faults from the same run that could be fixed:**

“sieden” was missing as a mishearing of “sieben”. The seven therefore fell
away *without replacement*, turning nine digits into eight — with nothing
to indicate it. That is the most dangerous kind of fault here, because what
remains is a plausible-sounding number the user confirms. Also added:
“viele” and “neue”. The third occurrence of the same pattern — an inflected
form missing while its neighbours have long been in the table.

“nein” was understood as “ein” twice, and the app then repeated the very
same question. On screen that is a shrug; for someone who cannot see, it is
an app that has stopped responding to anything. In the confirmation step
“ein” now counts as no — and only there, because while dictating it has to
remain the digit one.

**Also confirmed:** the international prefix spoken as “null null” now
comes through in full (`0 0 4 9 1 7 6 8 0`). Before 0.6.16 the app
swallowed both zeros and produced a different, plausible-sounding number.

**The APK is a good 3 MB smaller: 54.1 instead of 57.4 MB.**

No change to the app itself, only to its size. Until now the package also
carried the native Vosk and JNA libraries for x86_64 — an architecture no
phone has, only the Android emulator.

About the two figures, because they differ: the two omitted libraries weigh
**9.8 MB uncompressed**, but inside the package they are stored compressed
and cost **3.3 MB** there. The first draft of this entry quoted the 9.8 MB as
the saving — that was wrong; both were then measured.

The reason for doing it now is the planned direct download on dialos.org:
over the Play Store the weight never showed, because Google cuts tailored
packages from the AAB. Whoever downloads the APK from the website pulls the
whole file. For an app that blind people install over what may be a narrow
connection, that counts.

The restriction lives in `defaultConfig`, the addition for the emulator in
the `debug` block — not the other way round. Gradle **unions** a build type's
`abiFilters` with those of `defaultConfig` instead of replacing them; in the
other order the restriction would have had no effect at all.

### 0.6.16 (2026-10-01)

**The app was listening to itself – and crashed after every update.**

A day with two defects no test suite would ever have found, because both
only surface on a real device.

**It woke itself with its own announcements.** eine Testperson had
reported it three times, the last time with the decisive wording: "It says
the device switched the app off and it is working again, then it asks who I
want to call? No activation from me." The first time we had put it down to
misrecognition. That was wrong.

`VoiceService.onEngineReady()` arms the microphone and starts speaking one
line later – without pausing it. The pause logic lived solely in
`DialogController.say()`; the service's own two announcements bypassed it.
And both contained the wake phrase, the start announcement even verbatim
("Sagen Sie: Sprachsteuerung starten"). Vosk heard the app's own speaker
and activated.

Measuring revealed that the microphone pause alone would not have been
enough. The interruption announcement contains no "starten" at all and
still woke the app: the word pair "sprachsteuerung **wurde**" scores
**0.783** against "sprachsteuerung **starten**", and the threshold sits at
0.70. Raising the threshold was no way out – the only missed real call from
the 2026-09-21 measurement scored 0.78, i.e. the same. So the sentence had
to go.

Verified on the device, from both sides: the new build stays silent on
restart, while the 0.6.14 build running in parallel – precisely the one
eine Testperson has – heard its own announcement, processed it as a name and replied
with exactly the sentence she had reported.

The interruption announcement has been **dropped entirely**. It announced
the end of the outage, not the outage itself; at that point everything is
fine and there is nothing to do. The case it was built for – the service is
gone and does not come back – could never be announced anyway, because a
killed app cannot speak. And its advice ("exempt from battery
optimisation") pointed at a settings button this audience finds hard to
navigate to. The rule behind it: **the app speaks only when spoken to.**
That also removes the unasked volume increase – whatever stays silent need
not be audible.

**No more crash after an app update.** Spotted incidentally while
verifying: `BootReceiver` kicks off the service via
`startForegroundService()` on `MY_PACKAGE_REPLACED`; the service detected
the background start, posted the notification and called `stopSelf()` – but
never `startForeground()`. Android holds it to that contract and killed the
process with a `ForegroundServiceDidNotStartInTimeException`. **This hit
every tester on every Play update**, not just the developer.

**A phone number may now sit in the same sentence as the command:**
"Nummer wählen null eins sieben acht vier sechs". This came out of testing,
and measurement confirmed it – seven attempts without the announcement
failed, two with it came through flawlessly:

| Variant | What Vosk heard | Digits |
|---|---|---|
| without announcement | `null eines sie wenn acht vier sechs` | `0846` |
| with announcement | `nummer wählen null eins sieben acht vier sechs` | `017846` |

The likely explanation is the run-up letting the recogniser settle; until
then the first digit regularly fell apart ("null" became "nun"). The app
used to recognise the command, throw away the digits in that same sentence
and ask again. Now it takes them, as soon as at least six add up.

**Four gaps in the misheard-numeral table closed.** "nun" was missing – of
all words the most frequently measured mishearing of "null". "vielen" and
"neuen" were missing although "viel" and "neu" were there; that turned
`004917680` into `0017680`, a different and entirely plausible-sounding
number. Such silent omissions are the most dangerous failure mode here. On
top of that, two misheard numerals side by side could not be rescued – the
very case of "null null" opening an international number. Safety now
spreads outwards from genuine numerals, though it still needs a real
anchor.

**The leading plus is spoken out when reading back.** As a special
character it was left to the speech engine whether to voice it – someone
who cannot see the screen might hear no difference between "+49 176 …" and
"49 176 …". For international numbers "null null vier neun" is still the
safer route: a misheard "plus" vanishes without trace, whereas "null" gets
caught.

**"Sprachsteuerung stoppen" now ends the app.** "stopp" and "stop" were in
the list, "stoppen" was not – a distinction no user makes, and on the
direct opposite of the wake phrase at that. "stopp" on its own still only
cancels the current step; that separation is deliberate.

**"Neue Nummer" discards the current one** – it was in no list and ran into
nothing.

**The confirmation now says there are three ways out.** It read "Sagen Sie
Ja, Nein, oder Korrigieren für die letzte Ziffer" and sounded as if the
last digit were the only thing changeable. In fact "Nein" cancels and lets
you say a new number straight away; the prompt simply never said so.

**The test build has its own icon.** Both builds install side by side, and
that has now spoiled a measurement twice – most recently the Play build
spoke its announcement while the test build listened. The name has
distinguished them since 0.6.15, but on the home screen colour carries far
further.

81 tests, lint clean.

### 0.6.15 (2026-09-27)

**The app no longer talks over you endlessly, and "abschalten" (switch off)
actually switches it off.**

From a tester report on 27 September 2026: "The app has a mind of its own. It
speaks without being asked and then always answers that it can't find that in
the contacts. Even when I tell it to switch itself off […] it always
understands something else."

The quoted sentence was the key – it occurs exactly once in the code, and only
when the address book **has** been read. So this was not a defect but a gap in
the flow: after an unsuccessful name the app returned to the very same
question, and every recognition restarted the timeout. With a television on or
a conversation nearby this ran indefinitely, and the app commented on every
sentence in the room.

- **After three failed attempts in a row it stops** and says why: "I don't
  understand you right now. Perhaps it is too loud here. I will stop now so
  that I don't talk over you." A success resets the count, and a
  half-dictated phone number is still not thrown away – it is read back for
  confirmation.
- **"Abschalten" was missing from the commands** – the most obvious word of
  all. The app searched for it as a name and replied that it could not find it
  in the contacts. Added alongside it: "ausschalten", "abstellen", "schalte
  dich ab", "Ruhe" and the forms using "App".
- **The way out is now checked as generously as the way in.** Until now only
  the wake phrase tolerated mishearings via a similarity measure, while
  stopping demanded the exact wording – the door was easier to open from
  outside than from inside. Measured: the documented mishearing
  "sprachstörungen" scores 0.667, while the closest ordinary word
  ("sprechstunde") scores 0.533; the threshold sits between them.
- **Politeness no longer voids a command.** "Hilfe bitte" (help please),
  "bitte aufhören" (please stop) and "abbrechen bitte" (cancel please) all
  fell through and were searched as names – precisely the phrases someone uses
  when they are stuck were the least effective ones.

**Contacts with digits in their name are found – and a spoken phone number is
no longer searched for as a name.**

From the same tester, one day later: "I wanted to call MA40, that is how the
contact is stored in my phone, and the app did not find it, nor the phone
number from there that I read out to it." And the sentence that ruled out the
obvious explanation: "Both times it was quiet around me."

It wasn't that either. Measured, "ma vierzig" against the contact "MA40"
scores **0.412** against a match threshold of 0.62 – that could never have
worked, at any volume. The address book holds the digits, the speaker says the
number word, and nothing bridged the two.

- **Number words become digits** before comparison: "ma 40" scores 0.800, and
  run together as "ma40" exactly 1.000. This affects far more than this one
  case – government departments, bus routes, room numbers, "Werkstatt 2".
- **Split names are rejoined.** The speech model breaks unfamiliar names into
  familiar words; "Ludwig" arrives as "lud wig" (0.857 versus 1.000 when
  rejoined).
- Both are **additional** to the existing comparison, and the maximum wins.
  Each transformation on its own can also hurt: "ma vierzig" run together
  drops to 0.222, "Hans Peter" from 1.000 to 0.900. A contact found before is
  therefore still found.
- **A spoken phone number is recognised as one.** Previously you had to say
  "Nummer wählen" first; anyone who simply spoke the digits was told they were
  not in the contacts. The app now searches contacts first and, if none match
  and at least six digits add up, reads the number back for confirmation.

What this does **not** solve: the second reported contact, "Heli", remains
open. Nearly every plausible mishearing already matches it today (helli,
helly, eli, heil, geli – all above 0.75), so the cause lies elsewhere. Without
the actual wording, any change would be guesswork.

**The app now says how many contacts it knows.**

From a tester report: "it doesn't find my contacts". The wording already
narrowed things down – had the permission been missing, the app would have
said "Ich konnte keine Kontakte lesen". So it was reading the address book
and simply not finding the name.

That left the tester with a question she could not answer: does the app
know my address book at all, or is it just mishearing the name? A look at
the screen would not have helped either – the number was nowhere to be
seen. The app had known it all along.

- **On switch-on** it states the number: "Sprachsteuerung eingeschaltet.
  Ich kenne 247 Kontakte." Only on switch-on, not on every wake phrase –
  otherwise you would hear it before every call, and a diagnostic aid would
  turn into a nuisance.
- **With zero contacts** it becomes a hint: where the permission lives, and
  that calling via "Nummer wählen" still works.
- **Under "Infos & Einstellungen"** the same number appears in writing,
  right below the interruption counter.

People are counted, not phone numbers – someone with three numbers is one
contact. And the display distinguishes "zero contacts" from "not read yet":
one is a statement, the other merely absent knowledge, and a wrong zero
would be worse than no number at all.

### 0.6.14 (2026-09-21)

Three findings from a test on the device. The first is the most serious.

- **The switched-off setting failed silently.** The app recognised
  "Sprachsteuerung starten" twice, cleanly – it is in the log verbatim – and
  did nothing, because "Auf ‚Sprachsteuerung starten' hören" was switched off
  in the settings. No announcement, no sound. From outside this was
  indistinguishable from "doesn't hear me", and someone who cannot see the
  screen looks for the fault in their own pronunciation. That is exactly how
  the report arose that the wake phrase does not work.

  The app now says: "Ich habe Sie verstanden. Das Starten per
  Aktivierungswort ist aber ausgeschaltet" – along with the way to the
  switch. The opening words are the point: the missing information was not
  where the switch sits, but that the app had heard anything at all. It is
  said **once** per off-phase, not on every attempt; the silence should not
  turn into nagging.

The other two affect people with a well-kept address book.

- **Two numbers with the same label could not be told apart.** Anyone with
  two mobile phones has "Mobil" twice in their address book. The follow-up
  question then came through word for word twice – "Soll ich Max Mustermann
  auf Mobil anrufen?" – and answering "Nein" produced the very same question
  again. Someone who cannot see the screen had no way of telling which of
  the two was meant. When the label is ambiguous the app now adds the final
  digits: "auf Mobil, endet auf 5 6 7 8". Only then – with "Mobil" and
  "Privat" the shorter prompt stays.
- **The address book was read once, at switch-on.** Anyone who added a
  contact or corrected a number afterwards got "habe ich in den Kontakten
  nicht gefunden", with no hint that the app was working from a stale copy.
  It now listens for changes and re-reads – with a three-second delay,
  because an account sync would otherwise make it re-read the whole address
  book dozens of times over. Verified on the device: one change, dozens of
  individual notifications, **one** reload.

### 0.6.13 (2026-09-21)

**The wake phrase has been measured for the first time, not guessed.**

Since 0.6.0 this repository carried the sentence that the thresholds in
`CommandParser.isWakePhrase` were estimated and had never been checked with
a real voice. That is now done: a test run on the Motorola edge 50 neo, at
normal speaking volume, the device on the table, plus a quarter of an hour
of room noise and a conversation running alongside as a counter-check.

- **Five out of six calls were recognised.** The one that was missed came
  through as "sprachstörungen starten" – a similarity of 0.78, just under
  the guessed threshold of 0.82.
- **Not a single false alarm.** What the model made of background noise
  ("diese gelatine fuhr", "welcher bewegen erwachen er") never got above
  0.26.

There is a lot of room between 0.26 and 0.78, and it was being wasted. The
threshold now sits at **0.70**: it catches the misheard call and still keeps
almost three times the distance to the loudest background noise. The
measured sentences – calls and noise alike – are regression cases in
[`CommandParserTest`](app/src/test/java/org/dialos/mobil/CommandParserTest.kt),
so anyone touching the threshold in future notices immediately if they make
it worse.

There was a reason this test stayed meaningless for so long, and it only
came to light in 0.6.11: the app had not been listening at all for months.
A recognition problem cannot be measured while the microphone is mute.

### 0.6.12 (2026-09-20)

Two questions from the test, and both uncovered a gap.

- **The announcements were inaudible on a muted phone.** The volume was only
  raised when the start screen was opened – not when the app springs to life
  through the wake phrase. Anyone with the phone muted in their pocket who
  said "Sprachsteuerung starten" got an answer nobody could hear: precisely
  the everyday case the wake phrase was built for. The service now takes
  care of this itself, **before** it speaks for the first time. Whoever has
  turned it up keeps their setting – only what sits below is raised.
- **Airplane mode was unknown to the app.** It said "Ich rufe Max Mustermann
  an", the call failed silently, and only twelve seconds later did the call
  watchdog report that nothing had come of it – without saying why. The app
  now checks **before** dialling and says so straight away. It cannot switch
  airplane mode off; that has been reserved for system apps since Android
  4.2. What it can do is lead the way. A notification opens the airplane
  mode settings directly, and the announcement explains the route there
  instead of merely reporting that something does not work.

### 0.6.11 (2026-09-20)

**The most serious bug in this project – and the best hidden one.**

Two testers reported that the wake phrase did not work; one wrote that she
had to "activate it by hand every time". On the device the reason showed
up: the **last microphone access was fifteen days old**, while the app had
been displaying "DialOS Mobil is listening" the whole time.

From Android 12 on, foreground services started from the background face
two restrictions, and both hit this app:

- The start is **refused outright**
  (`ForegroundServiceStartNotAllowedException`). This happens after every
  reboot and after every app update.
- Or it succeeds, but **without microphone access**. The service then keeps
  running, the notification stands, and Vosk waits for audio that never
  arrives.

The first case produced a single log line, the second nothing at all. From
outside both looked identical: an app that appears to run and responds to
nothing. That is exactly the silent failure that costs this audience the
most – this time in the core function.

The app now asks **audibly** for a tap in both cases: its own notification
channel with high importance, sound and vibration instead of the previous
mute line in the shade. And a background start without microphone rights is
no longer attempted at all, rather than running on deaf.

Plus two routes that need no touch:

- **"Turn on as soon as the app opens"** (new setting). "Hey Google, öffne
  DialOS Mobil" is then enough – the app immediately asks who to call.
- The post-reboot notification leads straight into the dialogue when tapped,
  not merely into the app.

### 0.6.10 (2026-09-10)

Two findings from the closed test, and the second is the same bug as in
0.6.3 – just somewhere else.

- **Contact selection was hiding matches.** It cut off silently after the
  third suggestion. Anyone with five contacts called Hans could not reach
  two of them by voice and was never told they existed. It now offers up to
  six – the limit of what anyone can hold in mind while listening – and if
  there are more, **the app says so**: "I found eight and will read out the
  first six." Along with the way out: give both first and last name.
- **"Abbrechen" led into a dead end.** The app said "Abgebrochen." and fell
  silent. After that it listens only for the wake phrase – but it did not
  say so. Anyone who then said a name was talking to nobody; one tester
  concluded speech recognition was broken. It never was.
  Two things changed: the announcement now names the way back (and
  distinguishes whether the wake phrase is enabled at all). And
  **"Abbrechen" now ends only the current step**, not the whole
  conversation: cancelling in the middle of a contact selection asks "Wen
  möchten Sie anrufen?" instead of throwing you out. To end the conversation
  entirely, "Sprachsteuerung beenden" still works.

Both are the same pattern as twice before: the technology did not fail – the
app failed to say what it expected.

### 0.6.9 (2026-09-09)

The widget button from 0.6.8 works – but nobody could find it. That was not
the button's fault.

- **The settings page threw you out while you were reading.** It closed
  itself after **ten seconds** of inactivity; intended for someone who
  strays there by accident and cannot find their way back unaided. But the
  timer was only extended by **touches**. Anyone having the page read out by
  TalkBack touches nothing – and was thrown back mid-sentence. The settings
  were therefore unusable for precisely the users this app is built for.
  It is now 60 seconds, and **with a screen reader active the automatic
  return does not apply at all**.

Noticed because Stephan could not find the new widget button – and I needed
three attempts myself, with a cable and knowing where to look.

Verified on the device: the page is still open after 29 seconds. And the
widget button from 0.6.8 does what it should – the launcher shows its
confirmation dialogue with a preview, tapping "Add" places the bar, and
afterwards the button disappears.

### 0.6.8 (2026-09-09)

- **The app now offers to place the widget itself.** The usual route is
  practically unusable for this audience: press and hold an empty area,
  scroll a list, find the right entry, drag it and drop it in the right
  place. Anyone who cannot see, or whose hands are unsteady, fails at that –
  and would end up without the very aid built for them.
  "Info & settings" now has a button that asks the launcher to do it
  (`requestPinAppWidget`), which then shows nothing more than a confirmation
  dialogue. The button appears **only** when the bar is not yet placed and
  the launcher supports the request – otherwise it would be clutter or a
  disappointment. If the launcher cannot do it after all, a note explains
  the manual route.

### 0.6.7 (2026-09-09)

The first release verified on an actual device rather than merely built.
Both findings surfaced *because* the phone was plugged in – neither would
have shown up at the desk.

- **The widget text was wrong.** In the running state it read "Jetzt
  sprechen" (speak now) – but you cannot simply start speaking; you first
  have to say the wake phrase or tap. The line beneath it even contradicted
  it. It now shows a call to action: "Antippen zum Einschalten" (tap to turn
  on) and "Antippen und sprechen" (tap and speak).
  For blind users TalkBack reads a separate, fuller description anyway; the
  visible text is for people with residual vision. Both now say the same
  thing.
- **The interruption counter was counting app updates.** An `install -r`
  triggered an "interruption" because the service really is stopped and
  restarted. Technically correct, practically useless: the counter exists to
  answer whether the *device* is killing the app. Restarts after an update or
  a reboot no longer count.

Verified on the device (Motorola edge 50 neo, Android 16): the widget spans
the full screen width, switches colour and text reliably, a tap starts the
dialogue (not just the app), and TalkBack reads the correct description.
Interruption detection was triggered with `adb shell am force-stop` and
correctly classified the case as `AFTER_INTERRUPTION`.

### 0.6.6 (2026-09-09)

- **The home screen no longer trusts its own memory.** When Android tears
  down only the service and leaves the process standing – Xiaomi devices in
  particular do this – `onDestroy` does not reliably run, and the remembered
  state stays on "running". The large button therefore still read "turn
  voice control off" although nothing was running: pressing it did the
  opposite of what it said.
  On opening the app the state is now queried from the system instead of
  read from memory, and a notice says that the phone stopped voice control.
  Reported by a tester on a Xiaomi Redmi 13C who already had battery
  optimisation disabled.

### 0.6.5 (2026-09-07)

- **The app now says when Android has killed it.** A tester reported that
  the app kept shutting itself down. Whether that was true could not be
  established: a foreground service terminated by the system is **not a
  crash** and therefore appears in no statistic at all – not even in Android
  Vitals. And the app itself simply fell silent. Someone who cannot see the
  screen only notices when they want to make a call and nothing happens.
  It now detects the case
  ([`InterruptionDetector`](app/src/main/java/org/dialos/mobil/StartCause.kt))
  and announces on the next start that it was interrupted, along with a hint
  about battery optimisation. The distinction is made via the phone's uptime:
  if it runs backwards, there was a reboot in between, and that is not an
  interruption. "Info & settings" additionally shows **how often** it
  happened and when last. That answers the question without a cable, without
  a log and without the Play Console.
- **Home screen widget**, spanning the full width. An app icon among twenty
  others is not a usable target for this audience; a bar across the whole
  screen is. It also shows whether voice control is running – previously
  only visible in the notification shade. One tap turns it on and asks "Wen
  möchten Sie anrufen?" straight away, rather than merely opening the app.
  State changes use colour **and** text, because colour alone is not enough.

### 0.6.4 (2026-09-06)

Dictating phone numbers was unusable, and not because of the recogniser. One
test report exposed four things at once.

- **The app deafened itself while you dictated.** After every recognised
  block of digits it read back the *entire number so far* &#8211; and while
  it speaks, the microphone is off. Anyone dictating fluently spoke straight
  into that gap, and those digits were lost. The longer the number, the
  longer the announcement, the bigger the gap. That happened twice during the
  test, one digit each time. The app now confirms only the **newly added**
  digits.
- **A half-dictated number is no longer lost to a timeout.** The wait was a
  flat 15 seconds &#8211; including while dictating, where you may first have
  to look a number up or read it off a note. When it expired, the app called
  `goIdle()` and silently discarded **every** digit. Dictation now allows 45
  seconds, and if digits are present when it expires, the app asks about them
  instead of throwing them away.
- **After dictating, the app now says what to do.** It used to read the
  digits back and then fall silent. The hint about “fertig” came once at the
  very start &#8211; and, as the report put it, was long forgotten by the
  time dictation was over. The instruction is therefore repeated after the
  first block of digits, and not after that, so it does not get in the way.
- **Single digits can be taken back.** “Letzte Ziffer löschen”, “eine zurück”
  or “rückgängig” removes exactly one. Previously there was only “löschen”
  &#8211; so a single swallowed digit meant respeaking an eleven-digit number
  from scratch.

The spoken help text and the instructions under “Info &amp; settings” know
the new commands too.

### 0.6.3 (2026-09-05)

The first release driven by feedback from the closed test. Two testers
independently reported problems on the same day that turned out to be
plainly provable in the code.

- **"Ja bitte" and "nein danke" are understood.** Command words were matched
  against the *whole* sentence rather than word by word, so the most common
  form of answer failed – even though "ja" and "bitte" were both in the list
  individually. This affected every user, not just the one who reported it.
- **Similar-sounding names no longer crowd out the intended one.** "Michelle
  anrufen" stubbornly suggested Michaels. Michelle, Michel and Michael share
  the same Cologne phonetic code (645) and therefore scored identically; on
  a tie the alphabetical order decided, and the three suggestion slots were
  taken by Michaels. A pure sound-alike match now scores lower than a real
  one ([`NameMatcher.PHONETIC_MAX`](app/src/main/java/org/dialos/mobil/NameMatcher.kt)).
  Meier/Maier/Mayer/Meyer are still found.
- **Number types in the command:** "Michaela privat anrufen" now dials the
  private number, and "privat", "mobil" or "Arbeit" is enough as an answer
  to the confirmation. Previously "privat" ended up inside the name and
  disturbed the search, and as an answer it was not understood – anyone with
  two numbers stored could only reach the second by repeatedly saying no.
- **Dead end after "Das habe ich nicht verstanden" removed.** The app was
  meant to repeat the question afterwards but repeated the notice instead:
  the field holding the last question was overwritten by the notice itself.
- **The timeout announcement now tells the truth.** It used to say "Ich
  beende die Sprachsteuerung" although the app merely returned to listening
  for the wake phrase. Someone who cannot see the screen would switch it
  back on unnecessarily.
- **Voice and speech rate are configurable** (Info & settings). Four speed
  steps from slow to very fast, plus whichever German voices the device
  offers. Both as step-forward buttons rather than sliders, and **each tap
  plays a sample** – there is no other way to judge a voice without sight.

Not fixed: **the app does not understand Swiss German.** The Vosk model is
trained on standard German; no Swiss model of this size exists. That is a
limit, not a pending fix.

### 0.6.2 (2026-08-20)

- **Telephony is now required** (`uses-feature … required="true"`). Google
  Play therefore only offers the app to devices with telephony. Previously
  it could have been installed on a Wi-Fi tablet, would listen, recognise
  the name – and then fail silently when dialling. Exactly the failure mode
  that is worst for a blind user. Side effect: no tablet screenshots are
  needed for the store.
- **Internal test release published in the Play Console** (versionCode 8,
  49.9 MB download).
- **Recruiting testers on dialos.org**, new folder
  [`website/`](website/README.md): scripts that create both posts –
  [German](https://dialos.org/dialos-mobil-tester-gesucht/) and
  [English](https://dialos.org/dialos-mobil-testers-wanted/) – along with
  the sign-up form through the WordPress REST API, plus a small plugin for
  the comment sections. Everything is re-runnable, so that text changes
  happen in the repository rather than in the WordPress editor.


### 0.6.1 (2026-08-19)

**The first complete call by voice worked** – from „Sprachsteuerung
starten" through to a connected call. Two defects had been in the way:

- **Phone numbers are normalised before dialling.** Address books often
  store them as „+49 176 1234-5678"; the spaces make the `tel:` URI
  invalid, and the telecom service discards it **silently**, without
  throwing. That is exactly why the log showed nothing: no error, no call.
- **The call watcher cleaned up before the call existed.** It checked two
  seconds after dialling whether the audio mode was normal and concluded
  „call ended". A call needs several seconds to start ringing. There is now
  a 12-second grace period – and if nothing materialises, **the app says so
  out loud** instead of silently dropping back to idle.

**The confirmations now sound like questions.** In testing Stephan waited
7 and 10 seconds respectively because „Carola Stern, Mobil, anrufen?" did
not prompt him to answer – both delays are in the log. Android TTS does not
reliably produce a rising intonation, so the instruction is now part of the
sentence: „Soll ich … anrufen? **Sagen Sie Ja oder Nein.**"

*Which of the two fixes actually unblocked the call is open: both went in
together, and the successful test ran with the cable disconnected. The
number is the likelier candidate, because the watcher would not have
prevented the call, only reset the dialogue.*

- Also: phone numbers now appear only partially in the log (`017…78`),
  no longer in clear text.


### 0.6.0 (2026-08-19)

- **Text messages removed again.** Not for technical reasons – the path
  worked – but for publication: Google only grants the `SEND_SMS`
  permission for a closed list of approved use cases, and a voice dialler
  is not on it. The app would most likely have failed review. Calling
  remains the core function, and its chances there are good.
- The app therefore no longer requests any SMS permission.
- Play Store preparation: release signing, app bundle, privacy policy,
  store texts, data safety guidance and a
  [step-by-step guide](docs/veroeffentlichung.md) (German).
- Verified against the built package: the app has **no internet
  permission** – it is technically unable to send anything.


### 0.5.0 (2026-08-19)

- **Choosing the SIM by voice on dual SIM/eSIM devices.** After the
  confirmation the app asks „Über welche Karte? Eins: 1&1. Zwei: YELLLOW.“ –
  answer with the number or the carrier name. The name announced is the one
  from the Android settings, not the network operator – while roaming the
  card would otherwise be called „3 AT – 1&1“. Applies to calls and messages
  alike. With only one card, no question is asked.
- Without that choice Android silently uses the default card – possibly the
  wrong one, and abroad the expensive one.
- „Infos & Einstellungen“ now shows the version and a link to the source
  code on GitHub.
- The logo on the start screen is twice the size; the start screen scrolls
  in exchange, so nothing is cut off on small devices.


### 0.4.0 (2026-08-19)

- **Start screen reduced to the essentials:** volume, contrast, status and a
  single button of twice the height. Everything about setup now sits behind
  „Infos & Einstellungen“ at the bottom centre.
- **The button now starts the dialogue straight away.** Previously you had
  to say „Sprachsteuerung starten“ after switching on – redundant when the
  phone is already in your hand. The wake phrase still matters later (phone
  in a pocket, screen off).
- „Infos & Einstellungen“ has a large **Back button** at the top and bottom
  and returns to the start screen by itself after 10 seconds without a
  touch – someone who lands there by accident may not find their way back.
- Fixed: `fitsSystemWindows` overwrites the padding of the view it sits on –
  the buttons were stuck to the screen edges because of it.

### 0.3.0 (2026-08-19)

- **Contrast toggle** top right: black background, yellow buttons. Yellow
  on black stays legible under glare and contrast loss, where blue on white
  blurs.
- **Volume toggle** top left, 50 % or 100 %. The app sets 50 % on every
  start – the test device sat at 27 %, and someone who cannot see only
  notices too quiet an announcement when the app appears to go silent.
  Speech output now uses the media stream, the same one the phone's volume
  keys control.
- The **„Jetzt sprechen“ button was removed**: it was redundant. With voice
  control off it does not help; with it on, the wake phrase is enough. The
  quick settings tile, launcher shortcut and assistant intent remain for a
  manual start.
- The „Permissions“ and „Continuous operation“ sections are styled more
  quietly – they are needed once, not daily.

### 0.2.0 (2026-08-19)

- **Text messages by voice**: „Schreibe Max Mustermann“, dictate the text,
  finish with „fertig“. The app reads recipient and text back and only
  sends after „Ja“.
- The confirmation before sending is deliberately not switchable – unlike a
  call, an SMS is irreversible and costs money.
- Mobile numbers are preferred for messages; if only a landline is left,
  the app says so out loud.
- New voice commands: „Löschen“ / „noch mal von vorn“ discards the dictated
  text. Dictation allows a longer pause (30 instead of 15 seconds).
- The SIM card comes from the Android default for SMS – on dual-SIM devices
  the one already configured there is used.
- First run on real hardware passed (Motorola edge 50 neo, Android 16).
- Material You colours removed and all colour roles set explicitly: the
  DialOS blue had been replaced by tones derived from the wallpaper,
  leaving contrast to chance.

### 0.1.0 (2026-08-19)

- First version.
- Wake phrase „Sprachsteuerung starten“ via continuously running offline
  recognition (Vosk, German model).
- Calling contacts by name using Cologne phonetics, Levenshtein similarity
  and token comparison; disambiguation prompt for ambiguous matches.
- Dictating phone numbers by voice, including German numerals, country
  code via „plus“ and „doppel“ for repeated digits.
- Confirmation before dialling, can be switched off.
- Additional entry points: button, quick settings tile, launcher shortcut
  and assistant intent.
- Autostart after reboot with a fallback notification on Android 14+.
- Accessible UI with very large buttons and a live region for TalkBack.
- Logo and colour scheme taken from DialOS.
- The model is fetched at build time and is not stored in the repository.
