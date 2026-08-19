package org.dialos.mobil

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat

/** Eine Rufnummer eines Kontakts. */
data class PhoneEntry(
    val contactId: Long,
    val name: String,
    val number: String,
    val typeLabel: String,
    val isPrimary: Boolean
)

/** Ein Kontakt mit allen seinen Rufnummern und der Trefferwahrscheinlichkeit. */
data class ContactMatch(
    val name: String,
    val score: Double,
    val entries: List<PhoneEntry>
)

/**
 * Liest das Adressbuch und findet Kontakte zu einem gesprochenen Namen.
 * Die Kontakte werden im Speicher gehalten, damit die Suche während des
 * Dialogs ohne Datenbankzugriff auskommt.
 */
class ContactRepository(private val context: Context) {

    @Volatile
    private var entries: List<PhoneEntry> = emptyList()

    val isEmpty: Boolean get() = entries.isEmpty()

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    /** Lädt das Adressbuch neu. Läuft synchron - nicht im Hauptthread aufrufen. */
    fun reload() {
        if (!hasPermission()) {
            entries = emptyList()
            return
        }
        val result = mutableListOf<PhoneEntry>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY
        )
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(projection[0])
                val nameIdx = cursor.getColumnIndexOrThrow(projection[1])
                val numberIdx = cursor.getColumnIndexOrThrow(projection[2])
                val typeIdx = cursor.getColumnIndexOrThrow(projection[3])
                val labelIdx = cursor.getColumnIndexOrThrow(projection[4])
                val primaryIdx = cursor.getColumnIndexOrThrow(projection[5])

                val seen = HashSet<String>()
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIdx)?.trim().orEmpty()
                    val number = cursor.getString(numberIdx)?.trim().orEmpty()
                    if (name.isEmpty() || number.isEmpty()) continue

                    val id = cursor.getLong(idIdx)
                    // Dieselbe Nummer taucht bei verknüpften Konten mehrfach auf.
                    val key = id.toString() + "|" + number.filter { it.isDigit() || it == '+' }
                    if (!seen.add(key)) continue

                    result += PhoneEntry(
                        contactId = id,
                        name = name,
                        number = number,
                        typeLabel = typeLabel(cursor.getInt(typeIdx), cursor.getString(labelIdx)),
                        isPrimary = cursor.getInt(primaryIdx) != 0
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Kein Zugriff auf die Kontakte", e)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Kontakte konnten nicht gelesen werden", e)
        }
        entries = result
        Log.i(TAG, "${result.size} Rufnummern geladen")
    }

    /**
     * Sucht Kontakte zu einem gesprochenen Namen, beste Übereinstimmung zuerst.
     * Liefert höchstens [limit] Vorschläge oberhalb der Trefferschwelle.
     */
    fun find(spokenName: String, limit: Int = 3): List<ContactMatch> {
        val snapshot = entries
        if (snapshot.isEmpty() || spokenName.isBlank()) return emptyList()

        return snapshot.groupBy { it.name }
            .map { (name, phones) ->
                ContactMatch(
                    name = name,
                    score = NameMatcher.score(spokenName, name),
                    entries = phones.sortedWith(
                        compareByDescending<PhoneEntry> { it.isPrimary }
                            .thenBy { preferenceRank(it.typeLabel) }
                    )
                )
            }
            .filter { it.score >= NameMatcher.THRESHOLD }
            .sortedByDescending { it.score }
            .take(limit)
    }

    /** Mobilnummern zuerst vorschlagen - die erreichen den Angerufenen am ehesten. */
    private fun preferenceRank(label: String): Int = when (label) {
        context.getString(R.string.phone_type_mobile) -> 0
        context.getString(R.string.phone_type_home) -> 1
        context.getString(R.string.phone_type_work) -> 2
        else -> 3
    }

    private fun typeLabel(type: Int, customLabel: String?): String = when (type) {
        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ->
            context.getString(R.string.phone_type_mobile)

        ContactsContract.CommonDataKinds.Phone.TYPE_HOME ->
            context.getString(R.string.phone_type_home)

        ContactsContract.CommonDataKinds.Phone.TYPE_WORK,
        ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN ->
            context.getString(R.string.phone_type_work)

        ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM ->
            customLabel?.takeIf { it.isNotBlank() } ?: context.getString(R.string.phone_type_other)

        else -> context.getString(R.string.phone_type_other)
    }

    private companion object {
        const val TAG = "ContactRepository"
    }
}
