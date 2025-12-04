package com.example.tasktracker.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tasktracker.data.repository.AuthRepository
import com.example.tasktracker.databinding.ActivityLoginBinding
import com.example.tasktracker.presentation.auth.dialog.ResetPasswordDialog
import com.example.tasktracker.presentation.auth.state.AuthState
import com.example.tasktracker.presentation.task.MainActivity
import com.example.tasktracker.util.extensions.showSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateInputs(email, password)) {
                hideKeyboard()
                viewModel.signIn(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val dialog = ResetPasswordDialog { email ->
                if (email.isNotEmpty()) {
                    viewModel.sendPasswordReset(email)
                } else {
                    binding.root.showSnackbar("Email cannot be empty")
                }
            }

            dialog.show(supportFragmentManager, "ResetPasswordDialog")
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthState.Loading -> showLoading(true)
                    is AuthState.Success -> navigateToMain()
                    is AuthState.ResetPasswordSent -> {
                        showLoading(false)
                        binding.root.showSnackbar("Password reset email sent")
                        viewModel.clearState()
                    }
                    is AuthState.Error -> {
                        showLoading(false)
                        binding.root.showSnackbar(state.message)
                        viewModel.clearState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.textInputEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputEmail.error = "Valid email is required"
            isValid = false
        } else {
            binding.textInputEmail.error = null
        }

        if (password.isEmpty()) {
            binding.textInputPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.textInputPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.textInputPassword.error = null
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (isLoading) "" else "Login"
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}