package io.github.dovecoteescapee.byedpi.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode

var appStatus by mutableStateOf(AppStatus.Halted to Mode.VPN)
    private set

fun setStatus(status: AppStatus, mode: Mode) {
    appStatus = status to mode
}
