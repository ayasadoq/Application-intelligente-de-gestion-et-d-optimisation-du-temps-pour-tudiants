package com.etudiant.timemaster.data.repository

import com.etudiant.timemaster.data.dao.TacheDao
import com.etudiant.timemaster.data.dao.UserStatsDao
import com.etudiant.timemaster.data.entity.Tache
import com.etudiant.timemaster.data.entity.UserStats
import kotlinx.coroutines.flow.Flow

class TacheRepository(
    private val tacheDao: TacheDao,
    private val userStatsDao: UserStatsDao
) {
    val allTaches: Flow<List<Tache>> = tacheDao.getAllTaches()
    val nonTerminees: Flow<List<Tache>> = tacheDao.getTachesNonTerminees()
    val userStats: Flow<UserStats?> = userStatsDao.getUserStats()

    suspend fun addXp(amount: Int) {
        userStatsDao.addXpAndTask(amount)
    }
    
    suspend fun updateStats(stats: UserStats) {
        userStatsDao.insertOrUpdate(stats)
    }

    fun getTachesParDate(start: Long, end: Long): Flow<List<Tache>> {
        return tacheDao.getTachesParDate(start, end)
    }

    suspend fun getTacheById(id: Int): Tache? {
        return tacheDao.getTacheById(id)
    }

    suspend fun insert(tache: Tache): Long {
        return tacheDao.insert(tache)
    }

    suspend fun update(tache: Tache) {
        tacheDao.update(tache)
    }

    suspend fun delete(tache: Tache) {
        tacheDao.delete(tache)
    }

    suspend fun deleteById(id: Int) {
        tacheDao.deleteById(id)
    }
}
