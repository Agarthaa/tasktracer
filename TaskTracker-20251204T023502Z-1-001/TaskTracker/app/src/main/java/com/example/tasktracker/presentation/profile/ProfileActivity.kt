package com.example.tasktracker.presentation.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.data.repository.AuthRepository
import com.example.tasktracker.databinding.ActivityProfileBinding
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadUserData()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // REMOVED logout button from profile
        // binding.btnLogout.setOnClickListener {
        //     viewModel.logout()
        //     navigateToLogin()
        // }
    }

    private fun loadUserData() {
        val userMetadata = viewModel.getUserMetadata()
        userMetadata?.let { metadata ->
            binding.tvUserName.text = metadata.displayName
            binding.tvUserEmail.text = metadata.email
            binding.tvEmail.text = metadata.email

            val memberSince = formatCreationTime(metadata.creationTime)
            binding.tvMemberSince.text = memberSince
        } ?: run {
            binding.tvUserName.text = "User"
            binding.tvUserEmail.text = "No email"
            binding.tvEmail.text = "No email"
            binding.tvMemberSince.text = "Unknown"
        }
    }

    private fun formatCreationTime(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            dateFormat.format(date)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, com.example.tasktracker.presentation.auth.LoginActivity::class.java))
        finishAffinity()
    }
}

class ProfileViewModel(
    private val authRepository: AuthRepository
) : androidx.lifecycle.ViewModel() {

    fun getCurrentUser() = authRepository.getCurrentUser()

    fun getUserMetadata() = authRepository.getUserMetadata()

    fun logout() {
        authRepository.signOut()
    }
}

class ProfileViewModelFactory(private val authRepository: AuthRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}