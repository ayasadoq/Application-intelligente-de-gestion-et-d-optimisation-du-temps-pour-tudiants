package com.etudiant.timemaster.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Single row
    var xp: Int = 0,
    var level: Int = 1,
    var currentStreak: Int = 0,
    var lastTaskDate: Long = 0,
    var totalTasksCompleted: Int = 0,
    var pomodoroSessionsCompleted: Int = 0,
    var focusScore: Int = 100 // New: Focus score (0-100)
) {
    val xpToNextLevel: Int
        get() = level * 1000

    fun addXp(amount: Int) {
        xp += amount
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel
            level++
        }
    }
}
