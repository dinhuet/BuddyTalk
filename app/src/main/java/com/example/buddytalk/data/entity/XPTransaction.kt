package com.example.buddytalk.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xp_transactions")
data class XPTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Int = 1,
    val lessonId: Long,
    val xpAmount: Int,
    val reason: String, // "NEW_LESSON" or "REVIEW"
    val createdAt: Long = System.currentTimeMillis()
)
