package com.etudiant.timemaster.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.etudiant.timemaster.data.entity.Tache

object AlarmHelper {

    @SuppressLint("ScheduleExactAlarm")
    fun planifierRappel(context: Context, tache: Tache) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Rappel 1 heure avant la date limite
        val tempsRappel = tache.dateLimite - (60 * 60 * 1000)
        
        if (tempsRappel <= System.currentTimeMillis()) return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TITRE", "⏰ Rappel : ${tache.titre}")
            putExtra("DESC", "C'est le moment de s'y mettre ! Échéance dans 1 heure.")
            putExtra("ID", tache.id)
            putExtra("TYPE", "RAPPEL")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tache.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            tempsRappel,
            pendingIntent
        )
    }

    fun annulerRappel(context: Context, tacheId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tacheId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
