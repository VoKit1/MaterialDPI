package io.github.dovecoteescapee.byedpi.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import io.github.dovecoteescapee.byedpi.BuildConfig
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.*
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.ui.*
import io.github.dovecoteescapee.byedpi.ui.theme.ByeDpiTheme
import io.github.dovecoteescapee.byedpi.utility.*
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName

        private fun collectLogs(): String? =
            try {
                Runtime.getRuntime()
                    .exec("logcat *:D -d")
                    .inputStream.bufferedReader()
                    .use { it.readText() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to collect logs", e)
                null
            }
    }

    private val vpnRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                ServiceManager.start(this, Mode.VPN)
            } else {
                Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }

    private val logsRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { log ->
            lifecycleScope.launch(Dispatchers.IO) {
                val logs = collectLogs()

                if (logs == null) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.logs_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val uri = log.data?.data ?: run {
                        Log.e(TAG, "No data in result")
                        return@launch
                    }
                    contentResolver.openOutputStream(uri)?.use {
                        try {
                            it.write(logs.toByteArray())
                        } catch (e: IOException) {
                            Log.e(TAG, "Failed to save logs", e)
                        }
                    } ?: run {
                        Log.e(TAG, "Failed to open output stream")
                    }
                }
            }
        }

    private val exportSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val prefs = getPreferences()
            val history = HistoryUtils(this).getHistory()
            val apps = prefs.getSelectedApps()

            val settings = prefs.all.filterKeys { key ->
                key !in setOf("byedpi_command_history", "selected_apps")
            }

            val export = AppSettings(
                app = BuildConfig.APPLICATION_ID,
                version = BuildConfig.VERSION_NAME,
                history = history,
                apps = apps,
                settings = settings
            )

            val json = Gson().toJson(export)

            contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
        }
    }

    private val importSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            contentResolver.openInputStream(it)?.use { inputStream ->
                val json = inputStream.bufferedReader().readText()
                val import = try {
                    Gson().fromJson(json, AppSettings::class.java)
                } catch (e: Exception) {
                    null
                }

                if (import == null || import.app != BuildConfig.APPLICATION_ID) {
                    Toast.makeText(this, R.string.logs_failed, Toast.LENGTH_LONG).show()
                    return@use
                }

                val prefs = getPreferences()
                prefs.edit {
                    clear()
                    import.settings.forEach { (key, value) ->
                        when (value) {
                            is Int -> putInt(key, value)
                            is Boolean -> putBoolean(key, value)
                            is String -> putString(key, value)
                            is Float -> putFloat(key, value)
                            is Long -> putLong(key, value)
                        }
                    }
                    putStringSet("selected_apps", import.apps.toSet())
                }
                HistoryUtils(this).saveHistory(import.history)

                recreate()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, R.string.storage_access_allowed_summary, Toast.LENGTH_SHORT).show()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received intent: ${intent?.action}")

            if (intent == null) {
                Log.w(TAG, "Received null intent")
                return
            }

            val senderOrd = intent.getIntExtra(SENDER, -1)
            val sender = Sender.entries.getOrNull(senderOrd)
            if (sender == null) {
                Log.w(TAG, "Received intent with unknown sender: $senderOrd")
                return
            }

            when (val action = intent.action) {
                STARTED_BROADCAST,
                STOPPED_BROADCAST -> { /* appStatus is updated via mutableStateOf in ByeDpiStatus */ }

                FAILED_BROADCAST -> {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_start, sender.name),
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                else -> Log.w(TAG, "Unknown action: $action")
            }
        }
    }

    // Proxy Test State
    private var isTestingState by mutableStateOf(false)
    private var progressText by mutableStateOf("")
    private var resultsLog by mutableStateOf(AnnotatedString(""))
    private var testJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intentFilter = IntentFilter().apply {
            addAction(STARTED_BROADCAST)
            addAction(STOPPED_BROADCAST)
            addAction(FAILED_BROADCAST)
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        if (getPreferences().getBoolean("auto_connect", false) && appStatus.first != AppStatus.Running) {
            start()
        }

        ShortcutUtils.update(this)

        setContent {
            ByeDpiTheme {
                val navController = rememberNavController()
                
                LaunchedEffect(intent?.getStringExtra("navigate_to")) {
                    intent?.getStringExtra("navigate_to")?.let {
                        navController.navigate(it)
                        intent?.removeExtra("navigate_to")
                    }
                }

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        MainScreen(
                            onPrepareVpn = { vpnRegister.launch(it) },
                            onOpenSettings = {
                                navController.navigate("settings")
                            },
                            onSaveLogs = { saveLogs() },
                            onCloseApp = { closeApp() },
                            onOpenEditor = {
                                if (getPreferences().getBoolean("byedpi_enable_cmd_settings", false)) {
                                    navController.navigate("settings/cmd")
                                } else {
                                    navController.navigate("settings/ui")
                                }
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onReset = {
                                getPreferences().edit { clear() }
                                recreate()
                            },
                            onExport = {
                                val fileName = "bbd_${System.currentTimeMillis().toReadableDateTime()}.json"
                                exportSettingsLauncher.launch(fileName)
                            },
                            onImport = {
                                importSettingsLauncher.launch(arrayOf("application/json"))
                            },
                            onNavigateToTest = {
                                navController.navigate("test")
                            },
                            onNavigateToAppSelection = {
                                navController.navigate("settings/apps")
                            },
                            onNavigateToCmdSettings = {
                                navController.navigate("settings/cmd")
                            },
                            onNavigateToUISettings = {
                                navController.navigate("settings/ui")
                            },
                            onOpenTelegram = {
                                openUrl("https://t.me/byedpi_chat")
                            },
                            onOpenSourceCode = {
                                openUrl("https://github.com/dovecoteescapee/ByeByeDPI")
                            },
                            onRequestStorageAccess = {
                                requestStoragePermission()
                            }
                        )
                    }
                    composable("settings/cmd") {
                        CmdSettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/ui") {
                        UISettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/apps") {
                        AppSelectionScreen(onBack = { navController.popBackStack() })
                    }
                    composable("test") {
                        TestScreen(
                            isTesting = isTestingState,
                            progressText = progressText,
                            resultsLog = resultsLog,
                            onBack = {
                                if (isTestingState) stopTesting()
                                navController.popBackStack()
                            },
                            onToggleTest = {
                                if (isTestingState) stopTesting() else startTesting()
                            },
                            onOpenSettings = {
                                if (!isTestingState) {
                                    navController.navigate("settings/test")
                                } else {
                                    Toast.makeText(this@MainActivity, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onLogClick = { command ->
                                showCommandDialog(command)
                            }
                        )
                    }
                    composable("settings/test") {
                        TestSettingsScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun start() {
        when (getPreferences().mode()) {
            Mode.VPN -> {
                val intentPrepare = android.net.VpnService.prepare(this)
                if (intentPrepare != null) {
                    vpnRegister.launch(intentPrepare)
                } else {
                    ServiceManager.start(this, Mode.VPN)
                }
            }

            Mode.Proxy -> ServiceManager.start(this, Mode.Proxy)
        }
    }

    private fun stop() {
        ServiceManager.stop(this)
    }

    private fun saveLogs() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "byedpi.log")
        }
        logsRegister.launch(intent)
    }

    private fun closeApp() {
        val (status, _) = appStatus
        if (status == AppStatus.Running) stop()
        finishAffinity()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: $url", e)
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${packageName}")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun Long.toReadableDateTime(): String {
        val format = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
        return format.format(this)
    }

    // Proxy Test Logic
    private fun startTesting() {
        val sites = loadSites()
        val cmds = loadCmds()
        val prefs = getPreferences()

        if (sites.isEmpty()) {
            resultsLog = AnnotatedString("${getString(R.string.test_settings_domain_empty)}\n")
            return
        }

        testJob = lifecycleScope.launch(Dispatchers.IO) {
            isTestingState = true
            prefs.edit { putBoolean("is_test_running", true) }
            val savedCmd = prefs.getString("byedpi_cmd_args", "").orEmpty()
            clearLog()

            withContext(Dispatchers.Main) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                progressText = ""
                resultsLog = AnnotatedString("")
            }

            val fullLog = prefs.getBoolean("byedpi_proxytest_fulllog", false)
            val logClickable = prefs.getBoolean("byedpi_proxytest_logclickable", false)
            val autoSort = prefs.getBoolean("byedpi_proxytest_autosort", false)
            val delaySec = prefs.getIntStringNotNull("byedpi_proxytest_delay", 1)
            val requestsCount = prefs.getIntStringNotNull("byedpi_proxytest_requests", 1)
            val requestTimeout = prefs.getLongStringNotNull("byedpi_proxytest_timeout", 5)

            val ip = prefs.getStringNotNull("byedpi_proxy_ip", "127.0.0.1")
            val port = prefs.getIntStringNotNull("byedpi_proxy_port", 1080)
            val siteChecker = SiteCheckUtils(ip, port)

            val successfulCmds = mutableListOf<Triple<String, Int, Int>>()

            for ((index, cmd) in cmds.withIndex()) {
                if (!isActive) break

                withContext(Dispatchers.Main) {
                    progressText = getString(R.string.test_process, index + 1, cmds.size)
                }

                updateCmdArgs(cmd)

                if (appStatus.first == AppStatus.Running) {
                    ServiceManager.stop(this@MainActivity)
                    waitForProxyStatus(AppStatus.Halted)
                }
                ServiceManager.start(this@MainActivity, Mode.Proxy)

                if (!waitForProxyStatus(AppStatus.Running)) {
                    continue
                }

                withContext(Dispatchers.Main) {
                    appendToResults(cmd, isLink = logClickable)
                    if (!fullLog) appendToResults(": ") else appendToResults("\n")
                }

                delay(delaySec * 500L)

                val totalRequests = sites.size * requestsCount
                val checkResults = siteChecker.checkSitesAsync(
                    sites = sites,
                    requestsCount = requestsCount,
                    requestTimeout = requestTimeout,
                    fullLog = fullLog,
                    onSiteChecked = { site, successCount, countRequests ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            appendToResults("$site - $successCount/$countRequests\n")
                        }
                    }
                )

                val successfulCount = checkResults.sumOf { it.second }
                val successPercentage = (successfulCount * 100) / totalRequests

                if (successPercentage >= 50 || autoSort) successfulCmds.add(Triple(cmd, successfulCount, totalRequests))

                delay(delaySec * 500L)

                withContext(Dispatchers.Main) {
                    appendToResults("$successfulCount/$totalRequests ($successPercentage%)\n\n")
                }

                ServiceManager.stop(this@MainActivity)
                waitForProxyStatus(AppStatus.Halted)
                delay(1000L)
            }

            if (autoSort) {
                successfulCmds.sortByDescending { it.second }
            } else {
                successfulCmds.sortByDescending { it.second }
            }

            withContext(Dispatchers.Main) {
                appendToResults("${getString(R.string.test_good_cmds)}\n\n")
                successfulCmds.forEachIndexed { index, (cmd, successCount, total) ->
                    val percentage = (successCount * 100) / total
                    appendToResults("${index + 1}. ")
                    appendToResults(cmd, isLink = true)
                    appendToResults(" - $successCount/$total ($percentage%)\n\n")
                }
                appendToResults(getString(R.string.test_complete_info))
            }

            stopTesting(savedCmd)
        }
    }

    private fun stopTesting(savedCmd: String? = null) {
        isTestingState = false
        getPreferences().edit { putBoolean("is_test_running", false) }
        savedCmd?.let { updateCmdArgs(it) }

        lifecycleScope.launch(Dispatchers.IO) {
            testJob?.cancel()
            testJob = null

            if (appStatus.first == AppStatus.Running) {
                ServiceManager.stop(this@MainActivity)
            }

            withContext(Dispatchers.Main) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                progressText = getString(R.string.test_complete)
            }
        }
    }

    private fun appendToResults(text: String, isLink: Boolean = false) {
        val newPart = buildAnnotatedString {
            if (isLink) {
                pushStringAnnotation(tag = "COMMAND", annotation = text.trim())
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(text)
                }
                pop()
            } else {
                append(text)
            }
        }
        resultsLog += newPart
        saveLog(if (isLink) "{$text}" else text)
    }

    private fun showCommandDialog(command: String) {
        val menuItems = arrayOf(
            getString(R.string.cmd_history_apply),
            getString(R.string.cmd_history_copy)
        )
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.cmd_history_menu))
            .setItems(menuItems) { _, which ->
                when (which) {
                    0 -> {
                        updateCmdArgs(command)
                        HistoryUtils(this).addCommand(command)
                    }
                    1 -> {
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("command", command))
                    }
                }
            }
            .show()
    }

    private suspend fun waitForProxyStatus(statusNeeded: AppStatus): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 3000) {
            if (appStatus.first == statusNeeded) {
                delay(500)
                return true
            }
            delay(100)
        }
        return false
    }

    private fun updateCmdArgs(cmd: String) {
        getPreferences().edit { putString("byedpi_cmd_args", cmd) }
    }

    private fun saveLog(text: String) {
        File(filesDir, "proxy_test.log").appendText(text)
    }

    private fun clearLog() {
        File(filesDir, "proxy_test.log").writeText("")
    }

    private fun loadSites(): List<String> {
        val prefs = getPreferences()
        val defaultDomainLists = setOf("youtube", "googlevideo")
        val selectedDomainLists = prefs.getStringSet("byedpi_proxytest_domain_lists", defaultDomainLists) ?: emptySet()
        val allDomains = mutableListOf<String>()
        for (domainList in selectedDomainLists) {
            val domains = when (domainList) {
                "custom" -> prefs.getString("byedpi_proxytest_domains", "").orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }
                else -> assets.open("proxytest_$domainList.sites").bufferedReader().useLines { it.toList() }
            }
            allDomains.addAll(domains)
        }
        return allDomains.distinct()
    }

    private fun loadCmds(): List<String> {
        val prefs = getPreferences()
        val userCommands = prefs.getBoolean("byedpi_proxytest_usercommands", false)
        val sniValue = prefs.getStringNotNull("byedpi_proxytest_sni", "google.com")
        val content = if (userCommands) prefs.getStringNotNull("byedpi_proxytest_commands", "")
        else assets.open("proxytest_strategies.list").bufferedReader().readText()
        return content.replace("{sni}", sniValue).lines().map { it.trim() }.filter { it.isNotEmpty() }
    }
}
