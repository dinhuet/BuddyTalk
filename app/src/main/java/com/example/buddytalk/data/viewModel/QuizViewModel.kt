package com.example.buddytalk.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.repository.LessonRepository
import com.example.buddytalk.data.repository.TopicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val lessons: List<Lesson> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository: LessonRepository
    private val topicRepository: TopicRepository

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        lessonRepository = LessonRepository(database.lessonDao())
        topicRepository = TopicRepository(database.topicDao())
    }

    fun loadLessons(targetType: Int) {
        viewModelScope.launch {
            combine(
                topicRepository.allTopics,
                lessonRepository.getAllLessons()
            ) { topics, lessons ->
                val unlockedTopicIds = topics
                    .filter { !it.isLocked }
                    .map { it.id }
                    .toSet()

                lessons
                    .filter { it.ref in unlockedTopicIds && it.isWordLesson == targetType }
                    .shuffled()
            }
                .collect { filteredLessons ->
                    _uiState.update {
                        it.copy(
                            lessons = filteredLessons,
                            currentIndex = 0,
                            isLoading = false,
                            isFinished = false
                        )
                    }
                }
        }
    }

    fun nextQuestion() {
        _uiState.update { state ->
            if (state.currentIndex < state.lessons.size - 1) {
                state.copy(currentIndex = state.currentIndex + 1)
            } else {
                state.copy(isFinished = true)
            }
        }
    }

    fun resetFinish() {
        _uiState.update { it.copy(isFinished = false) }
    }
}
