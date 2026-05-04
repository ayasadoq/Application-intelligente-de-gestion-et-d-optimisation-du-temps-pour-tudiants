package com.studyflowai.data.dao

import androidx.room.*
import com.studyflowai.data.entity.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStats)

    @Transaction
    suspend fun addXpAndTask(xpAmount: Int, isTaskCompletion: Boolean = true, pomodoroCompleted: Boolean = false) {
        val stats = getUserStatsSync() ?: UserStats()
        stats.xp += xpAmount
        
        if (isTaskCompletion) {
            stats.totalTasksCompleted++
        }
        
        if (pomodoroCompleted) {
            stats.totalPomodoroSessions++
            stats.totalFocusTimeMinutes += 25
            stats.focusScore = (stats.focusScore + 5).coerceAtMost(100)
        }
        
        // Handle leveling
        while (stats.xp >= stats.xpToNextLevel) {
            stats.xp -= stats.xpToNextLevel
            stats.level++
        }

        if (isTaskCompletion) {
            // Streak logic
            val now = System.currentTimeMillis()
            val oneDayMillis = 24 * 60 * 60 * 1000
            if (now - stats.lastTaskDate < 2 * oneDayMillis && now - stats.lastTaskDate > oneDayMillis) {
                stats.currentStreak++
            } else if (now - stats.lastTaskDate >= 2 * oneDayMillis) {
                stats.currentStreak = 1
            }
            stats.lastTaskDate = now
        }
        
        insertOrUpdate(stats)
    }

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsSync(): UserStats?
}

