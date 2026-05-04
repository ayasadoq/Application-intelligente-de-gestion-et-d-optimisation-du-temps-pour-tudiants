package com.studyflowai.ui.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Appliquer le thème avant super.onCreate
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        
        // Gérer la direction de l'interface pour l'Arabe (RTL)
        val lang = prefs.getString("language", "fr") ?: "fr"
        if (lang == "ar") {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "fr") ?: "fr"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        
        val configuration = newBase.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }
}
