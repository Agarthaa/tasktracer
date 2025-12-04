package com.example.tasktracker.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.data.model.Result
import com.example.tasktracker.data.model.Task
import com.example.tasktracker.data.repository.AuthRepository
import com.example.tasktracker.data.repository.TaskRepository
import com.example.tasktracker.presentation.task.state.TaskState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskState>(TaskState.Loading)
    val uiState: StateFlow<TaskState> = _uiState.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = TaskState.Loading
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    when (val result = taskRepository.getTasks(user.uid)) {
                        is Result.Success -> {
                            _tasks.value = result.data
                            _uiState.value = if (result.data.isEmpty()) {
                                TaskState.Empty
                            } else {
                                TaskState.Success(result.data)
                            }
                        }
                        is Result.Failure -> {
                            _uiState.value = TaskState.Error(result.exception.message ?: "Failed to load tasks")
                            _tasks.value = emptyList()
                        }
                    }
                } else {
                    _uiState.value = TaskState.Error("User not authenticated")
                    _tasks.value = emptyList()
                }
            } catch (e: Exception) {
                _uiState.value = TaskState.Error(e.message ?: "Failed to load tasks")
                _tasks.value = emptyList()
            }
        }
    }

    fun refreshTasks() {
        loadTasks()
    }

    fun createTask(task: Task) = viewModelScope.launch {
        try {
            when (val result = taskRepository.createTask(task)) {
                is Result.Success -> {
                    // After creating, refresh the task list
                    loadTasks()
                }
                is Result.Failure -> {
                    _uiState.value = TaskState.Error("Failed to create task: ${result.exception.message}")
                }
            }
        } catch (e: Exception) {
            _uiState.value = TaskState.Error("Failed to create task: ${e.message}")
        }
    }

    fun updateTask(taskId: String, updatedTask: Task) = viewModelScope.launch {
        try {
            when (val result = taskRepository.updateTask(taskId, updatedTask)) {
                is Result.Success -> {
                    // After updating, refresh the task list
                    loadTasks()
                }
                is Result.Failure -> {
                    _uiState.value = TaskState.Error("Failed to update task: ${result.exception.message}")
                }
            }
        } catch (e: Exception) {
            _uiState.value = TaskState.Error("Failed to update task: ${e.message}")
        }
    }

    fun deleteTask(taskId: String) = viewModelScope.launch {
        try {
            when (val result = taskRepository.deleteTask(taskId)) {
                is Result.Success -> {
                    // After deleting, refresh the task list
                    loadTasks()
                }
                is Result.Failure -> {
                    _uiState.value = TaskState.Error("Failed to delete task: ${result.exception.message}")
                }
            }
        } catch (e: Exception) {
            _uiState.value = TaskState.Error("Failed to delete task: ${e.message}")
        }
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) = viewModelScope.launch {
        try {
            when (val result = taskRepository.toggleTaskCompletion(taskId, isCompleted)) {
                is Result.Success -> {
                    // After toggling, refresh the task list
                    loadTasks()
                }
                is Result.Failure -> {
                    _uiState.value = TaskState.Error("Failed to update task: ${result.exception.message}")
                }
            }
        } catch (e: Exception) {
            _uiState.value = TaskState.Error("Failed to update task: ${e.message}")
        }
    }

    fun clearError() {
        if (_tasks.value.isNotEmpty()) {
            _uiState.value = TaskState.Success(_tasks.value)
        } else {
            _uiState.value = TaskState.Empty
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(taskRepository, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}