package com.example.buddytalk.data.repository

import com.example.buddytalk.data.dao.LessonDao
import com.example.buddytalk.data.entity.Lesson
import kotlinx.coroutines.flow.Flow

class LessonRepository(private val lessonDao: LessonDao) {
    fun getLessonsByTopicId(topicId: Long): Flow<List<Lesson>> = lessonDao.getLessonsByTopicId(topicId)

    suspend fun getLessonById(id: Long): Lesson? = lessonDao.getLessonById(id)

    suspend fun insertLesson(lesson: Lesson) = lessonDao.insertLesson(lesson)

    suspend fun updateLesson(lesson: Lesson) = lessonDao.updateLesson(lesson)

    suspend fun deleteLesson(lesson: Lesson) = lessonDao.deleteLesson(lesson)
}
