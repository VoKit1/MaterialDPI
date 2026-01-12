package io.github.dovecoteescapee.byedpi.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getPreferences()

    val isDynamicColor: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "dynamic_colors") trySend(getDynamicColor())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getDynamicColor())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val isDarkTheme: Flow<Boolean?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "app_theme") trySend(getDarkTheme())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getDarkTheme())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val selectedColorScheme: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "color_scheme") trySend(getSelectedColorScheme())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getSelectedColorScheme())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun getDynamicColor(): Boolean = prefs.getBoolean("dynamic_colors", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    fun getDarkTheme(): Boolean? = when (prefs.getString("app_theme", "system")) {
        "dark" -> true
        "light" -> false
        else -> null
    }

    fun getSelectedColorScheme(): String = prefs.getString("color_scheme", "Default") ?: "Default"

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean("dynamic_colors", enabled).apply()
    }

    fun setDarkTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
    }

    fun setColorScheme(scheme: String) {
        prefs.edit().putString("color_scheme", scheme).apply()
    }
}
