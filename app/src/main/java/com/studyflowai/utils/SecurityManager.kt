package com.studyflowai.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manager de sécurité robuste optimisé pour Xiaomi/MIUI.
 * Bascule en mode normal si le mode chiffré échoue pour éviter les ANR/Crashes.
 */
class SecurityManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secure_settings_v2",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SecurityManager", "Erreur Crypto, bascule en mode standard", e)
            context.getSharedPreferences("standard_settings", Context.MODE_PRIVATE)
        }
    }

    fun saveGroqKey(key: String) {
        sharedPreferences.edit().putString("groq_api_key", key).apply()
    }

    fun getGroqKey(): String? {
        return sharedPreferences.getString("groq_api_key", null)
    }

    fun hasValidKey(): Boolean = !getGroqKey().isNullOrBlank()
}
