package com.studyflowai.utils

import android.util.Log
import com.google.gson.Gson
import com.studyflowai.data.entity.Task
import com.studyflowai.model.ai.AiPlanResponse
import java.util.Calendar

/**
 * Manager robuste pour le parsing et la conversion des données AI.
 */
class AiPlannerManager(private val userId: String) {
    private val gson = Gson()

    /**
     * Extrait le texte lisible pour l'utilisateur.
     * On privilégie le texte explicatif de l'IA s'il existe.
     */
    fun getDisplayableText(text: String?): String {
        if (text.isNullOrBlank()) return "..."
        
        // 1. On sépare le texte du JSON
        val textWithoutJson = text.replace(Regex("\\{.*\\}", RegexOption.DOT_MATCHES_ALL), "").trim()
        
        // 2. On parse le plan pour les tâches (background)
        val plan = parseAiResponse(text)

        return when {
            // Si l'IA a écrit une explication détaillée en dehors du JSON, on la montre !
            textWithoutJson.isNotBlank() -> textWithoutJson
            // Sinon on prend le message court du coach dans le JSON
            plan.coachMessage.isNotBlank() && plan.coachMessage != "string" -> plan.coachMessage
            else -> "J'ai préparé votre plan d'action."
        }
    }

    /**
     * Parse une réponse JSON brute en objet AiPlanResponse avec gestion d'erreurs.
     */
    fun parseAiResponse(json: String?): AiPlanResponse {
        if (json.isNullOrBlank()) return AiPlanResponse.fallback("Réponse vide")
        
        val cleanJson = sanitizeJson(json)
        
        // Tentative de trouver un bloc JSON si la réponse contient du texte autour
        val jsonMatch = Regex("\\{.*\\}", RegexOption.DOT_MATCHES_ALL).find(cleanJson)
        val finalJson = jsonMatch?.value ?: cleanJson

        return try {
            val response = gson.fromJson(finalJson, AiPlanResponse::class.java)
            if (response == null) {
                AiPlanResponse.fallback("Parsing JSON a retourné null")
            } else {
                response
            }
        } catch (e: Exception) {
            Log.e("AiPlannerManager", "Erreur de parsing JSON: ${e.message}\nBrut: $json")
            // Si le parsing échoue, on retourne le texte brut dans le coachMessage au lieu d'une erreur fatale
            AiPlanResponse(
                coachMessage = cleanJson.take(500), // On garde le texte tel quel pour l'utilisateur
                motivationalMessage = "Voici ma réponse."
            )
        }
    }

    /**
     * Extrait uniquement le message texte pour l'affichage UI simple.
     */
    fun extractMessage(json: String?): String {
        val plan = parseAiResponse(json)
        return if (plan.coachMessage.isNotBlank()) {
            plan.coachMessage
        } else if (plan.motivationalMessage.isNotBlank()) {
            plan.motivationalMessage
        } else {
            "J'ai mis à jour votre planning."
        }
    }

    /**
     * Nettoie les balises Markdown éventuelles.
     */
    fun sanitizeJson(json: String): String {
        var cleaned = json.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        return cleaned.trim()
    }

    /**
     * Convertit les tâches AI en entités Room.
     */
    fun convertAiTasksToEntities(aiPlan: AiPlanResponse): List<Task> {
        val now = System.currentTimeMillis()
        return aiPlan.tasks.mapIndexed { index, aiTask ->
            // Utilise le timestamp suggéré par l'IA ou décale par rapport à maintenant
            val deadlineTime = aiTask.suggestedTimestamp ?: (now + (index + 1) * 3600000L)
            
            Task(
                userId = userId,
                title = aiTask.title,
                description = "Généré par StudyFlow AI (${aiTask.category})",
                deadline = deadlineTime,
                priority = mapPriority(aiTask.priority),
                estimatedTime = aiTask.durationMin,
                isCompleted = false
            )
        }
    }

    private fun mapPriority(priority: String): Int {
        return when (priority.lowercase()) {
            "3", "high" -> 3
            "2", "medium" -> 2
            "1", "low" -> 1
            else -> 2
        }
    }

    private fun calculateDeadline(priority: String): Long {
        val calendar = Calendar.getInstance()
        val daysToAdd = when (priority.lowercase()) {
            "high" -> 1
            "medium" -> 3
            else -> 7
        }
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return calendar.timeInMillis
    }
}
