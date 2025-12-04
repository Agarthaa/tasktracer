package com.example.tasktracker.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tasktracker.data.repository.AuthRepository
import com.example.tasktracker.databinding.ActivitySignUpBinding
import com.example.tasktracker.presentation.auth.state.AuthState
import com.example.tasktracker.presentation.task.MainActivity
import com.example.tasktracker.util.extensions.showSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: SignUpViewModel by viewModels {
        SignUpViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val name = binding.etName.text.toString().trim()

            if (validateInputs(email, password, confirmPassword, name)) {
                hideKeyboard()
                viewModel.signUp(email, password, name)
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthState.Loading -> showLoading(true)
                    is AuthState.Success -> navigateToMain()
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

    private fun validateInputs(email: String, password: String, confirmPassword: String, name: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.textInputName.error = "Name is required"
            isValid = false
        } else {
            binding.textInputName.error = null
        }

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

        if (confirmPassword.isEmpty()) {
            binding.textInputConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.textInputConfirmPassword.error = "Passwords don't match"
            isValid = false
        } else {
            binding.textInputConfirmPassword.error = null
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnSignUp.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignUp.text = if (isLoading) "" else "Sign Up"
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