package com.example.buddytalk.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddytalk.data.database.AppDatabase
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.entity.Topic
import com.example.buddytalk.data.repository.LessonRepository
import com.example.buddytalk.data.repository.TopicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LearningMode {
    IMAGE,
    TEXT,
    SENTENCE,
    VOCABULARY,
    QUIZ_MATCH_IMAGE,
    QUIZ_AUDIO_WORD,
    QUIZ_AUDIO_IMAGE
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

        observeTopics()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTopics() {
        // Kết hợp chế độ học, tìm kiếm và danh sách topic để tính toán số câu chính xác
        _learningMode
            .flatMapLatest { mode ->
                val targetType = when (mode) {
                    LearningMode.IMAGE -> 0
                    LearningMode.TEXT -> 1
                    LearningMode.SENTENCE -> 3
                    LearningMode.VOCABULARY -> 1
                    LearningMode.QUIZ_MATCH_IMAGE -> 0
                    LearningMode.QUIZ_AUDIO_WORD -> 1
                    LearningMode.QUIZ_AUDIO_IMAGE -> 0
                }
                
                combine(
                    topicRepository.allTopics,
                    _searchQuery
                ) { topics, query ->
                    if (topics.isEmpty()) {
                        seedDatabase()
                        emptyList<Topic>()
                    } else {
                        topics.filter { it.name.contains(query, ignoreCase = true) }
                    }
                }.flatMapLatest { filteredTopics ->
                    if (filteredTopics.isEmpty()) {
                        flowOf(emptyList<TopicUiState>())
                    } else {
                        // Với mỗi topic, ta quan sát Flow danh sách bài học để đếm số câu theo mode
                        val topicUiStateFlows = filteredTopics.map { topic ->
                            lessonRepository.getLessonsByTopicId(topic.id).map { lessons ->
                                TopicUiState(
                                    topic = topic,
                                    lessonCount = lessons.count { it.isWordLesson == targetType },
                                    isCompleted = false // Có thể thêm logic kiểm tra hoàn thành ở đây
                                )
                            }
                        }
                        combine(topicUiStateFlows) { it.toList() }
                    }
                }
            }
            .onEach { _topicsWithCount.value = it }
            .launchIn(viewModelScope)
    }

    private suspend fun seedDatabase() {
        val topics = listOf(
            Topic(name = "Động vật", isLocked = false),
            Topic(name = "Nghề nghiệp", isLocked = false),
            Topic(name = "Gia đình", isLocked = false),
            Topic(name = "Thức ăn", isLocked = true),
            Topic(name = "Quần áo", isLocked = true)
        )

        val normalLessons = mutableListOf<Lesson>()
        val sentenceLessons = mutableListOf<Lesson>()

        topics.forEach { topic ->
            val topicId = topicRepository.insertTopic(topic)

            val lessons = when (topic.name) {
                "Động vật" -> listOf(
                    Lesson(name = "bo", letter = "B", word = "Bò", ref = topicId, isWordLesson = 1),
                    Lesson(name = "meo", letter = "M", word = "Mèo", ref = topicId, isWordLesson = 1),
                    Lesson(name = "cho", letter = "C", word = "Chó", ref = topicId, isWordLesson = 1),
                    Lesson(name = "ga", letter = "G", word = "Gà", ref = topicId, isWordLesson = 1),
                    Lesson(name = "con_bo", letter = "", word = "con bò", ref = topicId, isWordLesson = 0),
                    Lesson(name = "con_meo", letter = "", word = "con mèo", ref = topicId, isWordLesson = 0),
                    Lesson(name = "con_cho", letter = "", word = "con chó", ref = topicId, isWordLesson = 0),
                    Lesson(name = "con_ga", letter = "", word = "con gà", ref = topicId, isWordLesson = 0),
                    Lesson(name = "Đây là con bò.", letter = "", word = "Đây là con bò.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Con mèo đang ngủ.", letter = "", word = "Con mèo đang ngủ.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Con chó rất trung thành.", letter = "", word = "Con chó rất trung thành.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Con gà đang gáy.", letter = "", word = "Con gà đang gáy.", ref = topicId, isWordLesson = 3)
                )
                "Nghề nghiệp" -> listOf(
                    Lesson(name = "bacsi", letter = "B", word = "Bác sĩ", ref = topicId, isWordLesson = 1),
                    Lesson(name = "giaovien", letter = "G", word = "Giáo viên", ref = topicId, isWordLesson = 1),
                    Lesson(name = "kysu", letter = "K", word = "Kỹ sư", ref = topicId, isWordLesson = 1),
                    Lesson(name = "congnhan", letter = "C", word = "Công nhân", ref = topicId, isWordLesson = 1),
                    Lesson(name = "anh_bacsi", letter = "", word = "bác sĩ", ref = topicId, isWordLesson = 0),
                    Lesson(name = "anh_giaovien", letter = "", word = "giáo viên", ref = topicId, isWordLesson = 0),
                    Lesson(name = "anh_kysu", letter = "", word = "kỹ sư", ref = topicId, isWordLesson = 0),
                    Lesson(name = "anh_congnhan", letter = "", word = "công nhân", ref = topicId, isWordLesson = 0),
                    Lesson(name = "Bố em là bác sĩ.", letter = "", word = "Bố em là bác sĩ.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Cô ấy là giáo viên.", letter = "", word = "Cô ấy là giáo viên.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Anh tôi là kỹ sư.", letter = "", word = "Anh tôi là kỹ sư.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Chú ấy là công nhân.", letter = "", word = "Chú ấy là công nhân.", ref = topicId, isWordLesson = 3)
                )
                "Gia đình" -> listOf(
                    Lesson(name = "bo", letter = "B", word = "Bố", ref = topicId, isWordLesson = 1),
                    Lesson(name = "me", letter = "M", word = "Mẹ", ref = topicId, isWordLesson = 1),
                    Lesson(name = "anh", letter = "A", word = "Anh", ref = topicId, isWordLesson = 1),
                    Lesson(name = "chi", letter = "C", word = "Chị", ref = topicId, isWordLesson = 1),
                    Lesson(name = "gd_bo", letter = "", word = "bố", ref = topicId, isWordLesson = 0),
                    Lesson(name = "gd_me", letter = "", word = "mẹ", ref = topicId, isWordLesson = 0),
                    Lesson(name = "gd_anh", letter = "", word = "anh", ref = topicId, isWordLesson = 0),
                    Lesson(name = "gd_chi", letter = "", word = "chị", ref = topicId, isWordLesson = 0),
                    Lesson(name = "Đây là bố của em.", letter = "", word = "Đây là bố của em.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Mẹ em rất hiền.", letter = "", word = "Mẹ em rất hiền.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Anh em đang học bài.", letter = "", word = "Anh em đang học bài.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Chị em rất vui vẻ.", letter = "", word = "Chị em rất vui vẻ.", ref = topicId, isWordLesson = 3)
                )
                "Thức ăn" -> listOf(
                    Lesson(name = "com", letter = "C", word = "Cơm", ref = topicId, isWordLesson = 1),
                    Lesson(name = "pho", letter = "P", word = "Phở", ref = topicId, isWordLesson = 1),
                    Lesson(name = "banhmi", letter = "B", word = "Bánh mì", ref = topicId, isWordLesson = 1),
                    Lesson(name = "trung", letter = "T", word = "Trứng", ref = topicId, isWordLesson = 1),
                    Lesson(name = "food_com", letter = "", word = "cơm", ref = topicId, isWordLesson = 0),
                    Lesson(name = "food_pho", letter = "", word = "phở", ref = topicId, isWordLesson = 0),
                    Lesson(name = "food_banhmi", letter = "", word = "bánh mì", ref = topicId, isWordLesson = 0),
                    Lesson(name = "food_trung", letter = "", word = "trứng", ref = topicId, isWordLesson = 0),
                    Lesson(name = "Em ăn cơm mỗi ngày.", letter = "", word = "Em ăn cơm mỗi ngày.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Phở rất ngon.", letter = "", word = "Phở rất ngon.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Em thích bánh mì.", letter = "", word = "Em thích bánh mì.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Trứng rất bổ.", letter = "", word = "Trứng rất bổ.", ref = topicId, isWordLesson = 3)
                )
                "Quần áo" -> listOf(
                    Lesson(name = "ao", letter = "A", word = "Áo", ref = topicId, isWordLesson = 1),
                    Lesson(name = "quan", letter = "Q", word = "Quần", ref = topicId, isWordLesson = 1),
                    Lesson(name = "vay", letter = "V", word = "Váy", ref = topicId, isWordLesson = 1),
                    Lesson(name = "giay", letter = "G", word = "Giày", ref = topicId, isWordLesson = 1),
                    Lesson(name = "cloth_ao", letter = "", word = "áo", ref = topicId, isWordLesson = 0),
                    Lesson(name = "cloth_quan", letter = "", word = "quần", ref = topicId, isWordLesson = 0),
                    Lesson(name = "cloth_vay", letter = "", word = "váy", ref = topicId, isWordLesson = 0),
                    Lesson(name = "cloth_giay", letter = "", word = "giày", ref = topicId, isWordLesson = 0),
                    Lesson(name = "Em mặc áo mới.", letter = "", word = "Em mặc áo mới.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Đây là cái quần.", letter = "", word = "Đây là cái quần.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Cô ấy mặc váy đẹp.", letter = "", word = "Cô ấy mặc váy đẹp.", ref = topicId, isWordLesson = 3),
                    Lesson(name = "Em đi giày.", letter = "", word = "Em đi giày.", ref = topicId, isWordLesson = 3)
                )
                else -> emptyList()
            }

            lessons.forEach { lesson ->
                if (lesson.isWordLesson == 3) {
                    sentenceLessons.add(lesson)
                } else {
                    normalLessons.add(lesson)
                }
            }


        }

        normalLessons.forEach { lessonRepository.insertLesson(it) }

        // Insert all sentence lessons (isWordLesson 3) last
        sentenceLessons.forEach { lessonRepository.insertLesson(it) }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onLearningModeChange(mode: LearningMode) {
        _learningMode.value = mode
    }
}
