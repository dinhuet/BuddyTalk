package com.example.buddytalk.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["ref"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ref")]
)
data class Lesson(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ref: Long,
    val letter: String,
    val word: String,
    val isWordLesson: Int = 1
)