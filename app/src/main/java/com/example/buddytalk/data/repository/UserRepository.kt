package com.example.buddytalk.data.repository

import com.example.buddytalk.data.dao.UserDao
import com.example.buddytalk.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
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

    suspend fun updateStreak(currentUser: UserEntity, newStreak: Int, lastStudyDate: Long) {
        userDao.insertUser(currentUser.copy(streak = newStreak, lastStudyDate = lastStudyDate))
    }
}