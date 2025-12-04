package com.example.tasktracker.presentation.task.state

import com.example.tasktracker.data.model.Task

sealed class TaskState {
    object Loading : TaskState()
    object Empty : TaskState()
    data class Success(val tasks: List<Task>) : TaskState()
    data class Error(val message: String) : TaskState()
}