package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "client_profiles")
data class ClientProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val company: String,
    val email: String,
    val phone: String,
    val bio: String
)

@Entity(tableName = "project_tasks")
data class ProjectTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val clientProfileId: Int,
    val description: String,
    val status: String, // "Pending", "Completed"
    val projectChannel: String, // e.g. "Mobile App", "CRM Setup", "Marketing"
    val priority: String // "High", "Medium", "Low"
)

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientProfileId: Int?, // Linked client profile, if matched
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val transcript: String,
    val summary: String,
    val actionItemsRaw: String, // List of action items stored as delimiter-separated string or simple JSON
    val isEncrypted: Boolean = true, // Visual lock indicating private end-to-end local encryption
    val exportedStatus: String = "Local Only", // "Local Only", "Exported to CRM", "Exported to Jira"
    val companionNotes: String = "", // Custom notes added by user
    val sentiment: String = "Neutral",
    val highlightedPhrases: String = "",
    val tagsRaw: String = ""
) {
    val actionItemsList: List<String>
        get() = if (actionItemsRaw.isEmpty()) emptyList() else actionItemsRaw.split("||")

    val highlightedPhrasesList: List<String>
        get() = if (highlightedPhrases.isEmpty()) emptyList() else highlightedPhrases.split("||")

    val tagsList: List<String>
        get() = if (tagsRaw.isEmpty()) emptyList() else tagsRaw.split("||")
}

@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderNumber: String,
    val body: String,
    val urgencyScore: Int, // 1 to 10 scale
    val priorityLevel: String, // "Critical", "Urgent", "High", "Normal"
    val timestamp: Long = System.currentTimeMillis(),
    val linkedProjectId: Int?, // Associated project ID
    val categoryReasoning: String, // Explain why prioritized this way based on project channels
    val isRead: Boolean = false
)
