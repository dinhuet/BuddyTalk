package com.example.buddytalk.data.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.StudySession
import com.example.buddytalk.data.entity.UserEntity
import com.example.buddytalk.data.repository.StudySessionRepository
import com.example.buddytalk.data.repository.TopicRepository
import com.example.buddytalk.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import kotlin.math.max

data class AchievementEvent(
    val type: AchievementType,
    val title: String,
    val message: String
)

enum class AchievementType {
    LEVEL_UP,
    BADGE_UP
}

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val studySessionRepository: StudySessionRepository
    private val topicRepository: TopicRepository
    private val context = application.applicationContext

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _streakUpdatedEvent = MutableSharedFlow<Int>()
    val streakUpdatedEvent: SharedFlow<Int> = _streakUpdatedEvent.asSharedFlow()

    private val _achievementEvent = MutableSharedFlow<AchievementEvent>()
    val achievementEvent: SharedFlow<AchievementEvent> = _achievementEvent.asSharedFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        val userDao = db.userDao()
        repository = UserRepository(userDao)
        studySessionRepository = StudySessionRepository(db.studySessionDao())
        topicRepository = TopicRepository(db.topicDao())
        
        viewModelScope.launch {
            repository.getUser().collect {
                if (it == null) {
                    val defaultUser = UserEntity(
                        userName = "Anonymous",
                        level = 1,
                        rank = "Hạng I",
                        streak = 0,
                        lastStudyDate = null,
                        avatarUrl = null
                    )
                    repository.insertUser(defaultUser)
                } else {
                    _user.value = it
                }
            }
        }
    }

    fun completeLesson(topicId: Long? = null) {
        completeActivity(isLesson = true, topicId = topicId)
    }

    fun completeExercise(topicId: Long? = null) {
        completeActivity(isLesson = false, topicId = topicId)
    }

    private fun completeActivity(isLesson: Boolean, topicId: Long? = null) {
        viewModelScope.launch {
            val currentUser = _user.value ?: return@launch
            val now = System.currentTimeMillis()
            val wasCompletedBefore = topicId?.let { topicRepository.isTopicCompleted(it) } == true
            val expAmount = when {
                isLesson && !wasCompletedBefore -> UserRepository.FIRST_TIME_LESSON_EXP
                isLesson -> UserRepository.REPEATED_LESSON_EXP
                !isLesson && !wasCompletedBefore -> UserRepository.FIRST_TIME_EXERCISE_EXP
                else -> UserRepository.REPEATED_EXERCISE_EXP
            }

            studySessionRepository.insert(StudySession(timestamp = now))

            topicId?.let { topicRepository.markTopicCompleted(it) }

            val lastStudy = currentUser.lastStudyDate

            if (lastStudy == null) {
                if (isLesson) {
                    repository.updateUserAfterLesson(currentUser, 1, now, expAmount)
                } else {
                    repository.updateUserAfterExercise(currentUser, 1, now, expAmount)
                }
                emitAchievementEvents(currentUser, expAmount)
                _streakUpdatedEvent.emit(1)
            } else {
                val lastCalendar = Calendar.getInstance().apply { timeInMillis = lastStudy }
                val nowCalendar = Calendar.getInstance().apply { timeInMillis = now }

                if (isSameDay(lastCalendar, nowCalendar)) {
                    repository.incrementCounter(currentUser, isLesson, expAmount)
                    emitAchievementEvents(currentUser, expAmount)
                    return@launch
                }

                val newStreak = if (isYesterday(lastCalendar, nowCalendar)) {
                    currentUser.streak + 1
                } else {
                    1
                }
                if (isLesson) {
                    repository.updateUserAfterLesson(currentUser, newStreak, now, expAmount)
                } else {
                    repository.updateUserAfterExercise(currentUser, newStreak, now, expAmount)
                }
                emitAchievementEvents(currentUser, expAmount)
                _streakUpdatedEvent.emit(newStreak)
            }
        }
    }

    private suspend fun emitAchievementEvents(previousUser: UserEntity, gainedExp: Int) {
        val levelResult = simulateLevelProgress(previousUser, gainedExp)
        if (levelResult.newLevel > previousUser.level) {
            _achievementEvent.emit(
                AchievementEvent(
                    type = AchievementType.LEVEL_UP,
                    title = "Chúc mừng lên cấp!",
                    message = "Bé đã lên cấp ${levelResult.newLevel}!"
                )
            )
        }
        if (levelResult.newRank != previousUser.rank) {
            _achievementEvent.emit(
                AchievementEvent(
                    type = AchievementType.BADGE_UP,
                    title = "Chúc mừng nhận badge mới!",
                    message = "Bé đã đạt danh hiệu ${levelResult.newRank}!"
                )
            )
        }
    }

    private data class LevelProgressResult(
        val newLevel: Int,
        val newRank: String
    )

    private fun simulateLevelProgress(user: UserEntity, gainedExp: Int): LevelProgressResult {
        var newExp = user.experience + gainedExp
        var newLevel = user.level
        var newMaxExp = user.maxExperience

        while (newExp >= newMaxExp && newLevel < 30) {
            newExp -= newMaxExp
            newLevel++
            newMaxExp = calculateMaxExp(newLevel)
        }

        if (newLevel >= 30) {
            newLevel = 30
        }

        return LevelProgressResult(
            newLevel = newLevel,
            newRank = getRankName(newLevel)
        )
    }

    private fun calculateMaxExp(level: Int): Int = max(level, 1) * 100

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

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(last: Calendar, now: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(last, yesterday)
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            _user.value?.let {
                repository.updateUserName(newName, it)
            }
        }
    }

    fun updateAvatar(uriString: String) {
        viewModelScope.launch {
            val localPath = saveImageToInternalStorage(uriString)
            if (localPath != null) {
                _user.value?.let {
                    repository.updateAvatar(localPath, it)
                }
            }
        }
    }

    private suspend fun saveImageToInternalStorage(uriString: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "user_avatar_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                
                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}