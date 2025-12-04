package com.example.tasktracker.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)