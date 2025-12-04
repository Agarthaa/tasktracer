package com.example.tasktracker.data.repository

import com.example.tasktracker.data.model.Result
import com.example.tasktracker.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class TaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")

    // Real-time updates
    fun getTasksRealTime(userId: String): Flow<Result<List<Task>>> = callbackFlow {
        val listener = tasksCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { document ->
                        try {
                            documentToTask(document)
                        } catch (e: Exception) {
                            println("DEBUG: Error parsing task ${document.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(Result.Success(tasks))
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun createTask(task: Task): Result<String> = try {
        val taskData = hashMapOf(
            "title" to task.title,
            "description" to task.description,
            "priority" to task.priority.name,
            "dueDate" to task.dueDate,
            "isCompleted" to task.isCompleted,
            "createdAt" to task.createdAt,
            "updatedAt" to task.updatedAt,
            "userId" to task.userId,
            "category" to task.category
        )
        val document = tasksCollection.add(taskData).await()
        Result.Success(document.id)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    suspend fun updateTask(taskId: String, task: Task): Result<Unit> = try {
        val taskData = hashMapOf(
            "title" to task.title,
            "description" to task.description,
            "priority" to task.priority.name,
            "dueDate" to task.dueDate,
            "isCompleted" to task.isCompleted,
            "updatedAt" to Date(),
            "userId" to task.userId,
            "category" to task.category
        )
        tasksCollection.document(taskId).update(taskData as Map<String, Any>).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    suspend fun deleteTask(taskId: String): Result<Unit> = try {
        tasksCollection.document(taskId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    suspend fun getTasks(userId: String): Result<List<Task>> = try {
        val querySnapshot = tasksCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val tasks = querySnapshot.documents.mapNotNull { document ->
            try {
                documentToTask(document)
            } catch (e: Exception) {
                null
            }
        }
        Result.Success(tasks)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    suspend fun getTaskById(taskId: String): Result<Task?> = try {
        val document = tasksCollection.document(taskId).get().await()
        val task = try {
            documentToTask(document)
        } catch (e: Exception) {
            null
        }
        Result.Success(task)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean): Result<Unit> = try {
        tasksCollection.document(taskId)
            .update(
                mapOf(
                    "isCompleted" to isCompleted,
                    "updatedAt" to Date()
                )
            )
            .await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    private fun documentToTask(document: com.google.firebase.firestore.DocumentSnapshot): Task {
        val title = document.getString("title") ?: ""
        val description = document.getString("description") ?: ""
        val priorityStr = document.getString("priority") ?: "MEDIUM"
        val priority = Task.Priority.valueOf(priorityStr)
        val dueDate = document.getDate("dueDate")
        val isCompleted = document.getBoolean("isCompleted") ?: false
        val createdAt = document.getDate("createdAt") ?: Date()
        val updatedAt = document.getDate("updatedAt") ?: Date()
        val userId = document.getString("userId") ?: ""
        val category = document.getString("category") ?: "Personal"

        return Task(
            id = document.id,
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            isCompleted = isCompleted,
            createdAt = createdAt,
            updatedAt = updatedAt,
            userId = userId,
            category = category
        )
    }
}