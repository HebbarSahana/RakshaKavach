package com.example.kavach

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    fun getUserData(uid: String): Flow<UserData?> {
        return db.collection("users").document(uid).snapshots().map { snapshot ->
            snapshot.toObject<UserData>()
        }
    }

    suspend fun getSafetyTasks(): List<SafetyTask> {
        return try {
            val snapshot = db.collection("tasks").get().await()
            snapshot.documents.mapNotNull { it.toObject<SafetyTask>()?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveUserData(userData: UserData) {
        try {
            db.collection("users").document(userData.uid).set(userData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateUserProfile(uid: String, name: String, department: String, profileImage: String?) {
        try {
            db.collection("users").document(uid).update(
                mapOf(
                    "name" to name,
                    "department" to department,
                    "profileImage" to profileImage
                )
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveIncidentLog(log: IncidentLog) {
        try {
            db.collection("incident_logs").add(log).await()
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun updateUserStats(uid: String, pointsDelta: Int, streakUpdate: Boolean) {
        try {
            val userRef = db.collection("users").document(uid)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentPoints = snapshot.getLong("points") ?: 0
                val currentStreak = snapshot.getLong("streak") ?: 0
                
                transaction.update(userRef, "points", currentPoints + pointsDelta)
                if (streakUpdate) {
                    transaction.update(userRef, "streak", currentStreak + 1)
                }
            }.await()
        } catch (e: Exception) {
            // Log error
        }
    }
}
