package com.studyflowai.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.studyflowai.data.dao.TaskDao
import com.studyflowai.data.dao.UserStatsDao
import com.studyflowai.data.dao.AchievementDao
import com.studyflowai.data.dao.ExtracurricularDao
import com.studyflowai.data.dao.ChatMessageDao
import com.studyflowai.data.entity.Task
import com.studyflowai.data.entity.UserStats
import com.studyflowai.data.entity.Achievement
import com.studyflowai.data.entity.ExtracurricularActivity
import com.studyflowai.data.entity.ChatMessageEntity

/**
 * Base de données unifiée de l'application StudyFlow AI.
 */
@Database(
    entities = [Task::class, UserStats::class, Achievement::class, ExtracurricularActivity::class, ChatMessageEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun extracurricularDao(): ExtracurricularDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyflow_ai_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
