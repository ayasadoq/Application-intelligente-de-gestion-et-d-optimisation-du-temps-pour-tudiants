package com.etudiant.timemaster.data.dao

import androidx.room.*
import com.etudiant.timemaster.data.entity.Tache
import kotlinx.coroutines.flow.Flow

@Dao
interface TacheDao {
    @Query("SELECT * FROM taches ORDER BY dateLimite ASC")
    fun getAllTaches(): Flow<List<Tache>>

    @Query("SELECT * FROM taches WHERE id = :id")
    suspend fun getTacheById(id: Int): Tache?

    @Query("SELECT * FROM taches WHERE estTerminee = 0 ORDER BY dateLimite ASC")
    fun getTachesNonTerminees(): Flow<List<Tache>>

    @Query("SELECT * FROM taches WHERE dateLimite >= :startOfDay AND dateLimite <= :endOfDay")
    fun getTachesParDate(startOfDay: Long, endOfDay: Long): Flow<List<Tache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tache: Tache): Long

    @Update
    suspend fun update(tache: Tache)

    @Delete
    suspend fun delete(tache: Tache)

    @Query("DELETE FROM taches WHERE id = :id")
    suspend fun deleteById(id: Int)
}
