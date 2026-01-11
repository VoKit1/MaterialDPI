package io.github.dovecoteescapee.byedpi.utility

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.google.gson.Gson
import io.github.dovecoteescapee.byedpi.BuildConfig
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppSettings

object SettingsUtils {
    private const val TAG = "SettingsUtils"

    fun exportSettings(context: Context, uri: Uri) {
        try {
            val prefs = context.getPreferences()
            val history = HistoryUtils(context).getHistory()
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

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export settings", e)
            Toast.makeText(context, "Failed to export settings", Toast.LENGTH_SHORT).show()
        }
    }

    fun importSettings(context: Context, uri: Uri, onRestart: () -> Unit) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader().readText()
                val import = try {
                    Gson().fromJson(json, AppSettings::class.java)
                } catch (e: Exception) {
                    null
                }

                if (import == null || import.app != BuildConfig.APPLICATION_ID) {
                    Toast.makeText(context, R.string.logs_failed, Toast.LENGTH_LONG).show()
                    return@use
                }

                val prefs = context.getPreferences()
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
                HistoryUtils(context).saveHistory(import.history)

                onRestart()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import settings", e)
            Toast.makeText(context, "Failed to import settings", Toast.LENGTH_SHORT).show()
        }
    }
}
