package com.example.tasktracker.data.repository

import com.example.tasktracker.data.model.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signUp(email: String, password: String, name: String): Result<Unit> = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)?.await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    fun getCurrentUser() = auth.currentUser

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Get user metadata with creation time
    fun getUserMetadata(): UserMetadata? {
        val user = auth.currentUser
        return user?.let {
            UserMetadata(
                uid = it.uid,
                email = it.email ?: "No email",
                displayName = it.displayName ?: "User",
                creationTime = it.metadata?.creationTimestamp ?: System.currentTimeMillis()
            )
        }
    }
}

// Data class for user metadata
data class UserMetadata(
    val uid: String,
    val email: String,
    val displayName: String,
    val creationTime: Long
)