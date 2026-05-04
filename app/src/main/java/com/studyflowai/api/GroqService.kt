package com.studyflowai.api

import android.content.Context
import android.util.Log
import com.studyflowai.utils.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GroqService(context: Context) {
    private val securityManager = SecurityManager(context)
    
    private val api: GroqApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApi::class.java)
    }

    /**
     * Génère une réponse en prenant en compte l'historique complet pour la continuité.
     */
    suspend fun generateChat(
        history: List<GroqMessage>,
        systemInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = securityManager.getGroqKey()
        if (apiKey.isNullOrEmpty()) return@withContext Result.failure(Exception("Clé API Groq manquante"))

        val messages = mutableListOf<GroqMessage>()
        if (systemInstruction != null) {
            messages.add(GroqMessage(role = "system", content = systemInstruction))
        }
        messages.addAll(history)

        val request = GroqRequest(
            model = "llama-3.3-70b-versatile",
            messages = messages
        )

        try {
            val response = api.generateChatCompletion("Bearer $apiKey", request)
            val text = response.choices.firstOrNull()?.message?.content
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("Réponse vide de Groq"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("GroqService", "Erreur HTTP ${e.code()}: $errorBody")
            Result.failure(Exception("Erreur Groq ${e.code()}"))
        } catch (e: Exception) {
            Log.e("GroqService", "Erreur: ${e.message}")
            Result.failure(Exception("Connexion Groq échouée"))
        }
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null
    ): Result<String> {
        return generateChat(listOf(GroqMessage(role = "user", content = prompt)), systemInstruction)
    }
}
