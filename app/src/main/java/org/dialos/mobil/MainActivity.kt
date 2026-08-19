package org.dialos.mobil

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.dialos.mobil.databinding.ActivityMainBinding

/**
 * Der Startbildschirm ist bewusst karg: Lautstärke, Kontrast, der Zustand
 * und ein sehr großer Knopf. Alles zur Einrichtung liegt hinter
 * "Infos & Einstellungen". Bedient wird die App im Alltag per Sprache.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Prefs
    private lateinit var volume: VolumeController

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        // Direkt weitermachen, wenn der Nutzer gerade alles erlaubt hat -
        // sonst müsste er den großen Knopf ein zweites Mal drücken.
        if (granted.values.all { it }) VoiceService.activate(this)
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
                // Bewusst activate() statt start(): wer den Knopf drückt, hat
                // das Telefon in der Hand und will jetzt etwas - ihn danach
                // noch das Aktivierungswort sagen zu lassen, wäre doppelt.
                // Für später bleibt es wichtig (Telefon in der Tasche,
                // Bildschirm aus), nur eben nicht in dieser Sekunde.
                VoiceService.activate(this)
            }
        }

        binding.btnVolume.setOnClickListener { toggleVolume() }
        binding.btnContrast.setOnClickListener { toggleContrast() }
        binding.btnInfo.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

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

    /** Fordert fehlende Berechtigungen an. true = alles vorhanden. */
    private fun ensurePermissions(): Boolean {
        val missing = Permissions.missing(this)
        if (missing.isEmpty()) return true
        requestPermissions.launch(missing.toTypedArray())
        return false
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

    companion object {
        const val ACTION_ACTIVATE = "org.dialos.mobil.ACTIVATE"
    }
}
