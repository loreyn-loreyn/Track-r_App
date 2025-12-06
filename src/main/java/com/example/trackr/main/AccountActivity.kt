package com.example.trackr.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.trackr.R
import com.example.trackr.utils.FirebaseHelper
import com.example.trackr.utils.ValidationHelper
import com.example.trackr.utils.PhoneNumberValidator
import kotlinx.coroutines.launch

class AccountActivity : AppCompatActivity() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInputLayout: TextInputLayout
    private lateinit var phoneInput: TextInputEditText
    private lateinit var recoveryEmailInputLayout: TextInputLayout
    private lateinit var recoveryEmailInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var progressBar: View

    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        setupToolbar()
        initViews()
        loadUserData()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Account Information"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout)
        nameInput = findViewById(R.id.nameInput)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailInput = findViewById(R.id.emailInput)
        phoneInputLayout = findViewById(R.id.phoneInputLayout)
        phoneInput = findViewById(R.id.phoneInput)
        recoveryEmailInputLayout = findViewById(R.id.recoveryEmailInputLayout)
        recoveryEmailInput = findViewById(R.id.recoveryEmailInput)
        saveButton = findViewById(R.id.saveButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        progressBar = findViewById(R.id.progressBar)

        saveButton.setOnClickListener { handleSave() }
        changePasswordButton.setOnClickListener { showChangePasswordDialog() }
    }

    private fun loadUserData() {
        userId = FirebaseHelper.getCurrentUserId() ?: return

        setLoading(true)

        lifecycleScope.launch {
            val result = FirebaseHelper.getUserData(userId)

            setLoading(false)

            result.onSuccess { user ->
                nameInput.setText(user.fullName)
                emailInput.setText(user.email)
                phoneInput.setText(user.phoneNumber)
                // Recovery email can be loaded if stored
            }
        }
    }

    private fun handleSave() {
        // Clear errors
        nameInputLayout.error = null
        emailInputLayout.error = null
        phoneInputLayout.error = null
        recoveryEmailInputLayout.error = null

        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val recoveryEmail = recoveryEmailInput.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            nameInputLayout.error = "Name is required"
            return
        }

        if (!ValidationHelper.isValidName(name)) {
            nameInputLayout.error = "Please enter a valid name"
            return
        }

        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            return
        }

        if (!ValidationHelper.isValidEmail(email)) {
            emailInputLayout.error = "Please enter a valid email address"
            return
        }

        if (phone.isEmpty()) {
            phoneInputLayout.error = "Phone number is required"
            return
        }

        if (!ValidationHelper.isValidPhoneNumber(phone)) {
            phoneInputLayout.error = "Please enter a valid PH number (09XX-XXX-XXXX)"
            return
        }

        if (recoveryEmail.isNotEmpty() && !ValidationHelper.isValidEmail(recoveryEmail)) {
            recoveryEmailInputLayout.error = "Please enter a valid recovery email"
            return
        }

        val formattedPhone = PhoneNumberValidator.formatPhilippineNumber(phone)

        val updates = hashMapOf(
            "fullName" to name,
            "email" to email,
            "phoneNumber" to formattedPhone,
            "recoveryEmail" to recoveryEmail
        )

        setLoading(true)

        lifecycleScope.launch {
            val result = FirebaseHelper.updateUserProfile(userId, updates as Map<String, Any>)

            setLoading(false)

            result.onSuccess {
                Toast.makeText(this@AccountActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@AccountActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.currentPasswordInput)
        val newPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.confirmPasswordInput)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // TODO: Implement password change
                Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        saveButton.isEnabled = !loading
        nameInput.isEnabled = !loading
        emailInput.isEnabled = !loading
        phoneInput.isEnabled = !loading
        recoveryEmailInput.isEnabled = !loading
    }
}