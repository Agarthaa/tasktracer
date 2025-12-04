package com.example.tasktracker.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.tasktracker.data.repository.AuthRepository
import com.example.tasktracker.databinding.ActivitySplashBinding
import com.example.tasktracker.presentation.auth.LoginActivity
import com.example.tasktracker.presentation.task.MainActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToApp()
    }

    private fun navigateToApp() {
        Handler(Looper.getMainLooper()).postDelayed({
            val destination = if (authRepository.isUserLoggedIn()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this, destination))
            finish()
        }, 3000)
    }
}