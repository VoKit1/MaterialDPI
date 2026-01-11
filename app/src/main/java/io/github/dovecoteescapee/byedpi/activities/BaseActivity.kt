package io.github.dovecoteescapee.byedpi.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.dovecoteescapee.byedpi.fragments.MainSettingsFragment
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.getStringNotNull

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getPreferences()

        val lang = prefs.getStringNotNull("language", "system")
        MainSettingsFragment.setLang(lang)

        val theme = prefs.getStringNotNull("app_theme", "system")
        MainSettingsFragment.setTheme(theme)

        super.onCreate(savedInstanceState)
    }

}