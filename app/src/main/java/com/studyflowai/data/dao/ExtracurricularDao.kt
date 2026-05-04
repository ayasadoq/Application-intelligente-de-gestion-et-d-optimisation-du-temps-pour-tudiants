package com.studyflowai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.studyflowai.data.entity.ExtracurricularActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtracurricularDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ExtracurricularActivity)

    @Query("SELECT * FROM extracurricular_activities")
    fun getAll(): Flow<List<ExtracurricularActivity>>

    @Delete
    suspend fun delete(activity: ExtracurricularActivity)
}
