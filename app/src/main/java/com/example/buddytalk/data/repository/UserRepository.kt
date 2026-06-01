package com.example.buddytalk.data.repository

import com.example.buddytalk.data.dao.UserDao
import com.example.buddytalk.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    companion object {
        const val FIRST_TIME_LESSON_EXP = 50
        const val REPEATED_LESSON_EXP = 5
        const val FIRST_TIME_EXERCISE_EXP = 20
        const val REPEATED_EXERCISE_EXP = 10
    }

    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun updateUserName(newName: String, currentUser: UserEntity) {
        userDao.insertUser(currentUser.copy(userName = newName))
    }

    suspend fun updateAvatar(newUrl: String, currentUser: UserEntity) {
        userDao.insertUser(currentUser.copy(avatarUrl = newUrl))
    }

    suspend fun addExperience(currentUser: UserEntity, amount: Int) {
        var newExp = currentUser.experience + amount
        var newLevel = currentUser.level
        var newMaxExp = currentUser.maxExperience

        while (newExp >= newMaxExp && newLevel < 30) {
            newExp -= newMaxExp
            newLevel++
            newMaxExp = calculateMaxExp(newLevel)
        }
        
        // Nếu đã đạt max level thì không tăng exp nữa mà giữ ở mức max
        if (newLevel >= 30) {
            newExp = 0
            newLevel = 30
        }

        userDao.insertUser(currentUser.copy(
            experience = newExp,
            level = newLevel,
            maxExperience = newMaxExp,
            rank = getRankName(newLevel)
        ))
    }

    private fun calculateMaxExp(level: Int): Int {
        return level * 100 
    }

    private fun getRankName(level: Int): String {
        return when {
            level >= 30 -> "Thần tượng"
            level >= 25 -> "Huyền thoại"
            level >= 20 -> "Bậc thầy"
            level >= 15 -> "Chuyên gia"
            level >= 10 -> "Chiến binh"
            level >= 5 -> "Đồng hành"
            else -> "Tập sự"
        }
    }

    suspend fun updateUserAfterLesson(currentUser: UserEntity, newStreak: Int, lastStudyDate: Long, expAmount: Int = FIRST_TIME_LESSON_EXP) {
        val updatedUser = currentUser.copy(
            streak = newStreak,
            lessonCount = currentUser.lessonCount + 1,
            lastStudyDate = lastStudyDate
        )
        addExperience(updatedUser, expAmount)
    }

    suspend fun updateUserAfterExercise(currentUser: UserEntity, newStreak: Int, lastStudyDate: Long, expAmount: Int = FIRST_TIME_EXERCISE_EXP) {
        val updatedUser = currentUser.copy(
            streak = newStreak,
            exerciseCount = currentUser.exerciseCount + 1,
            lastStudyDate = lastStudyDate
        )
        addExperience(updatedUser, expAmount)
    }

    suspend fun incrementCounter(currentUser: UserEntity, isLesson: Boolean, expAmount: Int) {
        val updatedUser = currentUser.copy(
            lessonCount = if (isLesson) currentUser.lessonCount + 1 else currentUser.lessonCount,
            exerciseCount = if (!isLesson) currentUser.exerciseCount + 1 else currentUser.exerciseCount
        )
        addExperience(updatedUser, expAmount)
    }
}
