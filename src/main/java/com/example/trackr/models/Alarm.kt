package com.example.trackr.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Alarm(
    val id: String = "",
    val userId: String = "",
    val label: String = "",
    val timeInMillis: Long = 0,
    val hour: Int = 0,
    val minute: Int = 0,
    val repeat: String = "Once", // Once, Daily, Weekdays, Custom
    val ringtone: String = "Default",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    constructor() : this("", "", "", 0, 0, 0, "Once", "Default", true, 0L)

    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "userId" to userId,
            "label" to label,
            "timeInMillis" to timeInMillis,
            "hour" to hour,
            "minute" to minute,
            "repeat" to repeat,
            "ringtone" to ringtone,
            "isEnabled" to isEnabled,
            "createdAt" to createdAt
        )
    }

    fun getDisplayTime(): String {
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour < 12) "AM" else "PM"
        return String.format("%02d:%02d %s", h, minute, amPm)
    }
}