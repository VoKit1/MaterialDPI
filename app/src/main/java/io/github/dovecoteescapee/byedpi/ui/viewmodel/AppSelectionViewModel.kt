package io.github.dovecoteescapee.byedpi.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.dovecoteescapee.byedpi.data.AppInfo
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefs = context.getPreferences()
    private val pm = context.packageManager

    var apps by mutableStateOf<List<AppInfo>>(emptyList())
        private set
    var searchQuery by mutableStateOf("")
    var isLoading by mutableStateOf(true)
        private set
    var showSelectedOnly by mutableStateOf(false)

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            isLoading = true
            apps = withContext(Dispatchers.IO) {
                val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val selectedApps = prefs.getStringSet("selected_apps", setOf()) ?: setOf()

                installedApps
                    .filter { it.packageName != context.packageName }
                    .map { appInfo ->
                        val appName = try {
                            pm.getApplicationLabel(appInfo).toString()
                        } catch (_: Exception) {
                            appInfo.packageName
                        }
                        AppInfo(
                            appName,
                            appInfo.packageName,
                            selectedApps.contains(appInfo.packageName)
                        )
                    }
                    .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
            }
            isLoading = false
        }
    }

    fun toggleAppSelection(app: AppInfo, isChecked: Boolean) {
        val newSelected = prefs.getStringSet("selected_apps", setOf())?.toMutableSet() ?: mutableSetOf()
        if (isChecked) newSelected.add(app.packageName)
        else newSelected.remove(app.packageName)
        prefs.edit().putStringSet("selected_apps", newSelected).apply()

        apps = apps.map {
            if (it.packageName == app.packageName) it.copy(isSelected = isChecked)
            else it
        }
    }

    fun clearSelection() {
        prefs.edit().remove("selected_apps").apply()
        apps = apps.map { it.copy(isSelected = false) }
    }

    val filteredApps: List<AppInfo>
        get() {
            val filtered = if (searchQuery.isEmpty()) apps
            else apps.filter { it.appName.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
            
            return if (showSelectedOnly) filtered.filter { it.isSelected } else filtered
        }
}
