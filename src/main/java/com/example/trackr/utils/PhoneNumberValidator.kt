package com.example.trackr.utils

object PhoneNumberValidator {

    // Philippine mobile number formats:
    // 09XX-XXX-XXXX or +639XX-XXX-XXXX
    fun isValidPhilippineNumber(phoneNumber: String): Boolean {
        val cleaned = phoneNumber.replace("[\\s-]".toRegex(), "")

        return when {
            // 09XX format (11 digits)
            cleaned.matches("^09[0-9]{9}$".toRegex()) -> true
            // +639XX format (13 digits)
            cleaned.matches("^\\+639[0-9]{9}$".toRegex()) -> true
            // 639XX format (12 digits)
            cleaned.matches("^639[0-9]{9}$".toRegex()) -> true
            else -> false
        }
    }

    fun formatPhilippineNumber(phoneNumber: String): String {
        val cleaned = phoneNumber.replace("[\\s-]".toRegex(), "")

        return when {
            cleaned.startsWith("09") -> "+63${cleaned.substring(1)}"
            cleaned.startsWith("639") -> "+$cleaned"
            cleaned.startsWith("+639") -> cleaned
            else -> phoneNumber
        }
    }
}