package com.example.buddytalk.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isLocked: Boolean = false,
    val isCompleted: Boolean = false
)