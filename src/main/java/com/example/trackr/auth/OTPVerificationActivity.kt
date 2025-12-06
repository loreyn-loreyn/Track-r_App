package com.example.trackr.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.trackr.R
import com.example.trackr.utils.FirebaseHelper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class OTPVerificationActivity : AppCompatActivity() {

    private lateinit var otpInput: TextInputEditText
    private lateinit var verifyButton: MaterialButton
    private lateinit var resendButton: MaterialButton
    private lateinit var timerText: TextView
    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var resetButton: MaterialButton
    private lateinit var progressBar: View

    private var contact: String = ""
    private var sentOTP: String = ""
    private var isEmail: Boolean = true
    private var isVerified: Boolean = false
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        contact = intent.getStringExtra("CONTACT") ?: ""
        sentOTP = intent.getStringExtra("OTP") ?: ""
        isEmail = intent.getBooleanExtra("IS_EMAIL", true)

        initViews()
        setupListeners()
        startResendTimer()
    }

    private fun initViews() {
        otpInput = findViewById(R.id.otpInput)
        verifyButton = findViewById(R.id.verifyButton)
        resendButton = findViewById(R.id.resendButton)
        timerText = findViewById(R.id.timerText)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        resetButton = findViewById(R.id.resetButton)
        progressBar = findViewById(R.id.progressBar)

        // Initially hide password fields
        newPasswordInput.visibility = View.GONE
        confirmPasswordInput.visibility = View.GONE
        resetButton.visibility = View.GONE
    }

    private fun setupListeners() {
        verifyButton.setOnClickListener {
            handleVerifyOTP()
        }

        resendButton.setOnClickListener {
            handleResendOTP()
        }

        resetButton.setOnClickListener {
            handleResetPassword()
        }
    }

    private fun startResendTimer() {
        resendButton.isEnabled = false
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                timerText.text = ""
                resendButton.isEnabled = true
            }
        }.start()
    }

    private fun handleVerifyOTP() {
        val enteredOTP = otpInput.text.toString().trim()

        if (enteredOTP.isEmpty()) {
            showError("Please enter OTP")
            return
        }

        if (enteredOTP == sentOTP) {
            isVerified = true
            Toast.makeText(this, "OTP verified successfully!", Toast.LENGTH_SHORT).show()
            showPasswordFields()
        } else {
            showError("Invalid OTP. Please try again")
        }
    }

    private fun handleResendOTP() {
        Toast.makeText(this, "OTP resent to $contact", Toast.LENGTH_SHORT).show()
        startResendTimer()
    }

    private fun showPasswordFields() {
        otpInput.visibility = View.GONE
        verifyButton.visibility = View.GONE
        resendButton.visibility = View.GONE
        timerText.visibility = View.GONE

        newPasswordInput.visibility = View.VISIBLE
        confirmPasswordInput.visibility = View.VISIBLE
        resetButton.visibility = View.VISIBLE
    }

    private fun handleResetPassword() {
        val newPassword = newPasswordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (newPassword.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }

        if (newPassword != confirmPassword) {
            showError("Passwords do not match")
            return
        }

        setLoading(true)

        // Reset password via Firebase
        lifecycleScope.launch {
            val result = FirebaseHelper.resetPassword(contact, newPassword)

            setLoading(false)

            result.onSuccess {
                Toast.makeText(this@OTPVerificationActivity,
                    "Password reset successfully!", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }.onFailure {
                showError("Failed to reset password. Please try again")
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}