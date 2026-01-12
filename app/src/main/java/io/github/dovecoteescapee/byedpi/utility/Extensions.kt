package io.github.dovecoteescapee.byedpi.utility

import android.content.Context
import android.os.PowerManager
import java.text.SimpleDateFormat
import java.util.*

fun Long.toReadableDateTime(): String {
    val format = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
    return format.format(this)
}

fun Context.isBatteryOptimizationEnabled(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return !powerManager.isIgnoringBatteryOptimizations(packageName)
}
