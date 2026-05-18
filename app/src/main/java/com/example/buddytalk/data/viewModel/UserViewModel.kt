package com.example.buddytalk.data.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.StudySession
import com.example.buddytalk.data.entity.UserEntity
import com.example.buddytalk.data.entity.XPTransaction
import com.example.buddytalk.data.model.LevelSystem
import com.example.buddytalk.data.repository.StudySessionRepository
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

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val studySessionRepository: StudySessionRepository
    private val context = application.applicationContext

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _streakUpdatedEvent = MutableSharedFlow<Int>()
    val streakUpdatedEvent: SharedFlow<Int> = _streakUpdatedEvent.asSharedFlow()

    data class XPResult(val xpGained: Int, val isLevelUp: Boolean, val newLevel: Int)
    private val _xpGainedEvent = MutableSharedFlow<XPResult>()
    val xpGainedEvent: SharedFlow<XPResult> = _xpGainedEvent.asSharedFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        val userDao = db.userDao()
        repository = UserRepository(userDao)
        studySessionRepository = StudySessionRepository(db.studySessionDao())
        
        viewModelScope.launch {
            repository.getUser().collect {
                if (it == null) {
                    val defaultUser = UserEntity(
                        userName = "Anonymous",
                        level = 1,
                        totalXP = 0,
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

    /**
     * @param silent If true, don't emit xpGainedEvent (used for batch card completion)
     */
    fun completeLesson(lessonId: Long, isNewLesson: Boolean, silent: Boolean = false) {
        viewModelScope.launch {
            val currentUser = _user.value ?: return@launch
            if (repository.hasReceivedXP(lessonId)) return@launch

            val xpGained = if (isNewLesson) 200 else 50
            val newTotalXP = currentUser.totalXP + xpGained
            
            val levelInfo = LevelSystem.getLevelByXP(newTotalXP)
            val isLevelUp = levelInfo.level > currentUser.level

            val now = System.currentTimeMillis()

            studySessionRepository.insert(StudySession(timestamp = now))

            val lastStudy = currentUser.lastStudyDate

            if (lastStudy == null) {
                repository.updateUserAfterLesson(currentUser, 1, now)
                _streakUpdatedEvent.emit(1)
            } else {
                val lastCalendar = Calendar.getInstance().apply { timeInMillis = lastStudy }
                val nowCalendar = Calendar.getInstance().apply { timeInMillis = now }

                if (isSameDay(lastCalendar, nowCalendar)) {
                    return@launch
                }
            }

                val newStreak = if (isYesterday(lastCalendar, nowCalendar)) {
                    currentUser.streak + 1
                } else {
                    1
                }
                repository.updateUserAfterLesson(currentUser, newStreak, now)
                _streakUpdatedEvent.emit(newStreak)
            }
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
            _user.value?.let { repository.updateUserName(newName, it) }
        }
    }

    fun updateAvatar(uriString: String) {
        viewModelScope.launch {
            val localPath = saveImageToInternalStorage(uriString)
            if (localPath != null) {
                _user.value?.let { repository.updateAvatar(localPath, it) }
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
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
