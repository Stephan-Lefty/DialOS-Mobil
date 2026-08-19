package org.dialos.mobil

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.dialos.mobil.databinding.ActivityMainBinding

/**
 * Die Oberfläche ist bewusst schlicht: große Schaltflächen, viel Kontrast,
 * jede Zustandsänderung wird über eine Live-Region auch von TalkBack
 * vorgelesen. Bedient wird die App im Alltag per Sprache.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Prefs
    private lateinit var volume: VolumeController

    /** Nach einer Ablehnung wird ein deutlicherer Hinweis eingeblendet. */
    private var hasAskedForPermissions = false

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasAskedForPermissions = true
        updatePermissionUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs(this)
        // Das Theme muss vor super.onCreate stehen, sonst greift es erst beim
        // nächsten Start der Activity.
        setTheme(
            if (prefs.highContrast) R.style.Theme_DialOsMobil_HighContrast
            else R.style.Theme_DialOsMobil
        )
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        volume = VolumeController(this)
        // Bei jedem Start auf die Standardlautstärke zurück: das Telefon stand
        // im Test bei 27 %, und wer nichts sieht, merkt eine zu leise Ansage
        // erst, wenn die App scheinbar schweigt. Die 100 %-Stufe ist damit
        // bewusst eine Anhebung für den Moment, keine dauerhafte Einstellung.
        prefs.loudMode = false
        volume.setPercent(VolumeController.NORMAL_PERCENT)
        updateVolumeUi()

        binding.btnToggle.setOnClickListener {
            if (VoiceService.isRunning) {
                VoiceService.stop(this)
            } else if (ensurePermissions()) {
                VoiceService.start(this)
            }
        }

        binding.btnVolume.setOnClickListener { toggleVolume() }
        binding.btnContrast.setOnClickListener { toggleContrast() }

        binding.btnPermissions.setOnClickListener {
            if (missingPermissions().isEmpty()) openAppSettings() else ensurePermissions()
        }

        binding.btnBattery.setOnClickListener { requestIgnoreBatteryOptimizations() }

        binding.switchHotword.isChecked = prefs.hotwordEnabled
        binding.switchHotword.setOnCheckedChangeListener { _, checked -> prefs.hotwordEnabled = checked }

        binding.switchConfirm.isChecked = prefs.confirmBeforeCall
        binding.switchConfirm.setOnCheckedChangeListener { _, checked -> prefs.confirmBeforeCall = checked }

        binding.switchAutostart.isChecked = prefs.autostart
        binding.switchAutostart.setOnCheckedChangeListener { _, checked -> prefs.autostart = checked }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                VoiceService.status.collect { render(it) }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        updatePermissionUi()
        updateBatteryUi()
        updateContrastUi()
    }

    /**
     * Sowohl der Startsymbol-Kurzbefehl als auch der Assistenten-Aufruf
     * ("Assist"-Geste) sollen sofort losreden.
     */
    private fun handleIntent(intent: Intent?) {
        val wantsImmediateStart = intent?.action == ACTION_ACTIVATE ||
            intent?.action == Intent.ACTION_ASSIST
        if (wantsImmediateStart && ensurePermissions()) {
            VoiceService.activate(this)
        }
    }

    private fun render(state: ServiceState) {
        val text = when (state.status) {
            ServiceStatus.OFF -> getString(R.string.status_off)
            ServiceStatus.PREPARING -> getString(R.string.status_loading)
            ServiceStatus.LISTENING -> getString(R.string.status_listening)
            ServiceStatus.ACTIVE, ServiceStatus.CALLING ->
                state.detail ?: getString(R.string.status_active)

            ServiceStatus.ERROR -> getString(R.string.status_error, state.detail.orEmpty())
        }
        binding.status.text = text
        binding.btnToggle.setText(
            if (state.status == ServiceStatus.OFF) R.string.btn_start else R.string.btn_stop
        )
    }

    // -----------------------------------------------------------------------
    // Kopfzeile: Lautstärke und Kontrast
    // -----------------------------------------------------------------------

    private fun toggleVolume() {
        val wantLoud = !prefs.loudMode
        val target =
            if (wantLoud) VolumeController.LOUD_PERCENT else VolumeController.NORMAL_PERCENT
        if (volume.setPercent(target)) {
            prefs.loudMode = wantLoud
        } else {
            Toast.makeText(this, R.string.volume_blocked, Toast.LENGTH_LONG).show()
        }
        updateVolumeUi()
    }

    private fun updateVolumeUi() {
        val loud = prefs.loudMode
        binding.btnVolume.setText(if (loud) R.string.volume_loud else R.string.volume_normal)
        binding.btnVolume.contentDescription =
            getString(if (loud) R.string.volume_loud_desc else R.string.volume_normal_desc)
    }

    private fun toggleContrast() {
        prefs.highContrast = !prefs.highContrast
        // Die Activity neu aufbauen, damit das andere Theme greift. Der
        // Zustand steckt vollständig in den Einstellungen und im Dienst,
        // deshalb geht dabei nichts verloren.
        recreate()
    }

    private fun updateContrastUi() {
        val high = prefs.highContrast
        binding.btnContrast.setText(if (high) R.string.contrast_off else R.string.contrast_on)
        binding.btnContrast.contentDescription =
            getString(if (high) R.string.contrast_off_desc else R.string.contrast_on_desc)
    }

    // -----------------------------------------------------------------------
    // Berechtigungen
    // -----------------------------------------------------------------------

    private fun requiredPermissions(): List<String> = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.READ_CONTACTS)
        add(Manifest.permission.CALL_PHONE)
        add(Manifest.permission.SEND_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun missingPermissions(): List<String> = requiredPermissions().filter {
        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }

    /** Fordert fehlende Berechtigungen an. true = alles vorhanden. */
    private fun ensurePermissions(): Boolean {
        val missing = missingPermissions()
        if (missing.isEmpty()) return true
        requestPermissions.launch(missing.toTypedArray())
        return false
    }

    private fun updatePermissionUi() {
        val missing = missingPermissions()
        binding.permStatus.setText(
            when {
                missing.isEmpty() -> R.string.perm_all_granted
                hasAskedForPermissions -> R.string.perm_denied_hint
                else -> R.string.perm_needed
            }
        )
        binding.btnPermissions.setText(
            if (missing.isEmpty()) R.string.perm_title else R.string.perm_grant
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
    // Akku-Optimierung
    // -----------------------------------------------------------------------

    private fun isIgnoringBatteryOptimizations(): Boolean =
        getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(packageName) ?: false

    private fun updateBatteryUi() {
        val exempt = isIgnoringBatteryOptimizations()
        binding.batteryStatus.setText(if (exempt) R.string.battery_ok else R.string.battery_body)
        binding.btnBattery.isEnabled = !exempt
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

    companion object {
        const val ACTION_ACTIVATE = "org.dialos.mobil.ACTIVATE"
    }
}
