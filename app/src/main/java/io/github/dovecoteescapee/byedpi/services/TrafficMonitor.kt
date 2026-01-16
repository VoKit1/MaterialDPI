package io.github.dovecoteescapee.byedpi.services

import android.net.TrafficStats
import android.os.Process
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object TrafficMonitor {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    private val _uploadSpeed = MutableStateFlow("0 KB/s")
    val uploadSpeed = _uploadSpeed.asStateFlow()

    private val _downloadSpeed = MutableStateFlow("0 KB/s")
    val downloadSpeed = _downloadSpeed.asStateFlow()

    private val _sentPackets = MutableStateFlow(0L)
    val sentPackets = _sentPackets.asStateFlow()

    private val _recvPackets = MutableStateFlow(0L)
    val recvPackets = _recvPackets.asStateFlow()

    private var onUpdate: ((String, String, Long, Long) -> Unit)? = null

    fun setOnUpdateListener(listener: (String, String, Long, Long) -> Unit) {
        onUpdate = listener
    }

    fun removeOnUpdateListener() {
        onUpdate = null
    }

    fun start() {
        if (job?.isActive == true) return
        
        job = scope.launch {
            var lastRx = TrafficStats.getUidRxBytes(Process.myUid())
            var lastTx = TrafficStats.getUidTxBytes(Process.myUid())
            
            val startRxPackets = TrafficStats.getUidRxPackets(Process.myUid())
            val startTxPackets = TrafficStats.getUidTxPackets(Process.myUid())

            while (isActive) {
                delay(1000)
                val currentRx = TrafficStats.getUidRxBytes(Process.myUid())
                val currentTx = TrafficStats.getUidTxBytes(Process.myUid())
                
                val currentRxPackets = TrafficStats.getUidRxPackets(Process.myUid())
                val currentTxPackets = TrafficStats.getUidTxPackets(Process.myUid())

                val rxSpeed = if (lastRx != -1L && currentRx != -1L) currentRx - lastRx else 0
                val txSpeed = if (lastTx != -1L && currentTx != -1L) currentTx - lastTx else 0

                lastRx = currentRx
                lastTx = currentTx

                val dlSpeedStr = formatSpeed(rxSpeed)
                val ulSpeedStr = formatSpeed(txSpeed)
                val recv = if (currentRxPackets != -1L && startRxPackets != -1L) currentRxPackets - startRxPackets else 0
                val sent = if (currentTxPackets != -1L && startTxPackets != -1L) currentTxPackets - startTxPackets else 0

                _downloadSpeed.value = dlSpeedStr
                _uploadSpeed.value = ulSpeedStr
                _recvPackets.value = recv
                _sentPackets.value = sent

                onUpdate?.invoke(dlSpeedStr, ulSpeedStr, sent, recv)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _downloadSpeed.value = "0 KB/s"
        _uploadSpeed.value = "0 KB/s"
        _sentPackets.value = 0L
        _recvPackets.value = 0L
    }

    private fun formatSpeed(bytes: Long): String {
        if (bytes < 0) return "0 B/s"
        if (bytes < 1024) return "$bytes B/s"
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB/s", bytes / 1024f)
        return String.format(Locale.US, "%.1f MB/s", bytes / (1024f * 1024f))
    }
}
