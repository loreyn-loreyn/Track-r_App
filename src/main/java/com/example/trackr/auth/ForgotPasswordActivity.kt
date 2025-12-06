package com.example.trackr.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.trackr.R
import com.example.trackr.utils.FirebaseHelper
import com.example.trackr.utils.OTPHelper
import com.example.trackr.utils.PhoneNumberValidator
import com.example.trackr.utils.ValidationHelper
import kotlinx.coroutines.launch
import android.widget.TextView

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInputLayout: TextInputLayout
    private lateinit var phoneInput: TextInputEditText
    private lateinit var otpInputLayout: TextInputLayout
    private lateinit var otpInput: TextInputEditText
    private lateinit var newPasswordInputLayout: TextInputLayout
    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var sendOTPButton: MaterialButton
    private lateinit var verifyOTPButton: MaterialButton
    private lateinit var resetPasswordButton: MaterialButton
    private lateinit var resendButton: MaterialButton
    private lateinit var timerText: TextView
    private lateinit var backButton: MaterialButton
    private lateinit var progressBar: View

    private var sentOTP: String = ""
    private var userPhone: String = ""
    private var isOTPVerified: Boolean = false
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initViews()
        setupListeners()
        showEmailStep()
    }

    private fun initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailInput = findViewById(R.id.emailInput)
        phoneInputLayout = findViewById(R.id.phoneInputLayout)
        phoneInput = findViewById(R.id.phoneInput)
        otpInputLayout = findViewById(R.id.otpInputLayout)
        otpInput = findViewById(R.id.otpInput)
        newPasswordInputLayout = findViewById(R.id.newPasswordInputLayout)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        sendOTPButton = findViewById(R.id.sendOTPButton)
        verifyOTPButton = findViewById(R.id.verifyOTPButton)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        resendButton = findViewById(R.id.resendButton)
        timerText = findViewById(R.id.timerText)
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        sendOTPButton.setOnClickListener { handleSendOTP() }
        verifyOTPButton.setOnClickListener { handleVerifyOTP() }
        resetPasswordButton.setOnClickListener { handleResetPassword() }
        resendButton.setOnClickListener { handleResendOTP() }
        backButton.setOnClickListener { finish() }
    }

    private fun showEmailStep() {
        emailInputLayout.visibility = View.VISIBLE
        sendOTPButton.visibility = View.VISIBLE
        phoneInputLayout.visibility = View.GONE
        otpInputLayout.visibility = View.GONE
        verifyOTPButton.visibility = View.GONE
        resendButton.visibility = View.GONE
        timerText.visibility = View.GONE
        newPasswordInputLayout.visibility = View.GONE
        confirmPasswordInputLayout.visibility = View.GONE
        resetPasswordButton.visibility = View.GONE
    }

    private fun showOTPStep() {
        emailInputLayout.visibility = View.GONE
        sendOTPButton.visibility = View.GONE
        phoneInputLayout.visibility = View.VISIBLE
        phoneInput.setText(userPhone)
        phoneInput.isEnabled = false
        otpInputLayout.visibility = View.VISIBLE
        verifyOTPButton.visibility = View.VISIBLE
        resendButton.visibility = View.VISIBLE
        timerText.visibility = View.VISIBLE
        startResendTimer()
    }

    private fun showPasswordStep() {
        phoneInputLayout.visibility = View.GONE
        otpInputLayout.visibility = View.GONE
        verifyOTPButton.visibility = View.GONE
        resendButton.visibility = View.GONE
        timerText.visibility = View.GONE
        newPasswordInputLayout.visibility = View.VISIBLE
        confirmPasswordInputLayout.visibility = View.VISIBLE
        resetPasswordButton.visibility = View.VISIBLE
    }

    private fun handleSendOTP() {
        emailInputLayout.error = null
        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            return
        }

        if (!ValidationHelper.isValidEmail(email)) {
            emailInputLayout.error = "Please enter a valid email address"
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            val result = FirebaseHelper.getUserByEmail(email)

            setLoading(false)

            result.onSuccess { user ->
                userPhone = user.phoneNumber
                sentOTP = OTPHelper.generateOTP()

                // Send OTP to phone
                OTPHelper.sendOTPToPhone(userPhone, sentOTP) { success ->
                    if (success) {
                        showOTPStep()
                        showInfo("OTP sent to ${maskPhone(userPhone)}")
                    } else {
                        emailInputLayout.error = "Failed to send OTP"
                    }
                }
            }.onFailure {
                emailInputLayout.error = "Account not found with this email"
            }
        }
    }

    private fun handleVerifyOTP() {
        otpInputLayout.error = null
        val enteredOTP = otpInput.text.toString().trim()

        if (enteredOTP.isEmpty()) {
            otpInputLayout.error = "Please enter OTP"
            return
        }

        if (enteredOTP == sentOTP) {
            isOTPVerified = true
            showPasswordStep()
            showInfo("OTP verified successfully!")
        } else {
            otpInputLayout.error = "Invalid OTP. Please try again"
        }
    }

    private fun handleResendOTP() {
        sentOTP = OTPHelper.generateOTP()
        OTPHelper.sendOTPToPhone(userPhone, sentOTP) { success ->
            if (success) {
                startResendTimer()
                showInfo("OTP resent to ${maskPhone(userPhone)}")
            }
        }
    }

    private fun handleResetPassword() {
        newPasswordInputLayout.error = null
        confirmPasswordInputLayout.error = null

        val newPassword = newPasswordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (newPassword.isEmpty()) {
            newPasswordInputLayout.error = "Password is required"
            return
        }

        if (!ValidationHelper.isValidPassword(newPassword)) {
            newPasswordInputLayout.error = "Password must be at least 6 characters"
            return
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            return
        }

        if (newPassword != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords do not match"
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            val result = FirebaseHelper.updatePassword(userPhone, newPassword)

            setLoading(false)

            result.onSuccess {
                showSuccessAndFinish()
            }.onFailure {
                newPasswordInputLayout.error = "Failed to reset password"
            }
        }
    }

    private fun startResendTimer() {
        resendButton.isEnabled = false
        countDownTimer?.cancel()

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

    private fun maskPhone(phone: String): String {
        return if (phone.length > 4) {
            "****${phone.takeLast(4)}"
        } else {
            phone
        }
    }

    private fun showInfo(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccessAndFinish() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Password reset successfully! Please login with your new password.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}