package com.example.buddytalk.data.repository

import com.example.buddytalk.data.dao.TopicDao
import com.example.buddytalk.data.entity.Topic
import kotlinx.coroutines.flow.Flow

class TopicRepository(private val topicDao: TopicDao) {
    val allTopics: Flow<List<Topic>> = topicDao.getAllTopics()

    suspend fun getTopicById(id: Long): Topic? = topicDao.getTopicById(id)

    suspend fun insertTopic(topic: Topic): Long = topicDao.insertTopic(topic)

    suspend fun updateTopic(topic: Topic) = topicDao.updateTopic(topic)

    suspend fun deleteTopic(topic: Topic) = topicDao.deleteTopic(topic)
}
