package com.etudiant.timemaster.data

import androidx.lifecycle.LiveData
import com.etudiant.timemaster.model.Tache

class TacheRepository(private val tacheDao: TacheDao) {
    val allTaches: LiveData<List<Tache>> = tacheDao.getAllTaches()

    suspend fun insert(tache: Tache) {
        tacheDao.insert(tache)
    }

    suspend fun update(tache: Tache) {
        tacheDao.update(tache)
    }

    suspend fun delete(tache: Tache) {
        tacheDao.delete(tache)
    }

    fun getTachesByDate(date: Long): LiveData<List<Tache>> {
        val startOfDay = date
        val endOfDay = date + 86400000 // 24 hours
        return tacheDao.getTachesByDateRange(startOfDay, endOfDay)
    }
}
