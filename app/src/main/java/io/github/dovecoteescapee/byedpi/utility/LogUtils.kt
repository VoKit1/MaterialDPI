package io.github.dovecoteescapee.byedpi.utility

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import io.github.dovecoteescapee.byedpi.R
import java.io.IOException

object LogUtils {
    private const val TAG = "LogUtils"

    fun collectLogs(): String? =
        try {
            Runtime.getRuntime()
                .exec("logcat *:D -d")
                .inputStream.bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to collect logs", e)
            null
        }

    fun saveLogs(context: Context, uri: Uri) {
        val logs = collectLogs()

        if (logs == null) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    R.string.logs_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            context.contentResolver.openOutputStream(uri)?.use {
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
