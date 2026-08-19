package org.dialos.mobil

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import java.util.concurrent.atomic.AtomicLong

/**
 * Verschickt Kurznachrichten über [SmsManager].
 *
 * Bewusst SMS und nicht WhatsApp oder Signal: nur SMS lässt sich ohne
 * Umweg über eine fremde Oberfläche absenden. Bei WhatsApp öffnet der
 * Intent lediglich den Chat mit vorbereitetem Text - auf "Senden" müsste
 * getippt werden, und genau das kann die Zielgruppe nicht.
 *
 * Die SIM-Karte kommt aus der Android-Standardeinstellung für SMS. Auf
 * Geräten mit zwei Karten wird also die verschickt, die der Nutzer dort
 * ohnehin gewählt hat.
 */
class SmsSender(private val context: Context) {

    /**
     * Sendet [text] an [number]. [onResult] wird genau einmal aufgerufen,
     * sobald das Mobilfunknetz den Versand quittiert hat - oder mit einer
     * Fehlermeldung, wenn er scheitert.
     */
    @SuppressLint("MissingPermission")
    fun send(number: String, text: String, onResult: (success: Boolean, error: String?) -> Unit) {
        val manager = smsManager()
        if (manager == null) {
            onResult(false, context.getString(R.string.sms_error_no_service))
            return
        }

        val action = ACTION_SENT_PREFIX + counter.incrementAndGet()
        val parts = manager.divideMessage(text)

        // Bei einer langen Nachricht quittiert jedes Teilstück einzeln. Erst
        // wenn alle durch sind, darf die Ansage kommen - sonst meldet die App
        // "gesendet", während noch die Hälfte unterwegs ist.
        var remaining = parts.size
        var failure: String? = null
        var finished = false

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (resultCode != Activity.RESULT_OK && failure == null) {
                    failure = describeError(resultCode)
                }
                remaining--
                if (remaining > 0 || finished) return
                finished = true
                runCatching { context.unregisterReceiver(this) }
                onResult(failure == null, failure)
            }
        }

        val filter = IntentFilter(action)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }

        val sentIntents = ArrayList<PendingIntent>(parts.size)
        for (index in parts.indices) {
            sentIntents += PendingIntent.getBroadcast(
                context,
                index,
                Intent(action).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        try {
            manager.sendMultipartTextMessage(number, null, parts, sentIntents, null)
        } catch (e: Exception) {
            Log.e(TAG, "SMS konnte nicht übergeben werden", e)
            finished = true
            runCatching { context.unregisterReceiver(receiver) }
            onResult(false, e.message ?: context.getString(R.string.sms_error_generic))
        }
    }

    private fun smsManager(): SmsManager? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }.getOrNull()

    private fun describeError(code: Int): String = when (code) {
        SmsManager.RESULT_ERROR_NO_SERVICE -> context.getString(R.string.sms_error_no_service)
        SmsManager.RESULT_ERROR_RADIO_OFF -> context.getString(R.string.sms_error_radio_off)
        SmsManager.RESULT_ERROR_NULL_PDU,
        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> context.getString(R.string.sms_error_generic)

        else -> context.getString(R.string.sms_error_generic)
    }

    private companion object {
        const val TAG = "SmsSender"
        const val ACTION_SENT_PREFIX = "org.dialos.mobil.SMS_SENT."
        val counter = AtomicLong(0)
    }
}
