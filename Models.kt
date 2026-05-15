package com.example.kavach

import androidx.room.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp

enum class RiskLevel { LOW, MEDIUM, HIGH, EXTREME }

data class PPEItem(
    val name: String,
    val isEquipped: Boolean = false,
    val description: String = "",
    val icon: ImageVector? = null
)

// Updated Task model for Firebase
data class SafetyTask(
    val id: String = "",
    val taskName: String = "",
    val category: String = "",
    val riskLevel: String = "LOW",
    val requiredPPE: List<String> = emptyList()
)

// Legacy Task for UI compatibility if needed
data class Task(
    val id: String,
    val name: String,
    val riskLevel: RiskLevel,
    val requiredPPE: List<String>
)

data class UserData(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val points: Int = 0,
    val streak: Int = 0,
    val safetyBadge: String = "Bronze"
)

data class UserStats(
    val name: String = "",
    val id: String = "",
    val department: String = "Operations",
    val score: Int = 0,
    val safeStreak: Int = 0,
    val rank: String = "Bronze",
    val profileImage: String? = null
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

@Entity(tableName = "incidents")
data class Incident(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val severity: String,
    val date: String,
    val time: String,
    val location: String
)

data class IncidentLog(
    val id: String = "",
    val userId: String = "",
    val description: String = "",
    val severity: String = "Low",
    val timestamp: Timestamp = Timestamp.now(),
    val location: String = ""
)
