package io.github.dovecoteescapee.byedpi.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import io.github.dovecoteescapee.byedpi.utility.AppPreferences
import io.github.dovecoteescapee.byedpi.utility.HistoryUtils
import io.github.dovecoteescapee.byedpi.utility.getPreferences

class CmdSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val appPrefs = AppPreferences(application.getPreferences())
    private val historyUtils = HistoryUtils(application)

    var cmdArgs by mutableStateOf(appPrefs.cmdArgs)
        private set
    var history by mutableStateOf(historyUtils.getHistory())
        private set

    fun updateCmdArgs(newValue: String) {
        cmdArgs = newValue
        appPrefs.cmdArgs = newValue
        if (newValue.isNotBlank()) {
            historyUtils.addCommand(newValue)
            refreshHistory()
        }
    }

    fun clearCmdArgs() {
        cmdArgs = ""
        appPrefs.cmdArgs = ""
    }

    fun clearUnpinnedHistory() {
        historyUtils.clearUnpinnedHistory()
        refreshHistory()
    }

    fun clearAllHistory() {
        historyUtils.clearAllHistory()
        refreshHistory()
    }

    fun pinCommand(command: String) {
        historyUtils.addCommand(command)
        historyUtils.pinCommand(command)
        refreshHistory()
    }

    fun unpinCommand(command: String) {
        historyUtils.unpinCommand(command)
        refreshHistory()
    }

    fun renameCommand(command: String, newName: String) {
        historyUtils.renameCommand(command, newName)
        refreshHistory()
    }

    fun editCommand(oldText: String, newText: String) {
        if (newText.isNotBlank()) {
            historyUtils.editCommand(oldText, newText)
            if (cmdArgs == oldText) {
                cmdArgs = newText
                appPrefs.cmdArgs = newText
            }
            refreshHistory()
        }
    }

    fun deleteCommand(command: String) {
        historyUtils.deleteCommand(command)
        refreshHistory()
    }

    private fun refreshHistory() {
        history = historyUtils.getHistory()
    }
}
