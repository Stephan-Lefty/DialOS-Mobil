[Deutsch](TODO.md) | [English](TODO.en.md)

# TODO – DialOS Mobile

## Open

### Still to be verified with a voice

- [ ] **Actually send an SMS** – the whole path is built but has never run.
      Best to a message to oneself; the log then shows whether the network
      acknowledgement arrives.
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
- [ ] Make the speech rate configurable.

### Technical

- [ ] Upgrade `vosk-android` to 0.3.75 and `jna` to 5.19.1 – deliberately
      not done yet, because 0.3.47 is proven with the model in use and
      there is no device here to verify against.
- [ ] Check whether a grammar-restricted recogniser in the idle state saves
      battery without losing speech when switching (see the reasoning in
      the README).
- [ ] Sign the release build and back up the signing key.
- [ ] Choose a licence (DialOS itself has none yet) and add `LICENSE`.
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
