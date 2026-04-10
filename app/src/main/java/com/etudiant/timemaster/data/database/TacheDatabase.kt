package com.etudiant.timemaster.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.etudiant.timemaster.data.dao.TacheDao
import com.etudiant.timemaster.data.dao.UserStatsDao
import com.etudiant.timemaster.data.entity.Tache
import com.etudiant.timemaster.data.entity.UserStats

@Database(entities = [Tache::class, UserStats::class], version = 4, exportSchema = false)
abstract class TacheDatabase : RoomDatabase() {
    abstract fun tacheDao(): TacheDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile
        private var INSTANCE: TacheDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE taches ADD COLUMN dureeReelle INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_stats` (`id` INTEGER NOT NULL, `xp` INTEGER NOT NULL, `level` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `lastTaskDate` INTEGER NOT NULL, `totalTasksCompleted` INTEGER NOT NULL, `pomodoroSessionsCompleted` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_stats ADD COLUMN focusScore INTEGER NOT NULL DEFAULT 100")
            }
        }

        fun getDatabase(context: Context): TacheDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TacheDatabase::class.java,
                    "timemaster_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
