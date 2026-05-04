package com.studyflowai.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.studyflowai.R

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "TimeMaster_Notifications"
        const val CHANNEL_NAME = "Rappels de Tâches"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TITRE") ?: "Rappel de tâche"
        val desc = intent.getStringExtra("DESC") ?: "Une tâche arrive à échéance !"
        val type = intent.getStringExtra("TYPE") ?: "RAPPEL" // RAPPEL ou RETARD

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // BF4 - Notification Channel moderne
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal pour les alertes de gestion du temps"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (type == "RETARD") android.R.drawable.ic_dialog_alert else android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setColor(if (type == "RETARD") android.graphics.Color.RED else android.graphics.Color.BLUE)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}

