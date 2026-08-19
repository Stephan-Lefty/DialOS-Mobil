package org.dialos.mobil

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Schaltet die Sprachsteuerung nach einem Neustart wieder ein - aber nur,
 * wenn sie vorher lief und der Nutzer den Autostart nicht abgeschaltet hat.
 *
 * Hinweis: Ab Android 14 darf ein Dienst mit Mikrofon-Zugriff nicht aus dem
 * Hintergrund gestartet werden. Der Dienst fängt das ab und legt stattdessen
 * eine antippbare Benachrichtigung an (siehe [VoiceService]).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> Unit
            else -> return
        }
        val prefs = Prefs(context)
        if (!prefs.autostart || !prefs.wasRunning) return

        Log.i(TAG, "Starte Sprachsteuerung nach ${intent.action}")
        VoiceService.start(context)
    }

    private companion object {
        const val TAG = "BootReceiver"
    }
}
