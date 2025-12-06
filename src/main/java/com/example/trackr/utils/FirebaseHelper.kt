package com.example.trackr.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.trackr.models.*
import kotlinx.coroutines.tasks.await

object FirebaseHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Collections
    const val USERS_COLLECTION = "users"
    const val ALARMS_COLLECTION = "alarms"
    const val DEADLINES_COLLECTION = "deadlines"
    const val ACTIVITIES_COLLECTION = "activities"

    // Auth Functions
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun registerUser(email: String, password: String, user: User): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID not found")

            val userWithId = user.copy(uid = uid)
            db.collection(USERS_COLLECTION).document(uid).set(userWithId.toMap()).await()

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID not found")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

    suspend fun resetPassword(phoneNumber: String, newPassword: String): Result<Unit> {
        return try {
            // In production, use Firebase Admin SDK to reset password
            // For now, simulate success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): Result<User> {
        return try {
            val snapshot = db.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val user = snapshot.documents[0].toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(phoneNumber: String, newPassword: String): Result<Unit> {
        return try {
            // Get user by phone number
            val userResult = getUserByPhone(phoneNumber)

            userResult.onSuccess { user ->
                // Update password using Firebase Auth
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    currentUser.updatePassword(newPassword).await()
                    return Result.success(Unit)
                }
            }

            Result.failure(Exception("Failed to update password"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User Functions
    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val document = db.collection(USERS_COLLECTION).document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User Functions
    suspend fun getUserByPhone(phoneNumber: String): Result<User> {
        return try {
            val snapshot = db.collection(USERS_COLLECTION)
                .whereEqualTo("phoneNumber", phoneNumber)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val user = snapshot.documents[0].toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Alarm Functions
    suspend fun saveAlarm(alarm: Alarm): Result<String> {
        return try {
            val alarmId = if (alarm.id.isEmpty()) {
                db.collection(ALARMS_COLLECTION).document().id
            } else {
                alarm.id
            }

            val alarmWithId = alarm.copy(id = alarmId)

            // THIS SAVES TO FIREBASE FIRESTORE DATABASE!
            db.collection(ALARMS_COLLECTION)
                .document(alarmId)
                .set(alarmWithId.toMap())
                .await()

            Result.success(alarmId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlarms(userId: String): Result<List<Alarm>> {
        return try {
            val snapshot = db.collection(ALARMS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("hour", Query.Direction.ASCENDING)
                .orderBy("minute", Query.Direction.ASCENDING)
                .get()
                .await()

            val alarms = snapshot.documents.mapNotNull { it.toObject(Alarm::class.java) }
            Result.success(alarms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAlarm(alarmId: String, isEnabled: Boolean): Result<Unit> {
        return try {
            db.collection(ALARMS_COLLECTION)
                .document(alarmId)
                .update("isEnabled", isEnabled)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAlarm(alarmId: String): Result<Unit> {
        return try {
            db.collection(ALARMS_COLLECTION).document(alarmId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    const val TIMERS_COLLECTION = "timer_sets"

    suspend fun saveTimerSet(timerSet: TimerSet): Result<String> {
        return try {
            val timerId = if (timerSet.id.isEmpty()) {
                db.collection(TIMERS_COLLECTION).document().id
            } else {
                timerSet.id
            }

            val timerWithId = timerSet.copy(id = timerId)

            db.collection(TIMERS_COLLECTION)
                .document(timerId)
                .set(timerWithId.toMap())
                .await()

            Result.success(timerId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTimerSets(userId: String): Result<List<TimerSet>> {
        return try {
            val snapshot = db.collection(TIMERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val timers = snapshot.documents.mapNotNull { it.toObject(TimerSet::class.java) }
            Result.success(timers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTimerSet(timerId: String): Result<Unit> {
        return try {
            db.collection(TIMERS_COLLECTION).document(timerId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Deadline Functions
    suspend fun saveDeadline(deadline: Deadline): Result<String> {
        return try {
            val deadlineId = if (deadline.id.isEmpty()) {
                db.collection(DEADLINES_COLLECTION).document().id
            } else {
                deadline.id
            }

            val deadlineWithId = deadline.copy(id = deadlineId)

            // THIS SAVES TO FIREBASE FIRESTORE DATABASE!
            db.collection(DEADLINES_COLLECTION)
                .document(deadlineId)
                .set(deadlineWithId.toMap())
                .await()

            Result.success(deadlineId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeadlines(userId: String, filter: String = "All"): Result<List<Deadline>> {
        return try {
            var query = db.collection(DEADLINES_COLLECTION)
                .whereEqualTo("userId", userId)

            query = when (filter) {
                "Nearest Deadline" -> query.orderBy("dateTimeInMillis", Query.Direction.ASCENDING)
                "Farthest" -> query.orderBy("dateTimeInMillis", Query.Direction.DESCENDING)
                "By Name" -> query.orderBy("title", Query.Direction.ASCENDING)
                else -> query.orderBy("createdAt", Query.Direction.DESCENDING)
            }

            val snapshot = query.get().await()
            val deadlines = snapshot.documents.mapNotNull { it.toObject(Deadline::class.java) }
            Result.success(deadlines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDeadline(deadlineId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            db.collection(DEADLINES_COLLECTION)
                .document(deadlineId)
                .update("isCompleted", isCompleted)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDeadline(deadlineId: String): Result<Unit> {
        return try {
            db.collection(DEADLINES_COLLECTION).document(deadlineId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Activity Functions
    suspend fun saveActivity(activity: Activity): Result<String> {
        return try {
            val activityId = if (activity.id.isEmpty()) {
                db.collection(ACTIVITIES_COLLECTION).document().id
            } else {
                activity.id
            }

            val activityWithId = activity.copy(id = activityId)
            db.collection(ACTIVITIES_COLLECTION).document(activityId).set(activityWithId.toMap()).await()

            Result.success(activityId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActivities(deadlineId: String): Result<List<Activity>> {
        return try {
            val snapshot = db.collection(ACTIVITIES_COLLECTION)
                .whereEqualTo("deadlineId", deadlineId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            val activities = snapshot.documents.mapNotNull { it.toObject(Activity::class.java) }
            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateActivity(activityId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            db.collection(ACTIVITIES_COLLECTION)
                .document(activityId)
                .update("isCompleted", isCompleted)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteActivity(activityId: String): Result<Unit> {
        return try {
            db.collection(ACTIVITIES_COLLECTION).document(activityId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}