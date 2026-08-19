[Deutsch](PRIVACY.md) | [English](PRIVACY.en.md)

# Privacy Policy – DialOS Mobile

Last updated: 19 August 2026 · effective from version 0.6.0

## In short

**DialOS Mobile sends no data. To anyone.**

The app has no internet permission. It is technically unable to open a
connection – neither to a server of the provider nor to any third party.
There is no advertising, no analytics, no crash reporting, no user
accounts.

## Controller

Stephan Rösner
info@naturlust.net

## What the app processes

Everything listed below is processed **on the device only** and never
leaves it.

### Microphone audio

Speech recognition runs offline using
[Vosk](https://alphacephei.com/vosk/); the German speech model is bundled
with the app. Captured audio is turned into text immediately and then
discarded. **No audio recording is stored**, neither permanently nor
temporarily.

The recognised text is kept in memory only for as long as the current voice
command requires.

### Contacts

The app reads names, phone numbers and their labels (mobile, home, work)
from the phone's address book in order to match a spoken name to the right
number. Contacts are held in memory while the app runs and discarded when
it stops. **No copy of the address book is created.**

### Telephony

The app dials numbers through Android's telephony service. It keeps **no
call log** and does not read the system call log.

### SIM/eSIM information

On devices with more than one card, the app reads the display names of the
active cards so the user can choose by voice which one to call through.
This information is not stored.

### Settings

Only the app's own settings are stored (volume level, contrast view,
autostart, confirmation before calling) in the app's private storage on the
device.

## Permissions and why they are needed

| Permission | Purpose |
|---|---|
| Microphone | Recognise voice commands. Core function of the app. |
| Read contacts | Match a spoken name to a phone number. |
| Make calls | Actually place the call. |
| Read phone state | On dual-SIM devices, determine which cards are available. |
| Modify audio settings | Set the announcements to an audible volume on startup. |
| Notifications | Show that voice control is listening (legally required for continuous operation). |
| Run at startup | Switch voice control back on after a reboot. |
| Ignore battery optimisation | Prevent Android from stopping the service and ending the listening. |

The app requests **no** internet permission.

## Sharing with third parties

Does not occur. There are no recipients, because no data leaves the device.

## Retention and deletion

As nothing is stored, there is no retention period. The stored settings are
removed completely when the app is uninstalled.

## Children

The app is not directed at children and collects no data about them.

## Your rights

Under the GDPR you have the rights of access, rectification, erasure,
restriction, data portability and objection. Since the provider never
receives or processes any data about you, these rights have no substance
towards the provider – there is simply nothing to give information about.
For questions: info@naturlust.net

## Changes

Changes to this policy are recorded in this document and in the
[changelog](README.en.md#changelog).

---

*The published version of this policy is at*
*[https://dialos.org/dialos-mobil-datenschutz/](https://dialos.org/dialos-mobil-datenschutz/) (German) –*
*this file is its source. Changes here belong there too.*
