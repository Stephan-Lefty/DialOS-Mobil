package org.dialos.mobil

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

/** Eine eingelegte oder eingebuchte Karte (SIM oder eSIM). */
data class SimCard(
    val subscriptionId: Int,
    val slotIndex: Int,
    /** Was angesagt wird, z. B. "1&1" oder "YELLLOW". */
    val label: String
)

/**
 * Findet die aktiven Karten des Telefons.
 *
 * Bei zwei Karten muss der Nutzer wählen können, über welche telefoniert
 * oder geschrieben wird - sonst entscheidet die Android-Voreinstellung
 * stillschweigend, und das kann die falsche (teure, im Ausland roamende)
 * Karte sein.
 */
class SimRepository(private val context: Context) {

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * Aktive Karten, nach Steckplatz sortiert. Leere Liste, wenn die
     * Berechtigung fehlt oder das Gerät nur eine Karte hat - dann wird
     * bewusst nicht gefragt.
     */
    @SuppressLint("MissingPermission")
    fun activeSims(): List<SimCard> {
        if (!hasPermission()) return emptyList()
        val manager = context.getSystemService<SubscriptionManager>() ?: return emptyList()
        return try {
            manager.activeSubscriptionInfoList.orEmpty()
                .sortedBy { it.simSlotIndex }
                .map { info ->
                    // displayName ist der Name, den der Nutzer in den
                    // Android-Einstellungen sieht ("1&1"). carrierName trägt
                    // im Roaming den fremden Netzbetreiber mit ("3 AT – 1&1")
                    // und ist damit als Ansage unbrauchbar - deshalb erst
                    // displayName, carrierName nur als Rückfallebene.
                    val name = speakable(info.displayName)
                        .ifEmpty { speakable(info.carrierName) }
                        .ifEmpty { context.getString(R.string.sim_fallback, info.simSlotIndex + 1) }
                    SimCard(
                        subscriptionId = info.subscriptionId,
                        slotIndex = info.simSlotIndex,
                        label = name
                    )
                }
        } catch (e: SecurityException) {
            Log.w(TAG, "Keine Berechtigung, die Karten zu lesen", e)
            emptyList()
        }
    }

    /**
     * Der Telefonie-Zugang zu einer Karte. Wird als Extra an
     * `TelecomManager.placeCall` gehängt, damit der Anruf über die gewählte
     * Karte geht statt über die Voreinstellung.
     */
    @SuppressLint("MissingPermission")
    fun phoneAccountFor(subscriptionId: Int): PhoneAccountHandle? {
        if (!hasPermission()) return null
        val telecom = context.getSystemService<TelecomManager>() ?: return null
        return try {
            val accounts = telecom.callCapablePhoneAccounts
            // Auf den allermeisten Geräten ist die Kennung des Zugangs die
            // Subscription-ID als Text. Wo nicht, hilft nur der Vergleich
            // über die Beschriftung.
            accounts.firstOrNull { it.id == subscriptionId.toString() }
                ?: accounts.firstOrNull { handle ->
                    val label = telecom.getPhoneAccount(handle)?.label?.toString()?.trim()
                    label != null && label.equals(labelFor(subscriptionId), ignoreCase = true)
                }
        } catch (e: SecurityException) {
            Log.w(TAG, "Keine Berechtigung für die Telefonie-Zugänge", e)
            null
        }
    }

    /**
     * Macht einen Kartennamen vorlesbar: Gedankenstriche werden zu Pausen,
     * mehrfache Leerzeichen verschwinden. Ohne das liest die Sprachausgabe
     * "3 AT – 1&1" als einen Wortbrei vor.
     */
    private fun speakable(raw: CharSequence?): String =
        raw?.toString().orEmpty()
            .replace('–', ',')
            .replace('—', ',')
            .replace(" - ", ", ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .trim(',')
            .trim()

    private fun labelFor(subscriptionId: Int): String? =
        activeSims().firstOrNull { it.subscriptionId == subscriptionId }?.label

    private companion object {
        const val TAG = "SimRepository"
    }
}
