[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – DialOS Mobile

## Open

### Still to be verified with a voice

- [ ] **SIM choice in the dialogue** – card detection is confirmed on the
      device („1&1“ and „YELLLOW“), the spoken question itself is not. Check
      that both „Eins“ and the carrier name work and that the call goes out
      over the right card.
- [ ] **Measure the wake phrase.** During testing the recogniser once logged
      „sprach steigt“ – probably a misheard „Sprachsteuerung“ that
      `CommandParser.isWakePhrase` would reject. Say it repeatedly, evaluate
      the log, tune the thresholds against real data rather than a guess.

### Verify on real hardware

- [ ] Measure wake phrase accuracy: how often does „Sprachsteuerung starten“
      actually trigger, and how often does ordinary conversation trigger it
      by mistake? Adjust the thresholds in `CommandParser` if needed.
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
- [ ] A short beep before listening (like `dialos-start-ansage.py` in
      DialOS – a missing start signal was a real bug there).

### From the closed test (since 2026-09-05)

- [ ] **Check the widget on a device** – built, but never yet seen on a home
      screen. To verify: does it really span the full width (including on
      narrow devices)? Does the colour switch reliably? Does a tap start the
      dialogue, or merely open the app? And does TalkBack read the
      description instead of just saying "widget"?
- [ ] **Verify interruption detection on a device.** The logic is covered by
      tests, the path to it is not. The case can be forced with
      `adb shell am force-stop org.dialos.mobil` – the announcement must then
      come on the next start and the counter under "Info & settings" must go
      up by one. Note: `force-stop` is not the same as a kill by battery
      optimisation, but it does exercise the detection path.
- [ ] Establish whether Michaela's observation ("keeps shutting itself down")
      matches the false announcement from 0.6.2 or a genuine kill. Her answer
      to the follow-up of 07.09. decides this.

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

- [ ] Upgrade `vosk-android` to 0.3.75 and `jna` to 5.19.1 – deliberately
      not done yet, because 0.3.47 is proven with the model in use and
      there is no device here to verify against.
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
