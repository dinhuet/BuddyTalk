package com.example.buddytalk.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.repository.LessonRepository
import com.example.buddytalk.data.vosk.VoskManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

data class LessonUiState(
    val lessons: List<Lesson> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val isModelLoaded: Boolean = false,
    val recognizedText: String = "",
    val partialText: String = "",
    val isListening: Boolean = false,
    val isCorrect: Boolean = false,
    val completedIndices: Set<Int> = emptySet(),
    val errorMessage: String? = null
)

class LessonViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository: LessonRepository
    private val voskManager = VoskManager(application)
    
    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        lessonRepository = LessonRepository(database.lessonDao())
        
        // Initialize Vosk Model
        initVoskModel("model-vn")
    }

    private fun initVoskModel(modelPath: String) {
        voskManager.initModel(modelPath, object : VoskManager.VoskCallback {
            override fun onResult(text: String) {
                if (text.isNotEmpty()) {
                    handleRecognitionResult(text)
                }
            }

            override fun onPartialResult(text: String) {
                _uiState.update { it.copy(partialText = text) }
            }

            override fun onError(e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }

            override fun onModelLoaded() {
                _uiState.update { it.copy(isModelLoaded = true) }
            }
        })
    }

    private fun handleRecognitionResult(text: String) {
        val currentLesson = _uiState.value.lessons.getOrNull(_uiState.value.currentIndex)
        val targetWord = currentLesson?.word?.lowercase(Locale.ROOT)?.trim() ?: ""
        val recognizedWord = text.lowercase(Locale.ROOT).trim()

        // Simple comparison logic
        val isMatch = recognizedWord.contains(targetWord) || targetWord.contains(recognizedWord)
        
        _uiState.update { state ->
            state.copy(
                recognizedText = text,
                partialText = "",
                isCorrect = isMatch,
                completedIndices = if (isMatch) state.completedIndices + state.currentIndex else state.completedIndices
            )
        }
    }

    fun startListening() {
        if (_uiState.value.isModelLoaded) {
            _uiState.update { it.copy(isListening = true, recognizedText = "", partialText = "", isCorrect = false) }
            voskManager.startListening(object : VoskManager.VoskCallback {
                override fun onResult(text: String) {
                    if (text.isNotEmpty()) {
                        handleRecognitionResult(text)
                    }
                }
                override fun onPartialResult(text: String) {
                    _uiState.update { it.copy(partialText = text) }
                }
                override fun onError(e: Exception) {
                    _uiState.update { it.copy(errorMessage = e.message, isListening = false) }
                }
                override fun onModelLoaded() {}
            })
        }
    }

    fun stopListening() {
        voskManager.stopListening()
        _uiState.update { it.copy(isListening = false) }
    }

    fun resetPractice() {
        _uiState.update { it.copy(recognizedText = "", partialText = "", isCorrect = false) }
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
                        isFinished = false,
                        completedIndices = emptySet()
                    ) }
                }
        }
    }

    fun nextLesson() {
        _uiState.update { state ->
            if (state.currentIndex < state.lessons.size - 1) {
                state.copy(currentIndex = state.currentIndex + 1, recognizedText = "", partialText = "", isCorrect = false)
            } else {
                state.copy(isFinished = true)
            }
        }
    }

    fun previousLesson() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                state.copy(currentIndex = state.currentIndex - 1, recognizedText = "", partialText = "", isCorrect = false)
            } else {
                state
            }
        }
    }
    
    fun setCurrentIndex(index: Int) {
        _uiState.update { state ->
            if (index in state.lessons.indices) {
                state.copy(currentIndex = index, recognizedText = "", partialText = "", isCorrect = false)
            } else {
                state
            }
        }
    }
    
    fun resetFinish() {
        _uiState.update { it.copy(isFinished = false) }
    }

    override fun onCleared() {
        super.onCleared()
        voskManager.release()
    }
}
