package io.github.dovecoteescapee.byedpi.activities

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode

class ToggleActivity : ComponentActivity() {
    private val vpnRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                ServiceManager.start(this, Mode.VPN)
            }
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val strategy = intent?.getStringExtra("strategy")
        val modeStr = intent?.getStringExtra("mode")
        val prefs = getPreferences()
        var updated = false

        if (strategy != null && strategy != prefs.getString("byedpi_cmd_args", null)) {
            prefs.edit(commit = true) { putString("byedpi_cmd_args", strategy) }
            updated = true
        }

        if (modeStr != null) {
            val newMode = try { Mode.valueOf(modeStr) } catch (e: Exception) { null }
            if (newMode != null && newMode != prefs.mode()) {
                prefs.edit(commit = true) { putString("mode", newMode.name) }
                updated = true
            }
        }

        val onlyUpdate = intent?.getBooleanExtra("only_update", false) ?: false
        val onlyStart = intent?.getBooleanExtra("only_start", false) ?: false
        val onlyStop = intent?.getBooleanExtra("only_stop", false) ?: false

        val (status, _) = appStatus

        when {
            onlyUpdate -> { /* already updated above */ }
            onlyStart -> if (status == AppStatus.Halted) start()
            onlyStop -> if (status == AppStatus.Running) stop()
            else -> {
                if (status == AppStatus.Halted) {
                    start()
                } else {
                    if (updated) {
                        ServiceManager.restart(this, prefs.mode())
                    } else {
                        stop()
                    }
                }
            }
        }

        if (intent?.getBooleanExtra("finish_after", true) != false) {
            val mode = prefs.mode()
            if (status == AppStatus.Halted && !onlyStop && !onlyUpdate && mode == Mode.VPN && VpnService.prepare(this) != null) {
                return
            }
            finish()
        }
    }

    private fun start() {
        val mode = getPreferences().mode()
        if (mode == Mode.VPN) {
            val intentPrepare = VpnService.prepare(this)
            if (intentPrepare != null) {
                vpnRegister.launch(intentPrepare)
            } else {
                ServiceManager.start(this, Mode.VPN)
            }
        } else {
            ServiceManager.start(this, Mode.Proxy)
        }
    }

    private fun stop() {
        ServiceManager.stop(this)
    }
}
