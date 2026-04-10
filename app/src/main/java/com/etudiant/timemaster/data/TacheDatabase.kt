package com.etudiant.timemaster.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.etudiant.timemaster.model.Tache

@Database(entities = [Tache::class], version = 1, exportSchema = false)
abstract class TacheDatabase : RoomDatabase() {
    abstract fun tacheDao(): TacheDao

    companion object {
        @Volatile
        private var INSTANCE: TacheDatabase? = null

        fun getDatabase(context: Context): TacheDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TacheDatabase::class.java,
                    "tache_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
