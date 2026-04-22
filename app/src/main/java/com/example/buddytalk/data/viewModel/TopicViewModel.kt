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
    IMAGE, TEXT, SENTENCE, VOCABULARY
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
                        val count = 10 + (topic.id.toInt() % 10)
                        TopicUiState(
                            topic = topic,
                            lessonCount = count,
                            isCompleted = topic.id == 1L
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

                    Lesson(name = "con_bo", letter = "", word = "con bò", ref = topicId, isWordLesson = false),
                    Lesson(name = "con_meo", letter = "", word = "con mèo", ref = topicId, isWordLesson = false),
                    Lesson(name = "con_cho", letter = "", word = "con chó", ref = topicId, isWordLesson = false),
                    Lesson(name = "con_ga", letter = "", word = "con gà", ref = topicId, isWordLesson = false)
                )

                "Nghề nghiệp" -> listOf(
                    Lesson(name = "bacsi", letter = "B", word = "Bác sĩ", ref = topicId, isWordLesson = true),
                    Lesson(name = "giaovien", letter = "G", word = "Giáo viên", ref = topicId, isWordLesson = true),
                    Lesson(name = "kysu", letter = "K", word = "Kỹ sư", ref = topicId, isWordLesson = true),
                    Lesson(name = "congnhan", letter = "C", word = "Công nhân", ref = topicId, isWordLesson = true),

                    Lesson(name = "anh_bacsi", letter = "", word = "bác sĩ", ref = topicId, isWordLesson = false),
                    Lesson(name = "anh_giaovien", letter = "", word = "giáo viên", ref = topicId, isWordLesson = false),
                    Lesson(name = "anh_kysu", letter = "", word = "kỹ sư", ref = topicId, isWordLesson = false),
                    Lesson(name = "anh_congnhan", letter = "", word = "công nhân", ref = topicId, isWordLesson = false)
                )

                "Gia đình" -> listOf(
                    Lesson(name = "bo", letter = "B", word = "Bố", ref = topicId, isWordLesson = true),
                    Lesson(name = "me", letter = "M", word = "Mẹ", ref = topicId, isWordLesson = true),
                    Lesson(name = "anh", letter = "A", word = "Anh", ref = topicId, isWordLesson = true),
                    Lesson(name = "chi", letter = "C", word = "Chị", ref = topicId, isWordLesson = true),

                    Lesson(name = "gd_bo", letter = "", word = "bố", ref = topicId, isWordLesson = false),
                    Lesson(name = "gd_me", letter = "", word = "mẹ", ref = topicId, isWordLesson = false),
                    Lesson(name = "gd_anh", letter = "", word = "anh", ref = topicId, isWordLesson = false),
                    Lesson(name = "gd_chi", letter = "", word = "chị", ref = topicId, isWordLesson = false)
                )

                "Thức ăn" -> listOf(
                    Lesson(name = "com", letter = "C", word = "Cơm", ref = topicId, isWordLesson = true),
                    Lesson(name = "pho", letter = "P", word = "Phở", ref = topicId, isWordLesson = true),
                    Lesson(name = "banhmi", letter = "B", word = "Bánh mì", ref = topicId, isWordLesson = true),
                    Lesson(name = "trung", letter = "T", word = "Trứng", ref = topicId, isWordLesson = true),

                    Lesson(name = "food_com", letter = "", word = "cơm", ref = topicId, isWordLesson = false),
                    Lesson(name = "food_pho", letter = "", word = "phở", ref = topicId, isWordLesson = false),
                    Lesson(name = "food_banhmi", letter = "", word = "bánh mì", ref = topicId, isWordLesson = false),
                    Lesson(name = "food_trung", letter = "", word = "trứng", ref = topicId, isWordLesson = false)
                )

                "Quần áo" -> listOf(
                    Lesson(name = "ao", letter = "A", word = "Áo", ref = topicId, isWordLesson = true),
                    Lesson(name = "quan", letter = "Q", word = "Quần", ref = topicId, isWordLesson = true),
                    Lesson(name = "vay", letter = "V", word = "Váy", ref = topicId, isWordLesson = true),
                    Lesson(name = "giay", letter = "G", word = "Giày", ref = topicId, isWordLesson = true),

                    Lesson(name = "cloth_ao", letter = "", word = "áo", ref = topicId, isWordLesson = false),
                    Lesson(name = "cloth_quan", letter = "", word = "quần", ref = topicId, isWordLesson = false),
                    Lesson(name = "cloth_vay", letter = "", word = "váy", ref = topicId, isWordLesson = false),
                    Lesson(name = "cloth_giay", letter = "", word = "giày", ref = topicId, isWordLesson = false)
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
