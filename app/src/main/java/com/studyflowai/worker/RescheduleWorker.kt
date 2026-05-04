package com.studyflowai.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studyflowai.api.GroqService
import com.studyflowai.data.database.AppDatabase
import com.studyflowai.utils.AiPlannerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RescheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val taskDao = database.taskDao()
        val groqService = GroqService(applicationContext)
        val aiPlannerManager = AiPlannerManager("default_user")

        // 1. Détecter les tâches en retard
        val overdueTasks = taskDao.getOverdueTasks(System.currentTimeMillis())
        if (overdueTasks.isEmpty()) return@withContext Result.success()

        // 2. Préparer le prompt pour l'IA avec contexte temporel
        val nowFormatted = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val tasksDescription = overdueTasks.joinToString("\n") { "- ${it.title} (Priorité: ${it.priority})" }
        
        val prompt = """
            Tu es l'expert en récupération de planning StudyFlow.
            DATE ACTUELLE : $nowFormatted
            
            Les tâches suivantes sont EN RETARD :
            $tasksDescription
            
            MISSION : Reprogramme-les intelligemment sur les 3 prochains jours en évitant les surcharges.
            Répartis les tâches selon leur priorité.
            
            RETOURNE UNIQUEMENT UN JSON STRICT :
            {
              "status": "RECOVERY",
              "coach_message": "Un message d'encouragement personnalisé",
              "tasks": [
                { "title": "...", "suggestedTimestamp": 1712345678000, "priority": "high" }
              ]
            }
        """.trimIndent()

        // 3. Appeler l'IA et appliquer la stratégie intelligente
        try {
            val result = groqService.generateContent(prompt)
            val response = result.getOrNull() ?: return@withContext Result.retry()
            
            // On parse le JSON retourné par l'IA
            val aiPlan = aiPlannerManager.parseAiResponse(response)

            // On prépare la liste des tâches à mettre à jour
            val tasksToUpdate = mutableListOf<com.studyflowai.data.entity.Task>()

            aiPlan?.tasks?.forEach { aiTask ->
                val existingTask = overdueTasks.find { it.title == aiTask.title }
                if (existingTask != null) {
                    val newDeadline = aiTask.suggestedTimestamp ?: (System.currentTimeMillis() + 86400000)
                    tasksToUpdate.add(existingTask.copy(
                        deadline = newDeadline,
                        description = "✨ Replanifié intelligemment : ${existingTask.description}"
                    ))
                }
            }

            if (tasksToUpdate.isNotEmpty()) {
                taskDao.updateTasks(tasksToUpdate)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
