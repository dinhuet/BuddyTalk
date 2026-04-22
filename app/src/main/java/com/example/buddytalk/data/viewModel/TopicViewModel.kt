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

            val lessons = when (topic.name) {

                "Động vật" -> listOf(
                    Lesson(name = "bo", letter = "B", word = "Bò", ref = topicId, isWordLesson = true),
                    Lesson(name = "meo", letter = "M", word = "Mèo", ref = topicId, isWordLesson = true),
                    Lesson(name = "cho", letter = "C", word = "Chó", ref = topicId, isWordLesson = true),
                    Lesson(name = "ga", letter = "G", word = "Gà", ref = topicId, isWordLesson = true),
                    Lesson(name = "vit", letter = "V", word = "Vịt", ref = topicId, isWordLesson = true)
                )

                "Nghề nghiệp" -> listOf(
                    Lesson(name = "bacsi", letter = "B", word = "Bác sĩ", ref = topicId, isWordLesson = true),
                    Lesson(name = "giaovien", letter = "G", word = "Giáo viên", ref = topicId, isWordLesson = true),
                    Lesson(name = "congan", letter = "C", word = "Công an", ref = topicId, isWordLesson = true),
                    Lesson(name = "taixe", letter = "T", word = "Tài xế", ref = topicId, isWordLesson = true)
                )

                "Gia đình" -> listOf(
                    Lesson(name = "bo", letter = "B", word = "Bố", ref = topicId, isWordLesson = true),
                    Lesson(name = "me", letter = "M", word = "Mẹ", ref = topicId, isWordLesson = true),
                    Lesson(name = "anh", letter = "A", word = "Anh", ref = topicId, isWordLesson = true),
                    Lesson(name = "chi", letter = "C", word = "Chị", ref = topicId, isWordLesson = true)
                )

                "Thức ăn" -> listOf(
                    Lesson(name = "com", letter = "C", word = "Cơm", ref = topicId, isWordLesson = true),
                    Lesson(name = "pho", letter = "P", word = "Phở", ref = topicId, isWordLesson = true),
                    Lesson(name = "banhmi", letter = "B", word = "Bánh mì", ref = topicId, isWordLesson = true),
                    Lesson(name = "sua", letter = "S", word = "Sữa", ref = topicId, isWordLesson = true)
                )

                "Quần áo" -> listOf(
                    Lesson(name = "ao", letter = "A", word = "Áo", ref = topicId, isWordLesson = true),
                    Lesson(name = "quan", letter = "Q", word = "Quần", ref = topicId, isWordLesson = true),
                    Lesson(name = "mu", letter = "M", word = "Mũ", ref = topicId, isWordLesson = true),
                    Lesson(name = "giay", letter = "G", word = "Giày", ref = topicId, isWordLesson = true)
                )
                else -> emptyList()
            }
            lessons.forEach { lesson ->
                lessonRepository.insertLesson(lesson)
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
