package com.example.buddytalk.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.buddytalk.data.dao.LessonDao
import com.example.buddytalk.data.dao.TopicDao
import com.example.buddytalk.data.dao.UserDao
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.entity.Topic
import com.example.buddytalk.data.entity.UserEntity

@Database(entities = [UserEntity::class, Topic::class, Lesson::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun topicDao(): TopicDao
    abstract fun lessonDao(): LessonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buddy_talk_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
