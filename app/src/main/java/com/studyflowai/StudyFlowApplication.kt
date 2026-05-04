package com.studyflowai

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

class StudyFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Appliquer le thème sauvegardé dès le lancement de l'app
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        
        // La langue sera gérée dans BaseActivity ou via le contexte
    }
}
