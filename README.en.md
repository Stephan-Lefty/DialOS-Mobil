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

## What a call sounds like

```
User: „Sprachsteuerung starten“          (start voice control)
App:  „Sprachsteuerung bereit. Wen möchten Sie anrufen?“
User: „Max Mustermann anrufen“
App:  „Max Mustermann, Mobil, anrufen?“
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
App:  „Nummer 0 1 7 9 … anrufen?“
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
- **Dictate and send text messages** without touching the screen. The text
  is read back in full before sending, and that confirmation **cannot** be
  switched off: an SMS is irreversible and costs money.
- **Four ways to start the dialogue:** wake phrase, a large button in the
  app, a quick settings tile, or the assistant gesture (the app can be set
  as the default digital assistant).
- **Restarts automatically** after the phone reboots.
- **Accessible UI:** very large buttons, high contrast, status shown as a
  live region so TalkBack announces every change.

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
own voice. During a call it pauses as well and checks every two seconds
whether the call has ended.

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
- **Free text is recognised less well than commands.** The small model is
  built for commands; expect errors when dictating a message. That is why
  the text is always read back before sending.
- **SMS only, no WhatsApp or Signal.** Those services offer no way to send
  a message – their intent merely opens the chat with the text prepared,
  and „Send“ would have to be tapped. The only way around it is an
  accessibility service driving WhatsApp's UI, which breaks on every update
  and then fails silently. For an app a blind person relies on, silent
  failure is the worst behaviour there is.
- **This app is not an emergency call feature.** An emergency call should
  never depend on speech recognition.

## Credits

- [Vosk](https://alphacephei.com/vosk/) by Alpha Cephei – offline
  recognition and the German model (Apache License 2.0).
- Logo and colours from the [DialOS](https://github.com/Stephan-Lefty/DialOS) project.

## Changelog

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
