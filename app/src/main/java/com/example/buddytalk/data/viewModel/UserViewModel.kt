package com.example.buddytalk.data.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.UserEntity
import com.example.buddytalk.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val context = application.applicationContext

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        
        viewModelScope.launch {
            repository.getUser().collect {
                if (it == null) {
                    val defaultUser = UserEntity(
                        userName = "Anonymous",
                        level = 19,
                        rank = "Hạng VI",
                        streak = 17,
                        avatarUrl = null
                    )
                    repository.insertUser(defaultUser)
                } else {
                    _user.value = it
                }
            }
        }
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
                
                // Trả về đường dẫn tuyệt đối của file trong máy
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}