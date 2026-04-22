package com.example.buddytalk.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.repository.LessonRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LessonUiState(
    val lessons: List<Lesson> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false
)

class LessonViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository: LessonRepository
    
    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        lessonRepository = LessonRepository(database.lessonDao())
    }

    fun loadLessons(topicId: Long, mode: String) {
        viewModelScope.launch {
            lessonRepository.getLessonsByTopicId(topicId)
                .map { allLessons ->
                    allLessons.filter { lesson ->
                        if (mode == "TEXT") lesson.isWordLesson else !lesson.isWordLesson
                    }
                }
                .collect { filteredLessons ->
                    _uiState.update { it.copy(
                        lessons = filteredLessons,
                        isLoading = false,
                        currentIndex = 0,
                        isFinished = false
                    ) }
                }
        }
    }

    fun nextLesson() {
        _uiState.update { state ->
            if (state.currentIndex < state.lessons.size - 1) {
                state.copy(currentIndex = state.currentIndex + 1)
            } else {
                state.copy(isFinished = true)
            }
        }
    }

    fun previousLesson() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                state.copy(currentIndex = state.currentIndex - 1)
            } else {
                state
            }
        }
    }
    
    fun resetFinish() {
        _uiState.update { it.copy(isFinished = false) }
    }
}
