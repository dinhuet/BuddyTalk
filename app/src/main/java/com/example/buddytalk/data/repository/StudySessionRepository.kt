package com.example.buddytalk.data.repository

import com.example.buddytalk.data.dao.StudySessionDao
import com.example.buddytalk.data.entity.StudySession

class StudySessionRepository(private val dao: StudySessionDao) {
    suspend fun insert(session: StudySession) = dao.insert(session)

    suspend fun getDailyCounts(fromTimestamp: Long) = dao.getDailyCounts(fromTimestamp)
}
