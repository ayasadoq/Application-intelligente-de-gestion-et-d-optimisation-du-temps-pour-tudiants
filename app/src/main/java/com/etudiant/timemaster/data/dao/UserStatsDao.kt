package com.etudiant.timemaster.data.dao

import androidx.room.*
import com.etudiant.timemaster.data.entity.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStats)

    @Transaction
    suspend fun addXpAndTask(xpAmount: Int) {
        val stats = getUserStatsSync() ?: UserStats()
        stats.addXp(xpAmount)
        stats.totalTasksCompleted++
        
        // Streak logic
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000
        if (now - stats.lastTaskDate < 2 * oneDayMillis && now - stats.lastTaskDate > oneDayMillis) {
            stats.currentStreak++
        } else if (now - stats.lastTaskDate >= 2 * oneDayMillis) {
            stats.currentStreak = 1
        }
        stats.lastTaskDate = now
        
        insertOrUpdate(stats)
    }

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsSync(): UserStats?
}
