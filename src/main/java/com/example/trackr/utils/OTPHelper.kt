package com.example.trackr.utils

import android.os.Handler
import android.os.Looper
import kotlin.random.Random

object OTPHelper {

    fun generateOTP(): String {
        return String.format("%06d", Random.nextInt(0, 999999))
    }

    fun sendOTPToPhone(phoneNumber: String, otp: String, callback: (Boolean) -> Unit) {
        // Simulate sending OTP via SMS
        // In production, integrate with SMS gateway like Twilio or Semaphore

        Handler(Looper.getMainLooper()).postDelayed({
            callback(true)
            android.util.Log.d("OTP", "Sent OTP: $otp to $phoneNumber")
        }, 2000)
    }
}
