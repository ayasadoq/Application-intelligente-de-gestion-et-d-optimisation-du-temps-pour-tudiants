package com.studyflowai.data.repository

import androidx.lifecycle.LiveData
import com.studyflowai.data.dao.AchievementDao
import com.studyflowai.data.dao.ExtracurricularDao
import com.studyflowai.data.dao.TaskDao
import com.studyflowai.data.dao.UserStatsDao
import com.studyflowai.data.entity.Achievement
import com.studyflowai.data.entity.ExtracurricularActivity
import com.studyflowai.data.entity.Task
import com.studyflowai.data.entity.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskRepository(
    private val taskDao: TaskDao,
    private val userStatsDao: UserStatsDao,
    private val achievementDao: AchievementDao,
    private val extracurricularDao: ExtracurricularDao
) {

    fun getAllTasks(userId: String): Flow<List<Task>> = taskDao.getAllTasks(userId)
    val userStats: Flow<UserStats?> = userStatsDao.getUserStats()
    val allAchievements: LiveData<List<Achievement>> = achievementDao.getAllAchievements()
    
    fun getTasksByStatus(userId: String, completed: Boolean): Flow<List<Task>> = taskDao.getTasksByStatus(userId, completed)

    fun getAllExtracurriculars(): Flow<List<ExtracurricularActivity>> = extracurricularDao.getAll()
    suspend fun getExtracurricularsSync(): List<ExtracurricularActivity> = extracurricularDao.getAll().first()
    suspend fun insertExtracurricular(activity: ExtracurricularActivity) = extracurricularDao.insert(activity)

    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    suspend fun updateUserStats(stats: UserStats) = userStatsDao.insertOrUpdate(stats)

    suspend fun getUserStatsSync(): UserStats? = userStatsDao.getUserStatsSync()

    suspend fun addXpAndTask(xpAmount: Int, isTaskCompletion: Boolean = true, pomodoroCompleted: Boolean = false) = userStatsDao.addXpAndTask(xpAmount, isTaskCompletion, pomodoroCompleted)

    suspend fun allAchievementsSync(): List<Achievement> = achievementDao.getAllAchievementsSync()

    suspend fun insertAchievements(achievements: List<Achievement>) = achievementDao.insertAchievements(achievements)

    suspend fun updateAchievement(achievement: Achievement) = achievementDao.updateAchievement(achievement)
}
