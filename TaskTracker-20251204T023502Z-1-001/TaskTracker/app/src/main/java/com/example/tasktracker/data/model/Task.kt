package com.example.tasktracker.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

@Parcelize
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Date? = null,
    var isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val userId: String = "",
    val category: String = "Personal"
) : Parcelable {
    @Parcelize
    enum class Priority(val displayName: String) : Parcelable {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High")
    }
}
