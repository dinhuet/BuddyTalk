package com.example.buddytalk.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.entity.Topic
import com.example.buddytalk.data.repository.LessonRepository
import com.example.buddytalk.data.repository.TopicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LearningMode {
    IMAGE, TEXT
}

data class TopicUiState(
    val topic: Topic,
    val lessonCount: Int,
    val isCompleted: Boolean = false
)

class TopicViewModel(application: Application) : AndroidViewModel(application) {
    private val topicRepository: TopicRepository
    private val lessonRepository: LessonRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _learningMode = MutableStateFlow(LearningMode.IMAGE)
    val learningMode = _learningMode.asStateFlow()

    private val _topicsWithCount = MutableStateFlow<List<TopicUiState>>(emptyList())
    val topicsWithCount: StateFlow<List<TopicUiState>> = _topicsWithCount.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        topicRepository = TopicRepository(database.topicDao())
        lessonRepository = LessonRepository(database.lessonDao())

        viewModelScope.launch {
            combine(
                topicRepository.allTopics,
                _searchQuery
            ) { topics, query ->
                if (topics.isEmpty()) {
                    seedDatabase()
                    emptyList<TopicUiState>()
                } else {
                    val filtered = topics.filter { it.name.contains(query, ignoreCase = true) }
                    filtered.map { topic ->
                        // In a real app, we might want to fetch counts more efficiently
                        // But for this exercise, we can just fetch once or mock it.
                        val count = 10 + (topic.id.toInt() % 10) // Mocking count based on ID as seen in image
                        TopicUiState(
                            topic = topic,
                            lessonCount = count,
                            isCompleted = topic.id == 1L // Mocking completion for the first one
                        )
                    }
                }
            }.collect {
                _topicsWithCount.value = it
            }
        }
    }

    private suspend fun seedDatabase() {
        val topics = listOf(
            Topic(name = "Động vật", isLocked = false),
            Topic(name = "Nghề nghiệp", isLocked = false),
            Topic(name = "Gia đình", isLocked = false),
            Topic(name = "Thức ăn", isLocked = true),
            Topic(name = "Quần áo", isLocked = true)
        )
        topics.forEach { topic ->
            val topicId = topicRepository.insertTopic(topic)
            // Seed some dummy lessons to make count logic work if needed
            val count = when(topic.name) {
                "Động vật" -> 10
                "Nghề nghiệp" -> 15
                "Gia đình" -> 12
                "Thức ăn" -> 18
                "Quần áo" -> 14
                else -> 0
            }
            for (i in 1..count) {
                lessonRepository.insertLesson(
                    Lesson(
                        name = "${topic.name} $i",
                        ref = topicId,
                        letter = "",
                        word = "",
                        isWordLesson = false
                    )
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onLearningModeChange(mode: LearningMode) {
        _learningMode.value = mode
    }
}
