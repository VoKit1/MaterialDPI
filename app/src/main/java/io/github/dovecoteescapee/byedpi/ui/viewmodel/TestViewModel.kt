package io.github.dovecoteescapee.byedpi.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.HistoryUtils
import io.github.dovecoteescapee.byedpi.utility.SiteCheckUtils
import io.github.dovecoteescapee.byedpi.utility.getIntStringNotNull
import io.github.dovecoteescapee.byedpi.utility.getLongStringNotNull
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.getStringNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class TestResult(
    val command: String,
    val successCount: Int,
    val total: Int,
    val percentage: Int
)

class TestViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    var isTestingState by mutableStateOf(false)
        private set
    var progressText by mutableStateOf("")
        private set
    var resultsLog by mutableStateOf(AnnotatedString(""))
        private set
    var testResults = mutableStateListOf<TestResult>()
        private set
    var showCommandSheet by mutableStateOf<String?>(null)

    private var testJob: Job? = null

    fun startTesting() {
        val sites = loadSites()
        val cmds = loadCmds()
        val prefs = context.getPreferences()

        if (sites.isEmpty()) {
            resultsLog = AnnotatedString("${context.getString(R.string.test_settings_domain_empty)}\n")
            return
        }

        testJob = viewModelScope.launch(Dispatchers.IO) {
            isTestingState = true
            prefs.edit { putBoolean("is_test_running", true) }
            val savedCmd = prefs.getString("byedpi_cmd_args", "").orEmpty()
            clearLog()

            withContext(Dispatchers.Main) {
                progressText = ""
                resultsLog = AnnotatedString("")
                testResults.clear()
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

            for ((index, cmd) in cmds.withIndex()) {
                if (!isActive) break

                withContext(Dispatchers.Main) {
                    progressText = context.getString(R.string.test_process, index + 1, cmds.size)
                }

                updateCmdArgs(cmd)

                if (appStatus.first == AppStatus.Running) {
                    ServiceManager.stop(context)
                    waitForProxyStatus(AppStatus.Halted)
                }
                ServiceManager.start(context, Mode.Proxy)

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
                        viewModelScope.launch(Dispatchers.Main) {
                            appendToResults("$site - $successCount/$countRequests\n")
                        }
                    }
                )

                val successfulCount = checkResults.sumOf { it.second }
                val successPercentage = (successfulCount * 100) / totalRequests

                withContext(Dispatchers.Main) {
                    testResults.add(TestResult(cmd, successfulCount, totalRequests, successPercentage))
                    if (autoSort) {
                        testResults.sortByDescending { it.successCount }
                    }
                    appendToResults("$successfulCount/$totalRequests ($successPercentage%)\n\n")
                }

                delay(delaySec * 500L)

                ServiceManager.stop(context)
                waitForProxyStatus(AppStatus.Halted)
                delay(1000L)
            }

            withContext(Dispatchers.Main) {
                appendToResults(context.getString(R.string.test_complete_info))
            }

            stopTesting(savedCmd)
        }
    }

    fun stopTesting(savedCmd: String? = null) {
        isTestingState = false
        context.getPreferences().edit { putBoolean("is_test_running", false) }
        savedCmd?.let { updateCmdArgs(it) }

        viewModelScope.launch(Dispatchers.IO) {
            testJob?.cancel()
            testJob = null

            if (appStatus.first == AppStatus.Running) {
                ServiceManager.stop(context)
            }

            withContext(Dispatchers.Main) {
                progressText = context.getString(R.string.test_complete)
            }
        }
    }

    fun applyCommand(command: String) {
        updateCmdArgs(command)
        HistoryUtils(context).addCommand(command)
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
        context.getPreferences().edit { putString("byedpi_cmd_args", cmd) }
    }

    private fun saveLog(text: String) {
        File(context.filesDir, "proxy_test.log").appendText(text)
    }

    private fun clearLog() {
        File(context.filesDir, "proxy_test.log").writeText("")
    }

    private fun loadSites(): List<String> {
        val prefs = context.getPreferences()
        val defaultDomainLists = setOf("youtube", "googlevideo")
        val selectedDomainLists = prefs.getStringSet("byedpi_proxytest_domain_lists", defaultDomainLists) ?: emptySet()
        val allDomains = mutableListOf<String>()
        for (domainList in selectedDomainLists) {
            val domains = when (domainList) {
                "custom" -> prefs.getString("byedpi_proxytest_domains", "").orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }
                else -> context.assets.open("proxytest_$domainList.sites").bufferedReader().useLines { it.toList() }
            }
            allDomains.addAll(domains)
        }
        return allDomains.distinct()
    }

    private fun loadCmds(): List<String> {
        val prefs = context.getPreferences()
        val userCommands = prefs.getBoolean("byedpi_proxytest_usercommands", false)
        val sniValue = prefs.getStringNotNull("byedpi_proxytest_sni", "google.com")
        val content = if (userCommands) prefs.getStringNotNull("byedpi_proxytest_commands", "")
        else context.assets.open("proxytest_strategies.list").bufferedReader().readText()
        return content.replace("{sni}", sniValue).lines().map { it.trim() }.filter { it.isNotEmpty() }
    }
}
