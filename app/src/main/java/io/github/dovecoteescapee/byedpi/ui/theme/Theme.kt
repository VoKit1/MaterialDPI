package io.github.dovecoteescapee.byedpi.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.dovecoteescapee.byedpi.data.ThemeManager
import io.github.dovecoteescapee.byedpi.ui.theme.ThemeColors.darkColor
import io.github.dovecoteescapee.byedpi.ui.theme.ThemeColors.lightColor

@Composable
fun TrackerTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val managerDynamic by themeManager.isDynamicColor.collectAsState(initial = themeManager.getDynamicColor())
    val managerDark by themeManager.isDarkTheme.collectAsState(initial = themeManager.getDarkTheme())
    val selectedSchemeName by themeManager.selectedColorScheme.collectAsState(initial = themeManager.getSelectedColorScheme())

    // Determine if dark theme should be used
    val isDark = managerDark == true || (managerDark == null && isSystemInDarkTheme())

    // Check if dynamic color scheme should be used (Android 12+)
    val isSystemDynamic = managerDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Get the appropriate color scheme
    val colorScheme = getColorScheme(isDark, isSystemDynamic, selectedSchemeName)

    // Update window settings if not in edit mode
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            configureWindow(view, isDark)
        }
    }

    // Apply MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

val colorSchemes = mapOf(
    "Default" to (lightColor to darkColor),
    "Blue" to (BlueLightColors to BlueDarkColors),
    "Red" to (RedLightColors to RedDarkColors),
    "Yellow" to (YellowLightColors to YellowDarkColors),
    "Orange" to (OrangeLightColors to OrangeDarkColors),
    "Purple" to (PurpleLightColors to PurpleDarkColors),
    "Pink" to (PinkLightColors to PinkDarkColors)
)


@Composable
private fun getColorScheme(
    isDark: Boolean,
    isSystemDynamic: Boolean,
    selectedSchemeName: String
): ColorScheme {
    val context = LocalContext.current

    if (isSystemDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    val selectedScheme = colorSchemes[selectedSchemeName] ?: (lightColor to darkColor)
    return if (isDark) selectedScheme.second else selectedScheme.first
}

private fun configureWindow(view: View, isDark: Boolean) {
    val window = (view.context as Activity).window

    val insetsController = WindowCompat.getInsetsController(window, view)
    insetsController.apply {
        isAppearanceLightStatusBars = !isDark
        isAppearanceLightNavigationBars = !isDark
    }
}
