package org.dialos.mobil

import android.Manifest
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.format.DateUtils
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import org.dialos.mobil.databinding.ActivitySettingsBinding

/**
 * Einrichtung und Kurzanleitung - alles, was man einmal braucht und nicht
 * täglich. Die Startseite bleibt dadurch auf das Wesentliche beschränkt.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: Prefs

    /**
     * Eigene Sprachausgabe nur für die Hörproben. Der Dienst hat seine eigene
     * und übernimmt geänderte Einstellungen beim nächsten Satz von selbst.
     */
    private var speaker: Speaker? = null
    private var voices: List<android.speech.tts.Voice> = emptyList()

    /** Nach einer Ablehnung wird ein deutlicherer Hinweis eingeblendet. */
    private var hasAskedForPermissions = false

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasAskedForPermissions = true
        updatePermissionUi()
    }

    private val idleHandler = Handler(Looper.getMainLooper())

    /**
     * Wer hier versehentlich landet, findet ohne fremde Hilfe womöglich nicht
     * zurück. Nach kurzer Untätigkeit geht es deshalb von selbst zur
     * Startseite - dort ist der große Knopf, auf den es ankommt.
     */
    private val returnToStart = Runnable { finish() }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs(this)
        setTheme(
            if (prefs.highContrast) R.style.Theme_DialOsMobil_HighContrast
            else R.style.Theme_DialOsMobil
        )
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnBackBottom.setOnClickListener { finish() }

        binding.btnPermissions.setOnClickListener {
            val missing = Permissions.missing(this)
            if (missing.isEmpty()) openAppSettings() else requestPermissions.launch(missing.toTypedArray())
        }

        binding.btnBattery.setOnClickListener { requestIgnoreBatteryOptimizations() }

        binding.switchHotword.isChecked = prefs.hotwordEnabled
        binding.switchHotword.setOnCheckedChangeListener { _, checked -> prefs.hotwordEnabled = checked }

        binding.switchConfirm.isChecked = prefs.confirmBeforeCall
        binding.switchConfirm.setOnCheckedChangeListener { _, checked -> prefs.confirmBeforeCall = checked }

        binding.switchAutostart.isChecked = prefs.autostart
        binding.switchAutostart.setOnCheckedChangeListener { _, checked -> prefs.autostart = checked }

        binding.btnWidget.setOnClickListener { widgetAnbieten() }

        setUpVoiceControls()

        binding.versionInfo.text = getString(R.string.version_info, BuildConfig.VERSION_NAME)

        binding.btnRepo.setOnClickListener {
            // Das Repo ist privat - ohne Anmeldung mit Zugriff läuft der
            // Link ins Leere. Er ist als Verweis für Stephan gedacht.
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.repo_url)))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { startActivity(intent) }.onFailure {
                Toast.makeText(this, R.string.repo_unavailable, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionUi()
        updateBatteryUi()
        updateWidgetUi()
        restartIdleTimer()
    }

    override fun onPause() {
        super.onPause()
        // Nicht weiterzählen, während ein Systemdialog (Berechtigungen,
        // Akku-Einstellungen) obenauf liegt.
        idleHandler.removeCallbacks(returnToStart)
        // Eine laufende Hörprobe soll nicht weiterreden, wenn die Seite
        // verlassen wird - sonst spricht sie in den Dienst hinein.
        speaker?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        speaker?.shutdown()
        speaker = null
    }

    /** Jede Berührung - auch Scrollen - verlängert die Verweildauer. */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        restartIdleTimer()
        return super.dispatchTouchEvent(event)
    }

    private fun restartIdleTimer() {
        idleHandler.removeCallbacks(returnToStart)
        idleHandler.postDelayed(returnToStart, IDLE_TIMEOUT_MS)
    }

    // -----------------------------------------------------------------------
    // Stimme und Sprechtempo
    // -----------------------------------------------------------------------

    private fun setUpVoiceControls() {
        updateRateButton()
        binding.btnVoice.setText(R.string.voice_button_default)
        binding.btnVoice.isEnabled = false

        binding.btnSpeechRate.setOnClickListener {
            val rates = Prefs.SPEECH_RATES
            val next = (rates.indexOf(prefs.speechRate).takeIf { it >= 0 } ?: 1) + 1
            prefs.speechRate = rates[next % rates.size]
            updateRateButton()
            playSample()
        }

        binding.btnVoice.setOnClickListener {
            if (voices.isEmpty()) return@setOnClickListener
            val current = voices.indexOfFirst { it.name == prefs.voiceName }
            val next = voices[(current + 1) % voices.size]
            prefs.voiceName = next.name
            updateVoiceButton()
            playSample()
        }

        // Die Liste der Stimmen steht erst, wenn die Sprachausgabe bereit ist.
        speaker = Speaker(this) { ready ->
            if (isFinishing || isDestroyed) return@Speaker
            voices = if (ready) speaker?.germanVoices().orEmpty() else emptyList()
            binding.btnVoice.isEnabled = voices.isNotEmpty()
            if (voices.isEmpty()) {
                binding.btnVoice.setText(R.string.voice_none)
            } else {
                updateVoiceButton()
            }
        }
    }

    private fun updateRateButton() {
        val label = when (prefs.speechRate) {
            Prefs.SPEECH_RATES[0] -> R.string.voice_rate_slow
            Prefs.SPEECH_RATES[2] -> R.string.voice_rate_fast
            Prefs.SPEECH_RATES[3] -> R.string.voice_rate_faster
            else -> R.string.voice_rate_normal
        }
        binding.btnSpeechRate.text = getString(R.string.voice_rate_button, getString(label))
    }

    private fun updateVoiceButton() {
        val index = voices.indexOfFirst { it.name == prefs.voiceName }
        binding.btnVoice.text = if (index < 0) {
            getString(R.string.voice_button_default)
        } else {
            getString(R.string.voice_button, index + 1, voices.size)
        }
    }

    /**
     * Ohne Hörprobe wäre die Einstellung für die Zielgruppe wertlos - wer
     * nichts sieht, kann eine Stimme nur beurteilen, indem er sie hört.
     */
    private fun playSample() {
        restartIdleTimer()
        speaker?.speak(getString(R.string.voice_sample))
    }

    private fun updatePermissionUi() {
        val missing = Permissions.missing(this)
        binding.permStatus.setText(
            when {
                missing.isEmpty() -> R.string.perm_all_granted
                hasAskedForPermissions -> R.string.perm_denied_hint
                else -> R.string.perm_needed
            }
        )
        binding.btnPermissions.setText(
            if (missing.isEmpty()) R.string.perm_open_settings else R.string.perm_grant
        )
    }

    private fun openAppSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        )
    }

    // -----------------------------------------------------------------------
    // Widget auf den Startbildschirm
    // -----------------------------------------------------------------------

    /**
     * Bietet an, den Balken auf den Startbildschirm zu legen.
     *
     * Der übliche Weg dorthin ist für diese Zielgruppe praktisch
     * unbenutzbar: lange auf eine freie Fläche drücken, in einer Liste
     * blättern, den richtigen Eintrag finden, ziehen und an der richtigen
     * Stelle loslassen. Wer nichts sieht oder die Hände nicht ruhig führt,
     * scheitert daran - und hätte damit ausgerechnet die Bedienhilfe nicht,
     * die für ihn gebaut wurde.
     *
     * [AppWidgetManager.requestPinAppWidget] übernimmt das: Der Launcher
     * zeigt einen einfachen Bestätigungsdialog, kein Ziehen, kein Suchen.
     */
    private fun widgetAnbieten() {
        val manager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, VoiceWidgetProvider::class.java)
        val angefordert = runCatching {
            manager.requestPinAppWidget(provider, null, null)
        }.getOrDefault(false)

        if (!angefordert) {
            // Manche Launcher können das nicht. Dann bleibt nur der Hinweis,
            // es von Hand zu tun - besser als ein Knopf, der schweigend
            // nichts bewirkt.
            Toast.makeText(this, R.string.widget_add_manual, Toast.LENGTH_LONG).show()
        }
        restartIdleTimer()
    }

    private fun updateWidgetUi() {
        val manager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, VoiceWidgetProvider::class.java)
        val schonDa = runCatching {
            manager.getAppWidgetIds(provider).isNotEmpty()
        }.getOrDefault(false)
        val moeglich = runCatching {
            manager.isRequestPinAppWidgetSupported
        }.getOrDefault(false)

        // Liegt der Balken schon, wäre der Knopf nur Ballast. Kann der
        // Launcher es nicht, wäre er eine Enttäuschung.
        binding.btnWidget.isVisible = moeglich && !schonDa
        binding.widgetHint.isVisible = moeglich && !schonDa
    }

    private fun updateBatteryUi() {
        val exempt = getSystemService<PowerManager>()
            ?.isIgnoringBatteryOptimizations(packageName) ?: false
        binding.batteryStatus.setText(if (exempt) R.string.battery_ok else R.string.battery_body)
        binding.btnBattery.isEnabled = !exempt
        updateInterruptionUi()
    }

    /**
     * Zeigt, wie oft Android die Sprachsteuerung abgeräumt hat.
     *
     * Aus dem geschlossenen Test: Eine Testperson meldete, die App beende
     * sich immer wieder selbst. Ob das stimmt, war ohne Kabel nicht zu
     * klären - ein vom System beendeter Dienst ist kein Absturz und taucht
     * deshalb auch in den Play-Console-Statistiken nicht auf.
     */
    private fun updateInterruptionUi() {
        val anzahl = prefs.interruptions
        binding.interruptionStatus.text = if (anzahl == 0) {
            getString(R.string.interruptions_none)
        } else {
            val zeitpunkt = DateUtils.formatDateTime(
                this,
                prefs.lastInterruptionAt,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
            )
            getString(R.string.interruptions_some, anzahl, zeitpunkt)
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.fromParts("package", packageName, null)
        )
        runCatching { startActivity(intent) }
            .onFailure { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
    }

    private companion object {
        /** So lange darf die Seite unberührt offen bleiben. */
        const val IDLE_TIMEOUT_MS = 10_000L
    }
}

/** Die Berechtigungen, ohne die nichts geht - an einer Stelle. */
object Permissions {

    fun required(): List<String> = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.READ_CONTACTS)
        add(Manifest.permission.CALL_PHONE)
        add(Manifest.permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun missing(activity: AppCompatActivity): List<String> = required().filter {
        ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
    }
}
