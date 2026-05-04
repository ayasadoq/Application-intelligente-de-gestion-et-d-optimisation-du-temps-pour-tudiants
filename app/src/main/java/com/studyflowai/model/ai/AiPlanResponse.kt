package com.studyflowai.model.ai

import com.google.gson.annotations.SerializedName

/**
 * Modèle de réponse AI strictement typé pour la production.
 * Utilise des valeurs par défaut pour garantir la non-nullité après parsing.
 */
data class AiPlanResponse(
    @SerializedName("needsClarification") val needsClarification: Boolean = false,
    @SerializedName("clarificationQuestions") val clarificationQuestions: List<String> = emptyList(),
    @SerializedName("tasks") val tasks: List<AiTask> = emptyList(),
    @SerializedName("weeklySchedule") val weeklySchedule: Map<String, List<AiTask>> = emptyMap(),
    @SerializedName("missedTasksRescheduled") val missedTasksRescheduled: Boolean = false,
    @SerializedName("suggestions") val suggestions: List<String> = emptyList(),
    @SerializedName("modifications") val modifications: List<AiTaskModification> = emptyList(),
    @SerializedName("motivationalMessage") val motivationalMessage: String = "",
    @SerializedName("coach_message") val coachMessage: String = ""
) {
    companion object {
        fun fallback(error: String) = AiPlanResponse(
            needsClarification = true,
            coachMessage = "Désolé, j'ai rencontré une difficulté technique : $error",
            motivationalMessage = "Gardons le cap malgré tout !"
        )
    }
}

data class AiTask(
    @SerializedName("title") val title: String = "Nouvelle tâche",
    @SerializedName("category") val category: String = "Études",
    @SerializedName("durationMin") val durationMin: Int = 30,
    @SerializedName("priority") val priority: String = "medium",
    @SerializedName("suggestedTimestamp") val suggestedTimestamp: Long? = null
)

data class AiTaskModification(
    @SerializedName("id") val id: Int,
    @SerializedName("newDurationMin") val newDurationMin: Int,
    @SerializedName("reason") val reason: String = ""
)
