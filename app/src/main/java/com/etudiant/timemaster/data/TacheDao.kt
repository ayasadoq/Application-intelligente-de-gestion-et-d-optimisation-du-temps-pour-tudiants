package com.etudiant.timemaster.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.etudiant.timemaster.model.Tache

@Dao
interface TacheDao {
    @Query("SELECT * FROM taches ORDER BY priorite * importance DESC, dateLimite ASC")
    fun getAllTaches(): LiveData<List<Tache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tache: Tache)

    @Update
    suspend fun update(tache: Tache)

    @Delete
    suspend fun delete(tache: Tache)

    @Query("SELECT * FROM taches WHERE dateLimite >= :startOfDay AND dateLimite < :endOfDay")
    fun getTachesByDateRange(startOfDay: Long, endOfDay: Long): LiveData<List<Tache>>
}
