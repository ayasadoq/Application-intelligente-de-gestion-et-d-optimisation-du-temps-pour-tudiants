package com.studyflowai.data.model

data class Student(
    val id: String = "",
    val name: String = "",
    val status: String = "Focusing...",
    val avatarUrl: String = "",
    val isOnline: Boolean = true,
    val focusScore: Int = 100
)

