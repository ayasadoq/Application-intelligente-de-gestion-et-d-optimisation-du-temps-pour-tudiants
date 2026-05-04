package com.studyflowai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val iconResId: Int,
    val requirementType: String,
    val requirementValue: Int,
    var isUnlocked: Boolean = false,
    val unlockDate: Long? = null
)

