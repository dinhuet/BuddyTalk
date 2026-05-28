package com.example.buddytalk.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String,
    val level: Int = 1,
    val experience: Int = 0,
    val maxExperience: Int = 100,
    val rank: String = "Bắt đầu",
    val streak: Int = 0,
    val lessonCount: Int = 0,
    val exerciseCount: Int = 0,
    val lastStudyDate: Long? = null,
    val avatarUrl: String? = null
)
