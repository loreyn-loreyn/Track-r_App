package com.example.trackr.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.trackr.R
import com.example.trackr.main.MainActivity
import com.example.trackr.utils.FirebaseHelper
import com.example.trackr.utils.ValidationHelper
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordInput: TextInputEditText
    private lateinit var signInButton: MaterialButton
    private lateinit var signUpButton: MaterialButton
    private lateinit var forgotPasswordButton: MaterialButton
    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailInput = findViewById(R.id.emailInput)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        passwordInput = findViewById(R.id.passwordInput)
        signInButton = findViewById(R.id.signInButton)
        signUpButton = findViewById(R.id.signUpButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        signInButton.setOnClickListener {
            handleSignIn()
        }

        signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun handleSignIn() {
        // Clear previous errors
        emailInputLayout.error = null
        passwordInputLayout.error = null

        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            return
        }

        if (!ValidationHelper.isValidEmail(email)) {
            emailInputLayout.error = "Please enter a valid email address"
            return
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            return
        }

        if (!ValidationHelper.isValidPassword(password)) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            return
        }

        // Show loading
        setLoading(true)

        // Login
        lifecycleScope.launch {
            val result = FirebaseHelper.loginUser(email, password)

            setLoading(false)

            result.onSuccess {
                navigateToMain()
            }.onFailure { exception ->
                handleLoginError(exception)
            }
        }
    }

    private fun handleLoginError(exception: Throwable) {
        when {
            exception.message?.contains("password", ignoreCase = true) == true -> {
                passwordInputLayout.error = "Wrong email or password"
            }
            exception.message?.contains("user", ignoreCase = true) == true ||
                    exception.message?.contains("no user", ignoreCase = true) == true -> {
                emailInputLayout.error = "Account does not exist"
            }
            exception.message?.contains("network", ignoreCase = true) == true -> {
                emailInputLayout.error = "Network error. Check your connection"
            }
            exception.message?.contains("disabled", ignoreCase = true) == true -> {
                emailInputLayout.error = "This account has been disabled"
            }
            else -> {
                emailInputLayout.error = "Login failed. Please try again"
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        signInButton.isEnabled = !loading
        signUpButton.isEnabled = !loading
        forgotPasswordButton.isEnabled = !loading
        emailInput.isEnabled = !loading
        passwordInput.isEnabled = !loading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}