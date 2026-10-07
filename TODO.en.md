[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – DialOS Mobile

> **Note on this file:** it lags behind [TODO.md](TODO.md) by a fair
> stretch – the German version carries entries from 2026-10-01 onwards
> that were never carried over here, including the whole section on
> recruiting testers via self-help associations. The German file is the
> authoritative one; catching this one up is its own task and is listed
> under "Technical".

> **Break until 2026-10-19.** The last working day was 2026-10-07.
>
> **Both on-device measurements are done; 0.6.17 is no longer blocked.**
> The international prefix comes through in full, and the question about
> how to speak is settled: volume is the cause, distance is not. Both are
> under "Urgent", the figures live in `NummerMitEinleitungTest`.
>
> Whoever picks this up again has two starting points. The easier one:
> carry the guidance on how to speak into the tester checklist – it is now
> in the app and in both READMEs, but not there. The more important one:
> a tester's Galaxy A14, which kills the app one and a half times a day.
> The battery exemption is settled there (it is set, and it is not enough);
> what remains are Samsung's additional power-saving mechanisms – and those
> need her Android version first.
>
> One draft is also still missing: a short mail offering a **phone call**
> rather than yet another request. She cannot make that setting herself,
> she needs sighted help – a fourth mail carrying a task would be the wrong
> answer to that.
>
> **For placing new feedback:** the twelve testers are still running
> **0.6.14** from 2026-09-21 – with the self-trigger, the uninvited volume
> change and the crash after every update. Anything reported during this
> period may well have been fixed long ago. Always check the version first,
> then the finding.

## Distribution outside the Play Store (as of 2026-10-06)

- [x] **Direct download on dialos.org.** Two pages, German and English, by
      now built out into full app pages: **`/dialos-mobil/` (ID 720) and
      `/en/dialos-mobil/` (ID 721)**. Created with
      `website/dialos_seite.py`, renamed and expanded with
      `dialos_seite_ausbauen.py`. The first slugs
      (`/dialos-mobil-herunterladen/`, `/en/download-dialos-mobil/`) are
      dead – checked on 2026-10-06: HTTP 404, no redirect. If anything still
      links there, it has to go.
- [x] **Linked in the menu** – "Sprachsteuerung" → "DialOS Mobil" (menu 7,
      parent item 35, position 3), set with
      `website/dialos_seite_verlinken.py`. This was necessary because menu 7
      has `auto_add = False`; new pages therefore sit in no menu at all until
      someone creates the item. Plus links on both start pages (2 and 135).
- [x] **The file ships as a GitHub release** (`v0.6.17`), not from the media
      library. It is the **"signed universal APK" from the Play Console**,
      not our own build – verified: `CN=Android, O=Google Inc.`, certificate
      SHA-256 `4219a932…77b0`. Only that lets users move between a website
      install and a Store install without uninstalling. It is available in
      the bundle explorer even while the bundle is only a draft.
- [ ] **This is the app's first public distribution** – until now the only
      way in was an invitation to the closed test. Stephan's decision of
      2026-10-05: go ahead, even though it is not 1.x yet. The pages say so
      openly ("This is a test build").
- [ ] **Not yet measured:** how many device models drop out from leaving
      x86_64 behind. For NaturlustTrailGuide it was 8 out of 19,171; here it
      should be fewer, because the app requires telephony hardware anyway.
      It will show on the review page at the next submission.
- [x] **Installation instructions on both pages** – 2026-10-06, prompted by
      Stephan's question. Before that there was a single sentence, and the
      checksum came with `sha256sum` – a command that does not exist on the
      phone, which is exactly where the page gets opened. Tool:
      `website/dialos_seite_anleitung.py`. Five steps, a list of the error
      messages and what they mean, and the note that the instructions address
      a helping person – installing is the one part that needs eyes on the
      screen, and this app's users are people who cannot do that.
- [x] **Every device string was looked up, not recalled.** Dialog texts via
      `aapt2 dump resources` from the test device's `GooglePackageInstaller`
      (Motorola edge 50 neo, Android 16), German and English out of the same
      file – which is why the English page matches an English-language phone.
      The settings screen via a `uiautomator` dump: the Settings resources
      say "Zugelassen/Nicht zugelassen" there, but what is **displayed** is
      "Zulässig/Nicht zulässig". Anyone rebuilding this should trust the
      dump, not the resource file.
- [ ] **Android's developer verification already sits on the device.** Found
      alongside on 2026-10-06: `cannot_install_app_blocked_title` ("App
      developer unverified"), `install_without_verifying`, and a
      `…_fail_closed_…` variant – if the developer cannot be verified, the
      install is blocked, not waved through. It does not apply in Germany
      yet, but the machinery has shipped. **The check needs an internet
      connection** – this app does not even hold the INTERNET permission and
      would then only be installable with a network. Re-check whether the
      regime has kicked in before making any claim about the direct download.

## Open

### Urgent

- [x] **A tester's full name is gone from both the working tree and the
      history – 2026-10-07.** Noticed while checking before a commit:
      43 places
      across 8 files, among them both READMEs (the most visible files
      there are), **`VoiceService.kt`**, `SelbstausloeserTest.kt`,
      `docs/veroeffentlichung.md` and `docs/blindzeln-magazin.md`. Plus
      eleven commits by content and nineteen commit messages.

      This weighed more than a mail address: the surrounding text made
      clear that the person is blind, which phone she uses and how she
      copes with it – health data under the GDPR, and she cannot even read
      the page herself.

      Replaced with "a tester" or "she", and done by hand rather than by
      search-and-replace: mechanically, a possessive like "her second
      point" would have turned into an unreadable sentence, because there
      the name carries the grammar. The prose had to stay readable,
      otherwise the clean-up would have damaged the reasoning – and the
      reasoning is what makes these files worth anything.

      The history was rewritten with `git filter-repo`
      (`--replace-text` for contents, `--replace-message` for messages)
      and force-pushed. Verified afterwards: zero hits in the messages,
      no file in any commit. In the old commits the sentences now read
      clumsily – accepted, nobody reads them any more.

      - [x] **The same run destroyed 1,493 unrelated places, and that
            was my fault.** The rule file had a comment header, and in it
            a line consisting of a **bare `#`** as a visual separator.
            `git filter-repo` took it as a search pattern and replaced
            every `#` in the repository with filter-repo's default
            replacement text – three asterisks, the word REMOVED, three
            asterisks: Markdown
            headings, the shell comments in `gradlew`, `.gitignore`
            entries, the colour values in `colors.xml` (`#FFB300`). The
            repository no longer built.

            **Why this happened even though the docs were read:** the
            man-page does say "Blank lines and lines starting with a # are
            ignored" – but in the description of `--paths-from-file`, not
            of `--replace-text`. The source is unambiguous:
            `get_paths_from_file` skips `#` lines, `get_replace_text`
            does **not**. The latter has no comment handling at all. Every
            comment line is a search pattern there; the long sentences
            merely happened to match nothing.

            Fixed by a second run with a single rule,
            which lives in `~/marker-reparatur.txt`. The reverse
            substitution was unambiguous because the string `REMOVED`
            appeared nowhere in the previous state – checked in the
            backup clone, not assumed.

            **The search string deliberately does not appear verbatim
            here**, only described. Otherwise this very section would
            have become its own casualty: a run that repairs it would
            have destroyed the explanation along the way. Verbatim it
            lives only in the rule file outside the repository.

            The restoration is evidenced by a full comparison against the
            backup: apart from the two TODO files, which were edited
            afterwards, every file is byte-for-byte identical. Plus 89
            tests with `--rerun-tasks`, i.e. without the Gradle cache.

            **The lesson for next time:** rule files for
            `--replace-text` contain **no comments**, and every line
            starts with `literal:`. Check beforehand what filter-repo
            actually reads from them – this is risk-free:

            ```
            python3 -c "
            import sys; sys.path.insert(0, '/usr/lib/python3.14/site-packages')
            import git_filter_repo as fr
            r = fr.FilteringOptions.get_replace_text('/home/stephan/namen-ersetzungen.txt')
            print('Regex:', len(r['regexes']))
            for m, e in r['literals']: print(repr(m), '->', repr(e))
            "
            ```

            The rule files at `~/namen-ersetzungen.txt` and
            `~/marker-reparatur.txt` are comment-free by now and were
            checked with exactly that call.

      - [x] **The history is healed – 2026-10-07.** It was 97 of 98
            commits plus 21 places across six commit messages (`##`
            headings in running text, in one case a colour value).
            Verified: no marker left, neither in a file nor in a message.
            The oldest commit has its `#!/bin/sh` back, 89 tests green
            with `--rerun-tasks`, tag and release untouched.

            What ran:

            ```
            cd "/mnt/raid/eigene Daten/GitHub/Stephan-Lefty/DialOS-Mobil"
            git bundle create ~/dialos-mobil-vor-marker-reparatur.bundle --all
            git filter-repo --replace-text ~/marker-reparatur.txt --replace-message ~/marker-reparatur.txt --force
            git remote add origin https://github.com/Stephan-Lefty/DialOS-Mobil.git
            git push --force origin main
            git push --force origin v0.6.17
            ```

            The first line is new and deliberate: a fresh backup before
            history gets rewritten again. The earlier one
            (`~/dialos-mobil-vor-filter-repo.bundle`) is what made
            today's repair provable in the first place.

            As a counter-check – and there is a lesson here for next
            time. The first draft searched for `REMOVED` and then
            reported **1** instead of 0. That was not a leftover but the
            word in the running text of a commit message. A check has to
            search for the marker, not for one word out of it, or it
            raises a false alarm at exactly the moment you want to trust
            it:

            ```
            git log --all --format='%s%n%b' | grep -c '\*\*\*REMOVED\*\*\*'
            git grep -l '\*\*\*REMOVED\*\*\*' $(git rev-list --all) | head
            ```
            The first must return `0`, the second stay empty. Before the
            repair it was 21 and 97 commits respectively.

            **One spot stayed disfigured**, as announced and accepted:
            the message of "Die Zerstoerung durch meine
            filter-repo-Regeldatei zuruecknehmen" quoted the search
            string verbatim and lost it in the process. It now reads
            "ueberall war aus \"#\" die Zeichenkette # geworden" –
            circular. The rest of that message still explains the cause
            in full, and the detailed account lives here.

      The 19 screenshots were checked with OCR (`tesseract -l deu+eng`),
      because `filter-repo --replace-text` does not reach into images. The
      only hit was "Anna Berger" in
      `screenshots/verbaende/dialos-mobil-so-funktioniert-es-1500.png` –
      the invented example contact from the dialogue walkthrough, not a
      real name.

- [x] **Measured on 2026-10-07: loud speech is the cause, distance is
      not.** A tester on 2026-10-01: "She doesn't understand me,
      even though I hold the device in my hand and speak loudly and clearly.
      […] What am I doing wrong?"

      The same nine-digit number seven times, Motorola edge 50 neo:
      **3 out of 3 correct at normal volume, 1 out of 4 when spoken
      emphatically loudly.** From a metre away it was flawless – so holding
      the device close to one's face achieves nothing. The suspicion about
      the microphone on the underside being covered was not borne out; it
      is volume alone.

      Half the suspicion was right and half was wrong, and writing both
      into the guidance unmeasured would have produced a useless hint
      ("closer and quieter").

      Carried into the product: `say_help` and `help_body` in both
      languages, the "Known limits" section in both READMEs. Figures as
      regression cases in `NummerMitEinleitungTest`
      (`laut gesprochen bleibt unvollstaendig`).

      - [ ] **Still open: the tester checklist.** The hint is not there
            yet, and it belongs at the very top – ahead of every command.
            Do this before the next round starts.

      - [ ] **Consider a level warning.** Guidance only reaches whoever
            opens the help. She spoke loudly *because* she was not being
            understood – and had no way of learning that this was the
            problem. Vosk hands over the audio buffers; the amplitude could
            be read and, on clipping, announced once: "Please speak a
            little more quietly."

            This is the only measure that fixes the fault where it arises.
            To be settled: the threshold (must be measured, not guessed)
            and how often the announcement may fire before it becomes a
            nuisance.

      **What cannot be fixed, and why:** in the loud case "eins sieben"
      fell apart into "alles selber" and "einsehen sehen". Until then the
      mishearings always sat close to the number word and could be added to
      the table; these cannot. Treating "sehen" or "alles" as a digit would
      be indefensible, and the stem of "sieben" is "sie" – a pronoun. A
      stem or similarity rule is therefore deliberately **not** built.
      Anyone considering one later should have read the test
      `die Zerfallswoerter bleiben Woerter`.

- [x] **The international prefix is verified – confirmed on device on
      2026-10-07.** Spoken with the test build in a quiet room:

      ```
      Nummer wählen null null vier neun eins sieben sechs acht null
      ```

      It read back **0 0 4 9 1 7 6 8 0**, with no intervening question.
      Before the fix it produced `0 0 1 7 6 8 0` – "vielen" and "neuen"
      were missing from the mishearing table, and the prefix vanished
      without trace into a number that sounded perfectly valid. The
      recommendation in the help is thereby covered. **0.6.17 is clear on
      this count.**

      The caveat that belongs with it: this holds for digits spoken at
      **normal volume**. The same sentence spoken emphatically loudly
      produced a wrong number three times in a row – see the finding on how
      to speak above.

- [x] **"nein" was understood as "ein", and the app did nothing.** Twice
      in the run of 2026-10-07. The app repeated the very same question –
      on screen a shrug, for a blind user an app that has stopped
      responding to anything.

      Fixed in `DialogController.alsNeinWennVerschluckt`: in the
      confirmation step a standalone "ein" counts as no.

      **Why the reinterpretation lives there and not in `CommandParser`:**
      "ein" is the number word for one and has to stay that way while
      dictating – otherwise every dictated number would lose its ones. The
      confirmation step involves no dictation, only yes or no; that is
      precisely where the reinterpretation is safe, and nowhere else. The
      counter-check is a test (`ein bleibt beim Diktieren die Eins`).

      Anyone extending the `VERSCHLUCKTES_NEIN` list should know: every
      entry turns a misunderstood utterance into a no, and a false no
      throws away a fully dictated number. Hence the single measured word.

      "nein" was *not* entered as a mishearing of "neun", even though it
      was measured in the same run – it is the single most important
      rejection command in the whole app. Recorded in the test
      `nein bleibt eine Ablehnung und wird keine Neun`.

- [x] ~~**The app crashed after every app update.**~~ **Found and fixed on
      2026-10-01**, incidentally while verifying the interruption
      announcement – without the on-device test nobody would have noticed.
      `BootReceiver` kicks off the service via `startForegroundService()` on
      `MY_PACKAGE_REPLACED`; the service detects the background start, posts
      the notification and calls `stopSelf()` – but never
      `startForeground()`. Android holds it to that contract and kills the
      process with `ForegroundServiceDidNotStartInTimeException`. **This hit
      every tester on every Play update**, not just the developer. Fixed by
      attempting the foreground start anyway: it fails reliably with a
      `SecurityException` (microphone type from the background), but the
      caught failure discharges the contract. What remains is the tappable
      notification – which is what the branch always intended.

- [x] ~~**Test and Play builds are indistinguishable by their icon.**~~
      **Fixed on 2026-10-01.** This had spoiled a test run twice: on
      2026-10-01 the 0.6.14 build running in parallel spoke its interruption
      announcement while the 0.6.15 test build listened and processed it as
      speech input. The name has differed since 0.6.15 ("DialOS Mobil
      (Test)"), the icon has not – and on the home screen the name is far
      less noticeable than the colour. The test build now has an amber
      background (`app/src/debug/res/values/colors.xml`, overriding
      `ic_launcher_background`). Amber because it sits furthest from white
      while keeping the blue-green mark legible; the palette's blues and
      greens are out, as they should distinguish rather than resemble.
      Verified on the device.

      A side finding from it, not a bug but a property: **two DialOS
      instances within earshot wake each other.** That also affects DialOS
      on the PC, which listens for the same phrase, and is already noted
      under the second wake word.

- [ ] **The app activates itself – it hears its own announcement.** A tester's
      third report (2026-10-01) finally gives the wording: "It says the
      device switched the app off and it is working again, then it asks who
      I want to call? No activation from me." That makes the cause readable
      in the code, and it is the same one behind her "life of its own" from
      2026-09-27.

      The chain:

      1. `VoiceService.onEngineReady()` calls `engine.start()` on line 264 –
         the microphone is live from here on.
      2. Line 268 `sorgeFuerHoerbarkeit()` raises the volume.
      3. Lines 276 and 278 speak **directly via `speaker.speak()`**, without
         pausing the microphone. That pause lives only in
         `DialogController.say()` (line 798) – the service's own two
         announcements bypass it.
      4. And the texts contain the wake word verbatim:
         `say_started_contacts` ends with "**Sagen Sie: Sprachsteuerung
         starten.**", `say_after_interruption` opens with "Die
         **Sprachsteuerung** wurde vom Telefon unterbrochen".
      5. Vosk is listening, picks up the wake word from the app's own
         speaker and activates → "Sprachsteuerung bereit. Wen möchten Sie
         anrufen?" Exactly the sentence that was reported.

      **Measured on 2026-10-01 – and the measurement found more than
      expected.** `SelbstausloeserTest` runs the announcement texts through
      `CommandParser.isWakePhrase`:

      | Announcement | Similarity | wakes? |
      |---|---|---|
      | `say_started_contacts` | contains "sprachsteuerung starten" verbatim | yes |
      | `say_after_interruption` (old) | "sprachsteuerung wurde" = **0.783** | **yes** |
      | `say_after_interruption` (new) | – | no |

      The second value was the surprise: the interruption announcement
      contains no "starten" at all, and it still woke the app. The word
      pair "sprachsteuerung **wurde**" scores 0.783 against "sprachsteuerung
      **starten**", and the threshold sits at 0.70. **So the microphone
      pause alone would not have fixed it** – the wording was dangerous on
      its own.

      Raising the threshold would be the wrong move: the only missed real
      call from the 2026-09-21 measurement ("sprachstörungen starten")
      scored 0.78, i.e. the same. There is no gap there.

      - [x] **Microphone pause retrofitted.** New helper
            `VoiceService.sagOhneMitzuhoeren()`, both announcements now go
            through it – same logic as `DialogController.say()`.
      - [x] **Wording defused.** "Die Sprachsteuerung wurde vom Telefon
            unterbrochen" → "Ich wurde vom Telefon kurz unterbrochen und bin
            jetzt wieder da." Both resource files.
      - [x] **Four tests for it** (`SelbstausloeserTest`), including the old
            wording as a reminder. 65 tests total, green, lint clean.
      - [ ] **Verify on the device** (midday 2026-10-01): switch on, stay
            silent, and listen for whether it still asks "Wen möchten Sie
            anrufen?" on its own. The log must show Vosk recognising nothing
            at all during the announcement.
      - [ ] **Still open: the start announcement stays risky.** It
            deliberately says "Sagen Sie: Sprachsteuerung starten" – as
            guidance for blind users that is valuable. The pause covers it,
            but reverb or a second device within earshot remain conceivable,
            and DialOS on the PC listens for the same word. Decide after the
            on-device measurement.

- [ ] **The app raises the volume unasked.** A tester's second point from
      2026-10-01: "It is good that the app sets the volume, but it goes to
      loud automatically when you do not want to open the app."
      `sorgeFuerHoerbarkeit()` runs on **every** `onEngineReady`, including
      the silent restart after Android kills the service – with no user
      action at all. The reason behind it is sound (an inaudible
      announcement is worthless, see `VolumeController` lines 39–43), the
      side effect is not: anyone who deliberately silenced the phone gets
      overruled.

      Tied to the item above: if the app stops talking unasked after a
      restart, it does not need to raise the volume there either. Then this
      resolves by itself.

- [ ] **Decide whether the interruption announcement should stay at all.**
      `say_after_interruption` explains the battery-optimisation setting.
      For her that advice is not actionable, and the announcement comes
      unannounced out of a silent phone. Options: drop it and note it in the
      notification only, or once a day instead of on every restart. This is
      a product decision – ask Stephan, do not decide it alone.

- [x] ~~**The switched-off hotword setting fails silently.**~~ **Found and
      fixed on 2026-09-21 (0.6.14).** The app recognised "sprachsteuerung
      starten" twice, cleanly – it is in the log verbatim – and did nothing,
      because "Auf ‚Sprachsteuerung starten' hören" was switched off in the
      settings. No announcement, no sound, no reaction. The same class of bug
      as 0.6.11, one level up: someone who cannot see the screen has no way of
      telling "doesn't hear me" from "is ignoring me on purpose", and looks
      for the fault in their own pronunciation. That is exactly how the report
      arose that the wake phrase does not work. The announcement therefore
      opens with "Ich habe Sie verstanden" and comes once per off-phase.

- [ ] **Offer to switch the setting back on by voice.** The 0.6.14 hint names
      the way to the switch – but navigating there is precisely what this
      audience finds hard. Better would be: "Shall I turn it back on?" That
      needs a new dialogue state and was therefore deliberately left out of
      0.6.14, which was already trailing 0.6.13.

- [ ] **Reconsider how easy the switch is to reach** when turning it off
      disables the app's core function.

- [ ] **Ask the tester specifically** whether this switch is off on her
      phone. Her
      report fits it exactly, and it would be the simplest explanation.

- [ ] **The failed-attempt counter can only be checked on a device.** The
      heart of 0.6.15 – the app stops after three failures – has **no** unit
      test, because `DialogController` needs an Android `Context` and the
      project only has `junit` as a test dependency. The `CommandParser` half
      of the same change *is* tested (`EigenlebenTest`). To check on the
      device: say nonsense three times → announcement and stop; nonsense
      twice, then a real name → the count starts over; nonsense three times
      while dictating digits → the digits survive and are read back for
      confirmation.

- [ ] **Make `DialogController` testable.** It is the heart of the app and the
      only larger piece without tests. Robolectric would be the way; that is a
      task of its own, not something to bolt onto a bug fix.

- [ ] **There is no CI.** Tests exist, but `.github/workflows/` does not –
      so every release rests on a check run on a single machine. The 0.6.13
      release showed how quickly that goes wrong: a Gradle call piped through
      `tail` reported success, and the bundle under review was five weeks old.
      The obstacle is known: `prepareVoskModel` downloads 46 MB and would need
      a cache in the workflow. Worth checking whether `testDebugUnitTest` needs
      the model at all – if not, a lean test workflow without the download
      would do.

- [x] ~~**"MA40" is not found**~~ **Fixed in 0.6.15.** Number words are now
      turned into digits and split names rejoined – both as additional
      variants, taking the maximum. Only "Heli" remains, see below.

- [ ] **"Heli" is not found** (reported by a tester, 27 Sept 2026).
      Explicitly **not** a volume problem: "Both times it was quiet around
      me." The counter from 0.6.15 does not help here – it prevents the
      endless re-asking, not the not-finding.

      **This case differs from MA40, and that is why it stays open.** There it
      was provable that it could never have worked. Here nearly every
      plausible mishearing already matched before 0.6.15 (threshold is
      `NameMatcher.THRESHOLD` = 0.62):

      | spoken | against "Heli" | |
      |---|---|---|
      | `heli`, `helli`, `helly`, `eli`, `heil`, `helle`, `hely` | 0.825–1.000 | match |
      | `geli` | 0.750 | match |
      | `he li` | 0.800 / rejoined 1.000 | match |
      | `hell ich` | 0.500 / rejoined 0.571 | **miss** |
      | `heidi` | 0.600 | miss |

      So it must have been a substantial mishearing – perhaps a split into two
      full words such as "hell ich". That is a guess, not a measurement. **Do
      not change blindly:** lowering the threshold to catch 0.571 would create
      false matches, and four-letter contact names are inherently fragile
      against Levenshtein (one error costs 0.25 there).

      **What is missing is the wording.** Before "habe ich in den Kontakten
      nicht gefunden" the app names exactly what it heard – the request to
      write that down once is in the mail to her.

- [x] ~~**A phone number spoken in the name state is searched as a name.**~~
      **Fixed in 0.6.15:** if no contact matches and the words add up to at
      least six digits, the app reads the number back for confirmation.
      Original finding: a tester read out the MA40's phone number and heard "that
      is not in the contacts". Anyone wanting to speak a number first had to
      say "Nummer wählen" – a magic word in front of the most natural action
      there is. `GermanNumbers.toDigits` already recognises the digits
      reliably (measured: ten digits from a spoken number), while "ma vierzig"
      yields only two – a threshold of six separates the two cleanly.

### To check

- [ ] **Does the app work with headphones?** Asked by Stephan on 28 Sept 2026
      while preparing the device test – and the question is bigger than it
      sounds: many blind people wear a headset all day, because the screen
      reader would otherwise be audible to everyone. If the app cannot cope
      with that, it affects a substantial part of the target group.

      **There is nothing about it in the code** – no `startBluetoothSco`, no
      audio route, no `AudioSource`. The app leaves the choice of microphone
      to Android. What actually happens is untested, and separately so for two
      cases: wired headset with a microphone, and Bluetooth. For Bluetooth it
      is additionally open whether speech output lands in the ear while the
      microphone stays on the phone – which would be good – or whether the
      user misses the announcements, which would be bad.

      The regular device test deliberately runs **without** a headset: the
      testers hold the phone in their hand, and anything else would not be
      comparable.

- [ ] **Where did the four counts on 2026-09-21 come from?** The counter rose
      from 18 to 23 over the day, across three `installDebug` runs and three
      `force-stop`s.

      **Measured the same day, and two assumptions are gone:**

      - An `install -r` does **not** count – the counter stayed at 23. But
        not because the 0.6.7 exception applies: the service bails out
        earlier. After the update it starts from the background, gets no
        microphone and stops itself before `noteStartCause` is reached (log:
        "Hintergrundstart erkannt, Mikrofon laut System erlaubt: false").
        The documented reasoning does not match the actual path.
      - A `force-stop` does count – verified (22 → 23). That is correct.

      What remains open is the arithmetic: three `force-stop`s explain three
      counts, five were recorded. Two are unexplained. The suspect is still
      the `START_STICKY` path where `intent == null` – `expected` can never
      be true there (`intent?.getBooleanExtra(…) == true` is false for
      null), so an expected restart would register as an interruption.
      Whether that path is ever reached is untested: the background-start
      guard from 0.6.11 may catch it before counting.

      Why it matters: the counter is the only figure that can show a
      manufacturer is killing the app (see Michaela's Xiaomi). If it
      over-counts, an Android problem looks more urgent than it is.

- [x] ~~Does an audible prompt really arrive after an app update?~~
      **Verified on 2026-09-21.** After `installDebug` the notification was
      in the system with `channel=voice_control_boot`, **`importance=4`**
      (sound and heads-up) and `category=reminder`. The core fix from 0.6.11
      therefore demonstrably works – until now only the channel was
      verified, not delivery in the update case.

      This also confirmed what matters practically for the testers: **after
      every update voice control is off** and has to be switched back on via
      the notification. That is not a bug but Android's background-start
      restriction – but it belongs in the announcement of future updates.

- [ ] **The stop intent from outside does not work.** `am
      start-foreground-service -a org.dialos.mobil.action.STOP` did not stop
      the service on 2026-09-21; only `force-stop` did. Irrelevant in daily
      use (the button in the app and the notification both work), but a
      nuisance when testing on the device, because `force-stop` skews the
      interruption counter. Check whether `onStartCommand` sees the action at
      all when started from the background.

### Still to be verified with a voice

- [ ] **SIM choice in the dialogue** – card detection is confirmed on the
      device („1&1“ and „YELLLOW“), the spoken question itself is not. Check
      that both „Eins“ and the carrier name work and that the call goes out
      over the right card.
- [x] ~~**Measure the wake phrase.**~~ **Done on 2026-09-21** (Motorola edge
      50 neo): five of six calls recognised; the one that was missed came
      through as „sprachstörungen starten“ at 0.78 – just under the guessed
      threshold of 0.82. Not a single false alarm out of a quarter of an hour
      of room noise, highest value 0.26. The threshold was therefore lowered
      to 0.70 (`CommandParser.WAKE_MIN_RATIO`), and the real sentences are
      regression cases in `CommandParserTest`. Shipped in 0.6.13.

### Verify on real hardware

- [x] ~~Measure wake phrase accuracy.~~ **Done on 2026-09-21**, see above.
      What remains open is the long-run counter-check: a quarter of an hour
      of room noise is a sample, not everyday use. If the test group reports
      false alarms, the threshold belongs back on the bench.
- [ ] Verify that recognition really is silent while the app itself is
      speaking (echo problem) – especially over the loudspeaker.
- [ ] Measure battery drain over a full day.
- [ ] Test with a Bluetooth headset (the same AIRHUG 01 as DialOS?) – does
      Vosk pick up the headset microphone?
- [ ] Verify autostart after reboot on Android 14/15: does the fallback
      notification appear, and can a blind user find it at all?

### Features

- [ ] Answer calls by voice („Abheben“ / „Annehmen“) – right now the app
      can only place calls, not accept them. Needs `ANSWER_PHONE_CALLS`.
- [ ] Hang up by voice.
- [ ] Turn on the loudspeaker automatically
      (`EXTRA_START_CALL_WITH_SPEAKERPHONE`) so a blind user does not have
      to hold the phone to their ear – as a setting.
- [ ] Speed dial / favourites („Ruf meine Tochter an“) with custom labels
      pointing at a contact.
- [ ] **Make the call route selectable: WhatsApp, Signal, Telegram instead
      of the mobile network.** Requested during the closed test
      (2026-09-10): poor mobile reception in the office, good Wi-Fi – calls
      there already go through WhatsApp. Conceivable as a default setting or
      as a question before each call.
      **What needs clarifying:** these apps offer calls through a contact
      entry (their own MIME type in `ContactsContract.Data`), not through an
      open interface. So it only works for contacts linked there, and it
      requires a `<queries>` entry in the manifest to see the apps at all.
      **Not to be confused with the SMS finding from 0.6.0:** that was about
      sending messages, which genuinely has no interface. Calls are a
      different question and untested.
      The "no internet permission" promise is unaffected – the other app
      places the call, not DialOS Mobil.
      Samuel himself sees it as a wish for a later version.
- [ ] A short beep before listening (like `dialos-start-ansage.py` in
      DialOS – a missing start signal was a real bug there).

### From the closed test (since 2026-09-05)

- [x] ~~Check the widget on a device.~~ **Done 2026-09-09** on the Motorola
      edge 50 neo: full width, colour and text switch reliably, a tap starts
      the dialogue (not just the app), TalkBack reads the correct
      description. It also revealed that the text "Jetzt sprechen" was
      factually wrong – fixed in 0.6.7.
- [ ] Check the widget on a **narrow** device. So far only a 1200 px wide
      screen has been tested.
- [x] ~~Verify the widget offer on a device.~~ **Done 2026-09-09** on the
      Motorola: the button appears only when no widget is present, the
      launcher shows its confirmation dialogue with a preview, "Add" places
      the bar, and afterwards the button is gone.
- [ ] Verify the same **on a Xiaomi**. There "home screen shortcuts" was set
      to red – the request may fail for exactly that reason, in which case
      the fallback hint takes over.
- [x] ~~Verify interruption detection on a device.~~ **Done 2026-09-09:**
      triggered with `adb shell am force-stop`, the start cause was correctly
      classified as `AFTER_INTERRUPTION` and counted. It also revealed that
      app updates were being counted too – fixed in 0.6.7, since the counter
      would otherwise be useless as a diagnostic.
- [ ] Still to hear: the **announcement** after an interruption. So far only
      detection and counting are proven; that the app actually says it out
      loud is unverified (recognition had not started yet when the service
      came back during the test).
- [x] ~~Establish whether the "keeps shutting itself down" observation matches
      the false announcement or a genuine kill.~~ **Resolved 2026-09-09: a
      genuine kill.** The app says nothing and simply goes silent, the
      notification disappears. The device is a **Xiaomi Redmi 13C** and
      battery optimisation was already disabled – so it is MIUI's own process
      management, not the standard Android mechanism.
- [x] ~~Add a Xiaomi/MIUI note.~~ In
      [docs/xiaomi-einstellungen.md](xiaomi-einstellungen.md) since
      2026-09-09, **read off an actual device**, not guessed. The decisive
      setting is called "Hintergrund-Autostart" (background autostart) and is
      off for almost every app by default.
- [ ] **Bring the Xiaomi note into the app.** A file only helps those who
      read it – this audience does not read GitHub. A short version under
      "Info & settings", sensibly shown only on Xiaomi devices
      (`Build.MANUFACTURER`) so it does not confuse anyone else.
- [ ] **Check whether the widget can be placed at all on Xiaomi.** On the
      device inspected, "Startbildschirmverknüpfungen" (home screen
      shortcuts) is set to red. Whether that also covers widgets is unclear –
      verify on a device.
- [ ] Add other vendors (Samsung, Huawei, Oppo, OnePlus) once someone with
      such a device shows their settings. Same method: read off, not guessed.
- [ ] Investigate whether the app can detect the vendor restriction itself
      rather than warning in general terms. If the counter under "Info &
      settings" climbs although battery optimisation is disabled, that is
      exactly this case – a targeted hint could be derived from it.

- [ ] **Ask whether a Nadine exists in the address book.** A tester reported
      that "Nadine anrufen" suggested Martins. The code does not explain
      this: Nadine scores 0.97, Martin 0.32, and the phonetic codes differ
      (626 vs 6726). Most likely Vosk already heard something else. Without
      a log this is guesswork.
- [ ] **Recognition of first names with a Swiss accent.** The same tester
      speaks Swiss German and had trouble with first names even in standard
      German. Determine whether this is an accent or a model problem.
- [ ] **State clearly in the store listing that Swiss German is not
      understood.** The Vosk model is trained on standard German. Better said
      up front than discovered later.
- [ ] Check whether the four speed steps are the right ones – 1.6 may still
      be too slow for practised screen-reader users.

### Technical

- [ ] **This file lags behind by about 400 lines.** Noticed on 2026-10-07:
      the English version has 575 lines, the German one 974. It is missing
      every entry from 2026-10-01 onwards and the entire section on
      recruiting testers through the self-help associations.

      The entries from 2026-10-07 and a note about the backlog have been
      added; the rest has not. This deserves a session of its own, because
      it is not only about translating: the order of items should match in
      both files, otherwise nothing can be found again.

      The German version is authoritative. Whoever works on the backlog
      should go through both files side by side rather than translating
      section by section – translating section by section is how the
      backlog arose in the first place.

- [ ] **Align native libraries to 16 KB memory pages.** The Play Console
      flags this under "For your next release" as *Action required*: on
      devices with a 16 KB memory page size the app may crash, or fail to
      install at all. For this audience a silent crash on startup would be
      the worst failure mode.

      **Measured on 2026-09-21 rather than guessed** (`readelf -lW` on the
      `.so` files in the bundle, architecture arm64-v8a):

      - `libjnidispatch.so` from jna 5.13.0: `LOAD align 0x10000` = 64 KB.
        **Already fine.** The JNA problem previously assumed here does not
        exist, and no jna upgrade is needed for this.
      - `libvosk.so` from vosk-android 0.3.47: `LOAD align 0x1000` = 4 KB.
        **The sole culprit.**
      - Downloaded and measured for comparison: vosk-android 0.3.75 ships
        `0x4000` = 16 KB. **The fix is one line in
        `app/build.gradle.kts`.**

      Also corrected on 2026-09-21: this entry used to say the work had to
      wait "deliberately only after the 14 days" because a mid-test release
      would disturb the counting. That is not true. Google counts how long
      enough opted-in testers have had the app installed, not how long a
      particular version has been sitting there. The real reason to wait is
      a different one – see the next item.
- [ ] Upgrade `vosk-android` from 0.3.47 to 0.3.75. Fixes the 16 KB issue
      (see above), but is **not a one-liner to take along in passing**: it
      spans 28 versions of the speech recogniser itself. Two things need
      checking on the device afterwards, not assuming:

      1. Whether the bundled German model still fits unchanged.
      2. Whether recognition quality shifts. The wake phrase thresholds
         measured on 2026-09-21 (`CommandParser.WAKE_MIN_RATIO`, regression
         cases in `CommandParserTest`) come from **0.3.47** output. With a
         new recogniser that measurement is void and has to be repeated.

      This becomes urgent once production access is requested; for the
      closed test it stays a warning.
- [ ] Check whether a grammar-restricted recogniser in the idle state saves
      battery without losing speech when switching (see the reasoning in
      the README).
- [ ] Sign the release build and back up the signing key.
- [ ] Decide whether the app should go to the Play Store. If so:
      `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` and `CALL_PHONE` need a
      justification, and the Play Store requires a core-functionality
      declaration for `CALL_PHONE`.
- [ ] Check the behaviour when a call comes in during the dialogue.

### Interplay with DialOS

- [ ] Decide whether phone and laptop should share the same contacts, and
      how (Nextcloud contacts over CardDAV?).
- [ ] Unify the command language between the DialOS desktop and DialOS
      Mobile – the desktop uses hassil, here the parsing is hand-written.
      Eventually both should understand the same sentences.
- [ ] Link to DialOS Mobile from the DialOS repository (README + `docs/`).

## ✅ Done

- [x] **Dictating phone numbers became usable** (0.6.4) – 2026-09-06. The app
      read back the whole number after every block of digits and was deaf
      while doing so; anyone dictating on lost exactly those digits. Plus: 45
      instead of 15 seconds while dictating, no more silently discarded
      number on timeout, instructions where they are needed, and "letzte
      Ziffer löschen" instead of all-or-nothing. Details in the
      [changelog](README.en.md#changelog).

- [x] **Five findings from the closed test fixed** (0.6.3) – 2026-09-05.
      "Ja bitte" and "nein danke" are understood, similar-sounding names no
      longer crowd out the intended one, number types ("privat", "mobil",
      "Arbeit") work both in the command and as an answer, the dead end after
      "Das habe ich nicht verstanden" is gone, and the timeout announcement
      no longer claims the app is shutting down. Details in the
      [changelog](README.en.md#changelog).

- [x] Speech rate and voice configurable – 2026-09-05. Four speed steps and
      whichever German voices the device offers, as step-forward buttons
      rather than sliders, with a sample played after each tap.

- [x] Licence chosen: Apache 2.0, with a NOTICE file for Vosk, the speech
      model, JNA and AndroidX – 2026-08-19

- [x] **First complete call by voice on real hardware** (Motorola edge 50
      neo, Android 16) – 2026-08-19. Wake phrase, name matching („carola
      stören" → Carola Stern), confirmation, SIM choice and call setup. Two
      defects fixed for it, see changelog 0.6.1.
- [x] Choosing the SIM by voice on dual SIM/eSIM, version and GitHub link
      in the settings, larger logo – 2026-08-19
- [x] **Text messages by voice** (SMS) – 2026-08-19. WhatsApp deliberately
      ruled out: no send API, verified on the device.
- [x] Contrast toggle (black/yellow) and volume toggle (50 %/100 %) in the
      top bar, verified on the device – 2026-08-19
- [x] Removed the „Jetzt sprechen“ button (redundant with the main switch
      and the tile) – 2026-08-19
- [x] **First run on real hardware** (Motorola edge 50 neo, Android 16) –
      2026-08-19. Stephan walked through the flow out loud and it worked.
      Evidence in the log: Vosk model unpacked (91 MB in the external app
      folder), `Background started FGS: Allowed` for the microphone
      service, eight dialogue state changes, clean shutdown.
- [x] Removed Material You colours (`DynamicColors`) again – they replaced
      the DialOS blue with olive tones derived from the wallpaper and left
      contrast to chance – 2026-08-19
- [x] Project set up (Kotlin, Gradle 8.14.3, AGP 8.13, minSdk 26,
      targetSdk 36) – 2026-08-19
- [x] Vosk integrated offline, German model fetched at build time and kept
      out of the repository – 2026-08-19
- [x] Wake phrase „Sprachsteuerung starten“ – 2026-08-19
- [x] Contact matching with Cologne phonetics, Levenshtein and token
      comparison, including a disambiguation prompt – 2026-08-19
- [x] German numerals → digits, dictating a number – 2026-08-19
- [x] Confirmation before dialling, can be switched off – 2026-08-19
- [x] Dialling via `TelecomManager.placeCall` instead of `ACTION_CALL` – 2026-08-19
- [x] Quick settings tile, launcher shortcut and assistant intent as extra
      entry points – 2026-08-19
- [x] Accessible UI (large buttons, live region) – 2026-08-19
- [x] Logo and colours taken from DialOS – 2026-08-19
- [x] 22 unit tests for name matching, numerals and command parsing, all
      green – 2026-08-19
- [x] Debug APK built (63 MB), lint reports no errors – 2026-08-19
