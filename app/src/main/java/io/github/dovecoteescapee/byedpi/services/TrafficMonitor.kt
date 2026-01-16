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

    private val _totalUpload = MutableStateFlow("0 B")
    val totalUpload = _totalUpload.asStateFlow()

    private val _totalDownload = MutableStateFlow("0 B")
    val totalDownload = _totalDownload.asStateFlow()

    private val _duration = MutableStateFlow("00:00:00")
    val duration = _duration.asStateFlow()

    private var onUpdate: ((String, String, String, String) -> Unit)? = null

    fun setOnUpdateListener(listener: (String, String, String, String) -> Unit) {
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

            val startRxBytes = if (lastRx != -1L) lastRx else 0L
            val startTxBytes = if (lastTx != -1L) lastTx else 0L

            val startTime = System.currentTimeMillis()

            while (isActive) {
                delay(1000)
                val currentRx = TrafficStats.getUidRxBytes(Process.myUid())
                val currentTx = TrafficStats.getUidTxBytes(Process.myUid())
                
                val rxSpeed = if (lastRx != -1L && currentRx != -1L) currentRx - lastRx else 0
                val txSpeed = if (lastTx != -1L && currentTx != -1L) currentTx - lastTx else 0

                lastRx = currentRx
                lastTx = currentTx

                val totalRx = if (currentRx != -1L) currentRx - startRxBytes else 0
                val totalTx = if (currentTx != -1L) currentTx - startTxBytes else 0
                val durationMillis = System.currentTimeMillis() - startTime

                val dlSpeedStr = formatSpeed(rxSpeed)
                val ulSpeedStr = formatSpeed(txSpeed)
                val totalDlStr = formatBytes(totalRx)
                val totalUlStr = formatBytes(totalTx)
                val durationStr = formatDuration(durationMillis)

                _downloadSpeed.value = dlSpeedStr
                _uploadSpeed.value = ulSpeedStr
                _totalDownload.value = totalDlStr
                _totalUpload.value = totalUlStr
                _duration.value = durationStr

                onUpdate?.invoke(dlSpeedStr, ulSpeedStr, totalDlStr, totalUlStr)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _downloadSpeed.value = "0 KB/s"
        _uploadSpeed.value = "0 KB/s"
        _totalDownload.value = "0 B"
        _totalUpload.value = "0 B"
        _duration.value = "00:00:00"
    }

    private fun formatSpeed(bytes: Long): String {
        return "${formatBytes(bytes)}/s"
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "0 B"
        if (bytes < 1024) return "$bytes B"
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024f)
        if (bytes < 1024 * 1024 * 1024) return String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
        return String.format(Locale.US, "%.1f GB", bytes / (1024f * 1024f * 1024f))
    }

    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }
}