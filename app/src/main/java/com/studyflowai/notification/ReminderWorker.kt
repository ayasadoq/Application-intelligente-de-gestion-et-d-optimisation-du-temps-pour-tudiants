package com.studyflowai.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * BF4 - Worker exécutant l'affichage de la notification.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("task_title") ?: "Mission StudyFlow AI"
        val type = inputData.getString("type") ?: "Rappel"
        
        val message = when(type) {
            "24h" -> "Échéance dans 24 heures. Préparez-vous !"
            "1h" -> "Dernière ligne droite : échéance dans 1 heure !"
            "RETARD" -> "ALERTE : Cette mission est en retard !"
            "DEMO" -> "Démo Jury : Notification reçue avec succès ! 🚀"
            else -> "Une tâche nécessite votre attention."
        }

        showNotification(title, message)
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "STUDYFLOW_NOTIFS"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertes StudyFlow AI", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
