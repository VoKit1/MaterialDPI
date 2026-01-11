package io.github.dovecoteescapee.byedpi.utility

import java.text.SimpleDateFormat
import java.util.Locale

fun Long.toReadableDateTime(): String {
    val format = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
    return format.format(this)
}
