package com.example.buddytalk.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.buddytalk.data.entity.UserEntity
import com.example.buddytalk.data.entity.XPTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table WHERE id = 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // XP Transactions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXPTransaction(transaction: XPTransaction)

    @Query("SELECT * FROM xp_transactions WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    suspend fun getTransaction(userId: Int, lessonId: Long): XPTransaction?

    @Transaction
    suspend fun completeLessonWithXP(user: UserEntity, transaction: XPTransaction) {
        insertUser(user)
        insertXPTransaction(transaction)
    }
}
