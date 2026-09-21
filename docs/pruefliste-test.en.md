[Deutsch](pruefliste-test.md) | [English](pruefliste-test.en.md)

# Checklist for the closed test

For the testers of **DialOS Mobile**, version 0.6.13 (2026-09-21).

This list is meant to be read aloud – the points stand on their own and are
numbered, with no tables and no images, so a screen reader can work through
them in one pass.

**First things first:** you do not have to work through all of it. A single
reply about a single point already helps. And points 1 to 3 matter most – if
you only have time for three, make it those.

What helps us most: **not just whether something works, but what you heard.**
If the app says something wrong, the exact wording is worth gold.

## The important part

### 1. Does the app listen without you touching the screen?

Say this without picking up the phone, from about a metre away:

```
Sprachsteuerung starten
```

The app should answer: "Wen möchten Sie anrufen?"

Please try this **several times across the day**, with the phone in your
pocket and on the table. And tell us **how many times out of how many
attempts** it worked. That number is the single most valuable piece of
feedback from the whole test.

### 2. After restarting the phone

Restart the phone once. A notification should follow that **you can hear** –
with sound, not silently in the shade.

Up to the previous version the app was deaf after every restart without
saying so. It displayed "DialOS Mobil hört zu" and heard nothing. That is
exactly what should no longer happen.

Please report: did the notification arrive? Was it audible? And did voice
control run again after tapping it?

### 3. When the phone is muted

Set the phone to silent or mute it, then say "Sprachsteuerung starten".

The app should raise the volume by itself far enough for you to hear the
answer. If you had turned it up beforehand, that setting should stay.

## If you feel like it

### 4. Starting without the wake phrase

Say to Google: "Hey Google, öffne DialOS Mobil". If "Beim Öffnen der App
gleich einschalten" is on in the settings, the app asks straight away who
you want to call – without you touching the screen.

### 5. The bar on the home screen

During setup the app offers to place a wide bar on the home screen. One tap
on it is enough, then you can speak.

Please report: could the bar be added? Does your screen reader find it? And
does it say the right thing – "Antippen zum Einschalten" when voice control
is off, and "Antippen und sprechen" when it is running?

### 6. A contact with several numbers

If you have someone with both a private **and** a mobile number:

```
Michaela privat anrufen
```

Or say the name first and answer the follow-up question with "privat",
"mobil" or "Arbeit".

### 7. Names that sound alike

Call someone whose name appears more than once in your address book, or in a
similar form. Does the app suggest the right person? Does it read out **all**
the matches, or does it stop too early?

### 8. Dictating a number

Say "Nummer wählen", speak the digits one at a time, and finish with
"fertig". If you misspeak, "letzte Ziffer löschen" takes back only the last
one, not everything.

Take long pauses if you like – the app is supposed to give you time.

### 9. Cancelling midway

Say "Abbrechen" at some point. The app should still respond afterwards and
not fall silent.

### 10. Voice and speed

Under "Infos & Einstellungen" you can step through the voices and four speed
settings. Each tap plays a sample.

Please report: is there a voice among them that you understand well? And is
the fastest setting fast enough?

## What we already know and cannot fix

So you do not have to go looking:

- **The app does not understand Swiss German.** Speech recognition runs
  offline on the phone and is trained on standard German. No Swiss model of
  this size exists.
- **The app cannot switch airplane mode off.** That has been reserved for
  system apps since Android 4.2. What it does now is tell you that it is on
  and guide you by voice to the place where you can turn it off.
- **Calls over WhatsApp or Signal are not possible.** Neither offers an
  interface for it; this was checked on a real device.
- **Text messages are deliberately absent.** Google grants the SMS
  permission only for a fixed list of use cases, and a voice dialler is not
  on it.

## How to send your feedback

An informal email to Stephan is plenty – one sentence per point is quite
enough, and you are welcome to just write freely instead of following the
numbers.

Particularly valuable:

- **The exact wording** when the app said something wrong or confusing.
- **What you said** when it failed to understand you.
- **Which phone** you use, if something does not work at all. On Xiaomi,
  Redmi and POCO in particular there is a setting called "Autostart in the
  background" that switches apps off without reporting it – see
  [xiaomi-einstellungen.md](xiaomi-einstellungen.md).
