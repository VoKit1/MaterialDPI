package io.github.dovecoteescapee.byedpi.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import io.github.dovecoteescapee.byedpi.core.ByeDpiProxyUIPreferences
import io.github.dovecoteescapee.byedpi.utility.UIPreferences
import io.github.dovecoteescapee.byedpi.utility.getPreferences

class UISettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val uiPrefs = UIPreferences(application.getPreferences())

    var maxConnections by mutableStateOf(uiPrefs.maxConnections)
        private set
    var bufferSize by mutableStateOf(uiPrefs.bufferSize)
        private set
    var noDomain by mutableStateOf(uiPrefs.noDomain)
        private set
    var tcpFastOpen by mutableStateOf(uiPrefs.tcpFastOpen)
        private set

    var desyncMethod by mutableStateOf(
        ByeDpiProxyUIPreferences.DesyncMethod.fromName(uiPrefs.desyncMethod)
    )
        private set

    var hostsMode by mutableStateOf(
        ByeDpiProxyUIPreferences.HostsMode.fromName(uiPrefs.hostsMode)
    )
        private set

    var hostsBlacklist by mutableStateOf(
        uiPrefs.hostsBlacklist.split(",").filter { it.isNotBlank() }
    )
        private set
    var hostsWhitelist by mutableStateOf(
        uiPrefs.hostsWhitelist.split(",").filter { it.isNotBlank() }
    )
        private set
    var defaultTtl by mutableStateOf(uiPrefs.defaultTtl)
        private set
    var splitPosition by mutableStateOf(uiPrefs.splitPosition)
        private set
    var splitAtHost by mutableStateOf(uiPrefs.splitAtHost)
        private set
    var dropSack by mutableStateOf(uiPrefs.dropSack)
        private set
    var fakeTtl by mutableStateOf(uiPrefs.fakeTtl)
        private set
    var fakeOffset by mutableStateOf(uiPrefs.fakeOffset)
        private set
    var fakeSni by mutableStateOf(uiPrefs.fakeSni)
        private set
    var oobData by mutableStateOf(uiPrefs.oobData)
        private set

    var desyncHttp by mutableStateOf(uiPrefs.desyncHttp)
        private set
    var desyncHttps by mutableStateOf(uiPrefs.desyncHttps)
        private set
    var desyncUdp by mutableStateOf(uiPrefs.desyncUdp)
        private set

    var hostMixedCase by mutableStateOf(uiPrefs.hostMixedCase)
        private set
    var domainMixedCase by mutableStateOf(uiPrefs.domainMixedCase)
        private set
    var hostRemoveSpaces by mutableStateOf(uiPrefs.hostRemoveSpaces)
        private set

    var tlsRecEnabled by mutableStateOf(uiPrefs.tlsRecEnabled)
        private set
    var tlsRecPosition by mutableStateOf(uiPrefs.tlsRecPosition)
        private set
    var tlsRecAtSni by mutableStateOf(uiPrefs.tlsRecAtSni)
        private set
    var udpFakeCount by mutableStateOf(uiPrefs.udpFakeCount)
        private set

    fun updateMaxConnections(value: String) {
        uiPrefs.maxConnections = value
        maxConnections = value
    }

    fun updateBufferSize(value: String) {
        uiPrefs.bufferSize = value
        bufferSize = value
    }

    fun updateNoDomain(value: Boolean) {
        uiPrefs.noDomain = value
        noDomain = value
    }

    fun updateTcpFastOpen(value: Boolean) {
        uiPrefs.tcpFastOpen = value
        tcpFastOpen = value
    }

    fun updateHostsMode(value: String) {
        uiPrefs.hostsMode = value
        hostsMode = ByeDpiProxyUIPreferences.HostsMode.fromName(value)
    }

    fun updateHostsBlacklist(value: List<String>) {
        val stringValue = value.joinToString(",")
        uiPrefs.hostsBlacklist = stringValue
        hostsBlacklist = value
    }

    fun updateHostsWhitelist(value: List<String>) {
        val stringValue = value.joinToString(",")
        uiPrefs.hostsWhitelist = stringValue
        hostsWhitelist = value
    }

    fun updateDefaultTtl(value: String) {
        uiPrefs.defaultTtl = value
        defaultTtl = value
    }

    fun updateDesyncMethod(value: String) {
        uiPrefs.desyncMethod = value
        desyncMethod = ByeDpiProxyUIPreferences.DesyncMethod.fromName(value)
    }

    fun updateSplitPosition(value: String) {
        uiPrefs.splitPosition = value
        splitPosition = value
    }

    fun updateSplitAtHost(value: Boolean) {
        uiPrefs.splitAtHost = value
        splitAtHost = value
    }

    fun updateDropSack(value: Boolean) {
        uiPrefs.dropSack = value
        dropSack = value
    }

    fun updateFakeTtl(value: String) {
        uiPrefs.fakeTtl = value
        fakeTtl = value
    }

    fun updateFakeOffset(value: String) {
        uiPrefs.fakeOffset = value
        fakeOffset = value
    }

    fun updateFakeSni(value: String) {
        uiPrefs.fakeSni = value
        fakeSni = value
    }

    fun updateOobData(value: String) {
        if (value.length <= 1) {
            uiPrefs.oobData = value
            oobData = value
        }
    }

    fun updateDesyncHttp(value: Boolean) {
        uiPrefs.desyncHttp = value
        desyncHttp = value
    }

    fun updateDesyncHttps(value: Boolean) {
        uiPrefs.desyncHttps = value
        desyncHttps = value
    }

    fun updateDesyncUdp(value: Boolean) {
        uiPrefs.desyncUdp = value
        desyncUdp = value
    }

    fun updateHostMixedCase(value: Boolean) {
        uiPrefs.hostMixedCase = value
        hostMixedCase = value
    }

    fun updateDomainMixedCase(value: Boolean) {
        uiPrefs.domainMixedCase = value
        domainMixedCase = value
    }

    fun updateHostRemoveSpaces(value: Boolean) {
        uiPrefs.hostRemoveSpaces = value
        hostRemoveSpaces = value
    }

    fun updateTlsRecEnabled(value: Boolean) {
        uiPrefs.tlsRecEnabled = value
        tlsRecEnabled = value
    }

    fun updateTlsRecPosition(value: String) {
        uiPrefs.tlsRecPosition = value
        tlsRecPosition = value
    }

    fun updateTlsRecAtSni(value: Boolean) {
        uiPrefs.tlsRecAtSni = value
        tlsRecAtSni = value
    }

    fun updateUdpFakeCount(value: String) {
        uiPrefs.udpFakeCount = value
        udpFakeCount = value
    }
}
