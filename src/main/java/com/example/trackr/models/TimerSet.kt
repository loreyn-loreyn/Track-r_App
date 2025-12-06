package com.example.trackr.models

data class TimerSet(
    val id: String = "",
    val userId: String = "",  // ADD THIS
    val label: String = "",
    val durationMillis: Long = 0,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val remainingMillis: Long = 0
) {
    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "userId" to userId,  // ADD THIS
            "label" to label,
            "durationMillis" to durationMillis,
            "isActive" to isActive,
            "isPaused" to isPaused,
            "remainingMillis" to remainingMillis
        )
    }
}