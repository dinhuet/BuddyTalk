package com.example.buddytalk.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.UserEntity
import com.example.buddytalk.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        
        viewModelScope.launch {
            repository.getUser().collect {
                if (it == null) {
                    // Initial data if DB is empty
                    val defaultUser = UserEntity(
                        userName = "Anonymous",
                        level = 19,
                        rank = "Hạng VI",
                        streak = 17
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
}