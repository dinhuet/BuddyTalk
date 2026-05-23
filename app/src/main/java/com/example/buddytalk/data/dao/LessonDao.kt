package com.example.buddytalk.data.dao

import androidx.room.*
import com.example.buddytalk.data.entity.Lesson
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE ref = :topicId")
    fun getLessonsByTopicId(topicId: Long): Flow<List<Lesson>>

    @Query("SELECT COUNT(*) FROM lessons WHERE ref = :topicId AND isWordLesson = :type")
    suspend fun getLessonCountByType(topicId: Long, type: Int): Int

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: Long): Lesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson)

    @Update
    suspend fun updateLesson(lesson: Lesson)

    @Delete
    suspend fun deleteLesson(lesson: Lesson)
}
