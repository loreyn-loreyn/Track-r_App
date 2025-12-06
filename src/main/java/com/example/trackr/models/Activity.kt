package com.example.trackr.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Activity(
    val id: String = "",
    val userId: String = "",
    val deadlineId: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    constructor() : this("", "", "", "", false, 0, 0L)

    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "userId" to userId,
            "deadlineId" to deadlineId,
            "title" to title,
            "isCompleted" to isCompleted,
            "order" to order,
            "createdAt" to createdAt
        )
    }
}