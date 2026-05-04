package com.studyflowai.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.studyflowai.data.entity.Task

object AlarmHelper {

    @SuppressLint("ScheduleExactAlarm")
    fun planifierRappel(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Rappel 1 heure avant la date limite
        val tempsRappel = task.deadline - (60 * 60 * 1000)
        
        if (tempsRappel <= System.currentTimeMillis()) return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TITRE", "⏰ Rappel : ${task.title}")
            putExtra("DESC", "C'est le moment de s'y mettre ! Échéance dans 1 heure.")
            putExtra("ID", task.id)
            putExtra("TYPE", "RAPPEL")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            tempsRappel,
            pendingIntent
        )
    }

    fun annulerRappel(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
