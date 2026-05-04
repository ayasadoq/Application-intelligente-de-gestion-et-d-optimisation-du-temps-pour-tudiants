package com.studyflowai.notification

import android.content.Context
import androidx.work.*
import com.studyflowai.data.entity.Task
import java.util.concurrent.TimeUnit

/**
 * BF4 - Planificateur de notifications.
 * Gère les rappels tactiques et le mode Démo Jury (10s).
 */
object NotificationScheduler {

    fun schedule(context: Context, task: Task) {
        val workManager = WorkManager.getInstance(context)
        
        // --- MODE DÉMO JURY (BF4) ---
        // Notification après 10 secondes pour prouver le fonctionnement
        enqueueReminder(workManager, task, "DEMO", 10000)

        // --- RAPPELS RÉELS (BF4) ---
        val now = System.currentTimeMillis()
        
        // 1. Rappel 24h avant
        val delay24h = (task.deadline - now) - TimeUnit.DAYS.toMillis(1)
        if (delay24h > 0) {
            enqueueReminder(workManager, task, "24h", delay24h)
        }

        // 2. Rappel 1h avant
        val delay1h = (task.deadline - now) - TimeUnit.HOURS.toMillis(1)
        if (delay1h > 0) {
            enqueueReminder(workManager, task, "1h", delay1h)
        }
    }

    private fun enqueueReminder(workManager: WorkManager, task: Task, tag: String, delay: Long) {
        val data = workDataOf(
            "task_title" to task.title,
            "type" to tag
        )

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("task_${task.id}")
            .build()

        workManager.enqueueUniqueWork(
            "task_${task.id}_$tag",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelNotifications(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("task_$taskId")
    }
}
