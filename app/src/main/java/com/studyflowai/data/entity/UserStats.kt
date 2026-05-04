package com.studyflowai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    var xp: Int = 0,
    var level: Int = 1,
    var currentStreak: Int = 0,
    var lastTaskDate: Long = 0,
    var totalTasksCompleted: Int = 0,
    var totalPomodoroSessions: Int = 0,
    var totalFocusTimeMinutes: Int = 0,
    var focusScore: Int = 100,
    var currentAvatar: String = "default_agent",
    var unlockedThemes: String = "quantum", // Comma separated list
    var lastLoginDate: Long = System.currentTimeMillis()
) {
    val xpToNextLevel: Int
        get() = level * 1000

    val progressToNextLevel: Int
        get() = ((xp.toFloat() / xpToNextLevel.toFloat()) * 100).toInt()

    fun addXp(amount: Int) {
        xp += amount
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel
            level++
        }
    }
}

