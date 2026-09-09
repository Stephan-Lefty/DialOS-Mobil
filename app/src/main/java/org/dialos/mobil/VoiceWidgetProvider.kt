package org.dialos.mobil

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Das bildschirmbreite Widget für den Startbildschirm.
 *
 * Warum so groß: Die App richtet sich an Menschen, die den Bildschirm weder
 * gut sehen noch genau treffen können. Ein App-Symbol zwischen zwanzig
 * anderen ist für sie kein brauchbares Ziel - ein Balken über die volle
 * Breite schon. Er zeigt zugleich, ob die Sprachsteuerung läuft; das war
 * bisher nur in der Benachrichtigungsleiste zu sehen.
 *
 * Ein Tippen schaltet ein und startet sofort den Dialog ("Wen möchten Sie
 * anrufen?"), statt nur die App zu öffnen. Wer das Widget drückt, hat das
 * Telefon in der Hand und will jetzt telefonieren - dieselbe Überlegung wie
 * beim großen Knopf auf der Startseite.
 */
class VoiceWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    companion object {

        /**
         * Zeichnet alle vorhandenen Widgets neu.
         *
         * Wird vom [VoiceService] bei jedem Zustandswechsel gerufen. Ohne das
         * bliebe im Widget "ausgeschaltet" stehen, während die App längst
         * zuhört - und eine Anzeige, die etwas anderes behauptet als der
         * Zustand, ist schlimmer als gar keine.
         */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(
                ComponentName(context, VoiceWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return
            val views = buildViews(context)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun buildViews(context: Context): RemoteViews {
            val state = VoiceService.status.value
            val laeuft = state.status != ServiceStatus.OFF

            val views = RemoteViews(context.packageName, R.layout.widget_voice)
            views.setTextViewText(
                R.id.widgetTitle,
                context.getString(if (laeuft) R.string.widget_speak else R.string.widget_start)
            )
            views.setTextViewText(R.id.widgetStatus, statusText(context, state))
            views.setInt(
                R.id.widgetRoot, "setBackgroundResource",
                if (laeuft) R.drawable.widget_background_active
                else R.drawable.widget_background
            )
            // Für TalkBack: Der Balken ist eine Schaltfläche, kein Text.
            views.setContentDescription(
                R.id.widgetRoot,
                context.getString(
                    if (laeuft) R.string.widget_speak_desc else R.string.widget_start_desc
                )
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, tapIntent(context))
            return views
        }

        private fun statusText(context: Context, state: ServiceState): String =
            when (state.status) {
                ServiceStatus.OFF -> context.getString(R.string.status_off)
                ServiceStatus.PREPARING -> context.getString(R.string.status_loading)
                // Im Lauschzustand steht hier der zweite Weg, nicht der
                // Zustand: Wer das Widget sieht, weiß am Knopftext schon,
                // dass es läuft - was er wissen muss, ist, dass es auch ohne
                // Tippen geht.
                ServiceStatus.LISTENING ->
                    context.getString(R.string.widget_status_ready)

                ServiceStatus.ERROR ->
                    context.getString(R.string.status_error, state.detail.orEmpty())

                else -> state.detail ?: context.getString(R.string.status_active)
            }

        /**
         * Der Weg führt über [MainActivity], nicht direkt in den Dienst:
         * Ab Android 12 darf ein Mikrofon-Vordergrunddienst nicht aus dem
         * Hintergrund gestartet werden, und fehlende Berechtigungen kann
         * ohnehin nur eine Activity erfragen.
         */
        private fun tapIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_ACTIVATE
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
