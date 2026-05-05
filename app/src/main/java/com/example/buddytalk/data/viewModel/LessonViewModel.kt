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
    val mispronouncedIndices: Set<Int> = emptySet(),
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
        if (currentLesson == null) return

        val targetSentence = currentLesson.word.lowercase(Locale.ROOT).trim()
        val recognizedSentence = text.lowercase(Locale.ROOT).trim()

        // Clean punctuation for comparison
        val regex = Regex("[^a-z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹ\\s]")
        val targetWords = targetSentence.split(Regex("\\s+"))
        val recognizedWords = recognizedSentence.split(Regex("\\s+")).map { it.replace(regex, "") }

        val mispronouncedIndices = mutableSetOf<Int>()
        targetWords.forEachIndexed { index, word ->
            val cleanedTarget = word.replace(regex, "")
            if (cleanedTarget.isNotEmpty()) {
                val isFound = recognizedWords.any { it == cleanedTarget }
                if (!isFound) {
                    mispronouncedIndices.add(index)
                }
            }
        }

        val isMatch = mispronouncedIndices.isEmpty() && recognizedSentence.isNotEmpty()
        
        _uiState.update { state ->
            state.copy(
                recognizedText = text,
                partialText = "",
                isCorrect = isMatch,
                mispronouncedIndices = if (text.isEmpty()) emptySet() else mispronouncedIndices,
                completedIndices = if (isMatch) state.completedIndices + state.currentIndex else state.completedIndices
            )
        }
    }

    fun startListening() {
        if (_uiState.value.isModelLoaded) {
            _uiState.update { it.copy(isListening = true, recognizedText = "", partialText = "", isCorrect = false, mispronouncedIndices = emptySet(), errorMessage = null) }
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
        _uiState.update { it.copy(recognizedText = "", partialText = "", isCorrect = false, mispronouncedIndices = emptySet(), errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun loadLessons(topicId: Long, mode: String) {
        viewModelScope.launch {
            lessonRepository.getLessonsByTopicId(topicId)
                .map { allLessons ->
                    allLessons.filter { lesson ->
                        when (mode) {
                            "TEXT" -> lesson.isWordLesson == 1
                            "SENTENCE" -> lesson.isWordLesson == 3
                            else -> lesson.isWordLesson == 0
                        }
                    }
                }
                .collect { filteredLessons ->
                    _uiState.update { it.copy(
                        lessons = filteredLessons,
                        isLoading = false,
                        currentIndex = 0,
                        isFinished = false,
                        completedIndices = emptySet(),
                        mispronouncedIndices = emptySet(),
                        errorMessage = null
                    ) }
                }
        }
    }

    fun nextLesson() {
        _uiState.update { state ->
            if (state.currentIndex < state.lessons.size - 1) {
                state.copy(
                    currentIndex = state.currentIndex + 1,
                    recognizedText = "",
                    partialText = "",
                    isCorrect = false,
                    mispronouncedIndices = emptySet(),
                    errorMessage = null
                )
            } else {
                // At last lesson: check if all are completed
                if (state.completedIndices.size < state.lessons.size) {
                    state.copy(errorMessage = "Bé hãy hoàn thành các câu trước nhé!")
                } else {
                    state.copy(isFinished = true, errorMessage = null)
                }
            }
        }
    }

    fun previousLesson() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                state.copy(currentIndex = state.currentIndex - 1, recognizedText = "", partialText = "", isCorrect = false, mispronouncedIndices = emptySet(), errorMessage = null)
            } else {
                state
            }
        }
    }
    
    fun setCurrentIndex(index: Int) {
        _uiState.update { state ->
            if (index in state.lessons.indices) {
                state.copy(currentIndex = index, recognizedText = "", partialText = "", isCorrect = false, mispronouncedIndices = emptySet(), errorMessage = null)
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
