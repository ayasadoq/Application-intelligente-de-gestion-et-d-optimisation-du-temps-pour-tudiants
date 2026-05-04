package com.studyflowai.data.dao

import androidx.room.*
import com.studyflowai.data.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllTasks(userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId")
    suspend fun getAllTasksSync(userId: String): List<Task>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = :completed")
    fun getTasksByStatus(userId: String, completed: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND deadline < :currentTime")
    suspend fun getOverdueTasks(currentTime: Long): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Update
    suspend fun updateTasks(tasks: List<Task>)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?
}
