package com.etudiant.timemaster

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.etudiant.timemaster.model.Tache

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "timemaster_channel"
    private val CHANNEL_NAME = "Rappels TimeMaster"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(tache: Tache) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("titre", tache.titre)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tache.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = tache.dateLimite - 3600000 // 1 heure avant

        if (triggerTime > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titre = intent.getStringExtra("titre") ?: "Tâche"
        val notification = NotificationCompat.Builder(context, "timemaster_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rappel Tâche")
            .setContentText("Votre tâche '$titre' arrive à échéance dans 1 heure.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
