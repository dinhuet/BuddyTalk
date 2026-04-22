package com.example.buddytalk.data.dao

import androidx.room.*
import com.example.buddytalk.data.entity.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getTopicById(id: Long): Topic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)
}
