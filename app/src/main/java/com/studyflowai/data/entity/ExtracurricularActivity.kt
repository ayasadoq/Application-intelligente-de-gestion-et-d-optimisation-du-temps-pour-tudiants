package com.studyflowai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extracurricular_activities")
data class ExtracurricularActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dayOfWeek: Int, // Calendar.MONDAY, etc.
    val startHour: Int,
    val durationMinutes: Int
)
