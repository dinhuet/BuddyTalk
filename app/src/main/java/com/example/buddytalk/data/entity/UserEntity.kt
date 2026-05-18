package com.example.buddytalk.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String,
    val level: Int,
    val totalXP: Int = 0, // Added totalXP
    val rank: String,
    val streak: Int,
    val lessonCount: Int = 0,
    val lastStudyDate: Long? = null,
    val avatarUrl: String? = null
)
