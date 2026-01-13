package io.github.dovecoteescapee.byedpi.data

import com.google.gson.annotations.SerializedName

data class AppSettings(
    @SerializedName("app") val app: String,
    @SerializedName("version") val version: String,
    @SerializedName("history") val history: List<Command>,
    @SerializedName("apps") val apps: List<String>,
    @SerializedName("settings") val settings: Map<String, Any?>
)