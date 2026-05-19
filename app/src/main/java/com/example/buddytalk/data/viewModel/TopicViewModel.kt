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

enum class TopicViewMode {
    LIST,
    TREE
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

    private val _viewMode = MutableStateFlow(TopicViewMode.TREE)
    val viewMode = _viewMode.asStateFlow()

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
                        val topicUiStateFlows = filteredTopics.map { topic ->
                            lessonRepository.getLessonsByTopicId(topic.id).map { lessons ->
                                TopicUiState(
                                    topic = topic,
                                    lessonCount = lessons.count { it.isWordLesson == targetType },
                                    isCompleted = topic.isCompleted
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
                    Lesson(name = "bo", letter = "B", word = "Bò", ref = topicId, isWordLesson = 1, tip = "Môi hơi tròn, lưỡi thẳng. Âm 'ò' — giọng huyền, hạ thấp giọng xuống."),
                    Lesson(name = "meo", letter = "M", word = "Mèo", ref = topicId, isWordLesson = 1, tip = "Mím môi bật hơi ra. Âm 'èo' — giọng huyền, hạ giọng. Lưỡi hơi cong lên khi kết thúc."),
                    Lesson(name = "cho", letter = "C", word = "Chó", ref = topicId, isWordLesson = 1, tip = "Đầu lưỡi chạm lợi trên, bật hơi mạnh. Âm 'ó' — giọng sắc, lên cao đột ngột."),
                    Lesson(name = "ga", letter = "G", word = "Gà", ref = topicId, isWordLesson = 1, tip = "Cuống lưỡi nâng lên chạm vòm mềm, bật âm. Âm 'à' — giọng huyền, hạ thấp."),
                    Lesson(name = "con_bo", letter = "", word = "con bò", ref = topicId, isWordLesson = 0, tip = "'Con': lưỡi thẳng, âm ngang. 'Bò': môi tròn, giọng huyền hạ xuống."),
                    Lesson(name = "con_meo", letter = "", word = "con mèo", ref = topicId, isWordLesson = 0, tip = "'Con': âm ngang. 'Mèo': mím môi bật hơi, giọng huyền, kết thúc lưỡi cong."),
                    Lesson(name = "con_cho", letter = "", word = "con chó", ref = topicId, isWordLesson = 0, tip = "'Con': âm ngang. 'Chó': lưỡi chạm lợi trên bật hơi mạnh, giọng sắc."),
                    Lesson(name = "con_ga", letter = "", word = "con gà", ref = topicId, isWordLesson = 0, tip = "'Con': âm ngang. 'Gà': cuống lưỡi nâng, giọng huyền hạ thấp."),
                    Lesson(name = "Đây là con bò.", letter = "", word = "Đây là con bò.", ref = topicId, isWordLesson = 3, tip = "Nhấn vào 'Đây' và 'bò'. 'Đây': âm đ đầu lưỡi chạm răng. 'bò': giọng huyền."),
                    Lesson(name = "Con mèo đang ngủ.", letter = "", word = "Con mèo đang ngủ.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'mèo' và 'ngủ'. 'mèo': giọng huyền. 'Ngủ': giọng hỏi, xuống thấp rồi lên."),
                    Lesson(name = "Con chó rất trung thành.", letter = "", word = "Con chó rất trung thành.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'chó' (giọng sắc) và 'thành' (giọng huyền). 'trung': lưỡi cong chạm vòm."),
                    Lesson(name = "Con gà đang gáy.", letter = "", word = "Con gà đang gáy.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'gà' (huyền) và 'gáy' (sắc). 'Gáy': cuống lưỡi nâng cao, giọng sắc lên gấp.")
                )
                "Nghề nghiệp" -> listOf(
                    Lesson(name = "bacsi", letter = "B", word = "Bác sĩ", ref = topicId, isWordLesson = 1, tip = "'Bác': môi tròn bật hơi, giọng sắc. 'Sĩ': lưỡi thẳng răng, giọng ngã — lên cao rồi xuống."),
                    Lesson(name = "giaovien", letter = "G", word = "Giáo viên", ref = topicId, isWordLesson = 1, tip = "'Giáo': cuống lưỡi nâng, giọng sắc. 'Viên': môi tròn dần, giọng ngang."),
                    Lesson(name = "kysu", letter = "K", word = "Kỹ sư", ref = topicId, isWordLesson = 1, tip = "'Kỹ': cuống lưỡi bật hơi, giọng ngã. 'Sư': lưỡi thẳng, môi tròn, giọng ngang."),
                    Lesson(name = "congnhan", letter = "C", word = "Công nhân", ref = topicId, isWordLesson = 1, tip = "'Công': cuống lưỡi bật, môi tròn, giọng ngang. 'Nhân': lưỡi cong chạm vòm."),
                    Lesson(name = "anh_bacsi", letter = "", word = "bác sĩ", ref = topicId, isWordLesson = 0, tip = "'Bác': môi tròn, giọng sắc. 'Sĩ': lưỡi chạm răng, giọng ngã."),
                    Lesson(name = "anh_giaovien", letter = "", word = "giáo viên", ref = topicId, isWordLesson = 0, tip = "'Giáo': gần giống 'záo', giọng sắc. 'Viên': môi tròn."),
                    Lesson(name = "anh_kysu", letter = "", word = "kỹ sư", ref = topicId, isWordLesson = 0, tip = "'Kỹ': giọng ngã, lên cao rồi xuống. 'Sư': môi tròn nhẹ."),
                    Lesson(name = "anh_congnhan", letter = "", word = "công nhân", ref = topicId, isWordLesson = 0, tip = "'Công': môi tròn. 'Nhân': lưỡi cong lên chạm vòm."),
                    Lesson(name = "Bố em là bác sĩ.", letter = "", word = "Bố em là bác sĩ.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Bố' (giọng sắc), 'bác' (sắc), 'sĩ' (ngã). 'bác sĩ' đọc rõ hai âm tiết."),
                    Lesson(name = "Cô ấy là giáo viên.", letter = "", word = "Cô ấy là giáo viên.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Cô', 'giáo' (sắc), 'viên'. 'Ấy': giọng sắc, ngắn. Đọc 'giáo viên' liền mạch."),
                    Lesson(name = "Anh tôi là kỹ sư.", letter = "", word = "Anh tôi là kỹ sư.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Anh', 'tôi', 'kỹ' (ngã), 'sư'. 'Kỹ sư' đọc rõ dấu ngã ở 'kỹ'."),
                    Lesson(name = "Chú ấy là công nhân.", letter = "", word = "Chú ấy là công nhân.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Chú' (sắc), 'công' (ngang), 'nhân'. 'công nhân' môi tròn ở 'công'.")
                )
                "Gia đình" -> listOf(
                    Lesson(name = "bo", letter = "B", word = "Bố", ref = topicId, isWordLesson = 1, tip = "Môi tròn bật hơi. Âm 'ố' — giọng sắc, lên cao nhanh."),
                    Lesson(name = "me", letter = "M", word = "Mẹ", ref = topicId, isWordLesson = 1, tip = "Mím môi bật hơi. Âm 'ẹ' — giọng nặng, đọc xuống thấp và ngắt gấp."),
                    Lesson(name = "anh", letter = "A", word = "Anh", ref = topicId, isWordLesson = 1, tip = "Miệng mở rộng. Âm 'anh' — lưỡi thẳng, giọng ngang. Kết thúc lưỡi chạm vòm."),
                    Lesson(name = "chi", letter = "C", word = "Chị", ref = topicId, isWordLesson = 1, tip = "Đầu lưỡi chạm lợi trên bật hơi. Âm 'ị' — giọng nặng, hạ thấp và dừng gấp."),
                    Lesson(name = "gd_bo", letter = "", word = "bố", ref = topicId, isWordLesson = 0, tip = "Âm 'bố' — môi tròn, giọng sắc lên cao. Đọc dứt khoát."),
                    Lesson(name = "gd_me", letter = "", word = "mẹ", ref = topicId, isWordLesson = 0, tip = "Âm 'mẹ' — mím môi bật ra, giọng nặng xuống thấp."),
                    Lesson(name = "gd_anh", letter = "", word = "anh", ref = topicId, isWordLesson = 0, tip = "Âm 'anh' — miệng mở, lưỡi thẳng, giọng ngang. Kết thúc lưỡi chạm vòm nhẹ."),
                    Lesson(name = "gd_chi", letter = "", word = "chị", ref = topicId, isWordLesson = 0, tip = "Âm 'chị' — lưỡi chạm lợi trên, bật hơi. Giọng nặng, hạ thấp."),
                    Lesson(name = "Đây là bố của em.", letter = "", word = "Đây là bố của em.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Đây' và 'bố'. 'Đây': đầu lưỡi chạm răng. 'bố': giọng sắc. 'của': giọng hỏi."),
                    Lesson(name = "Mẹ em rất hiền.", letter = "", word = "Mẹ em rất hiền.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Mẹ' (nặng) và 'hiền' (huyền). 'rất': giọng sắc, ngắn. 'hiền': lưỡi chạm vòm."),
                    Lesson(name = "Anh em đang học bài.", letter = "", word = "Anh em đang học bài.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Anh', 'học' (nặng), 'bài' (huyền). 'học': cuống lưỡi, giọng nặng hạ thấp."),
                    Lesson(name = "Chị em rất vui vẻ.", letter = "", word = "Chị em rất vui vẻ.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Chị' (nặng), 'vui', 'vẻ' (hỏi). 'vẻ': giọng hỏi, xuống thấp rồi lên nhẹ.")
                )
                "Thức ăn" -> listOf(
                    Lesson(name = "com", letter = "C", word = "Cơm", ref = topicId, isWordLesson = 1, tip = "Cuống lưỡi bật âm. Âm 'ơ' — môi dẹt. Kết thúc mím môi. Giọng ngang."),
                    Lesson(name = "pho", letter = "P", word = "Phở", ref = topicId, isWordLesson = 1, tip = "Môi trên dưới chạm nhau, bật hơi mạnh 'ph'. Âm 'ở' — giọng hỏi, xuống rồi lên."),
                    Lesson(name = "banhmi", letter = "B", word = "Bánh mì", ref = topicId, isWordLesson = 1, tip = "'Bánh': môi tròn bật, giọng sắc. 'Mì': mím môi, giọng huyền hạ xuống."),
                    Lesson(name = "trung", letter = "T", word = "Trứng", ref = topicId, isWordLesson = 1, tip = "Lưỡi cong mạnh chạm vòm. Âm 'ứng' — giọng sắc, lên cao. Kết thúc mím môi."),
                    Lesson(name = "food_com", letter = "", word = "cơm", ref = topicId, isWordLesson = 0, tip = "Âm 'cơm': cuống lưỡi bật, môi dẹt, kết thúc mím môi. Giọng ngang."),
                    Lesson(name = "food_pho", letter = "", word = "phở", ref = topicId, isWordLesson = 0, tip = "Âm 'phở': bật hơi mạnh, giọng hỏi xuống thấp rồi lên."),
                    Lesson(name = "food_banhmi", letter = "", word = "bánh mì", ref = topicId, isWordLesson = 0, tip = "'Bánh': sắc. 'Mì': huyền hạ xuống. Kết hợp rõ hai âm."),
                    Lesson(name = "food_trung", letter = "", word = "trứng", ref = topicId, isWordLesson = 0, tip = "Âm 'trứng': lưỡi cong mạnh, giọng sắc, mím môi cuối."),
                    Lesson(name = "Em ăn cơm mỗi ngày.", letter = "", word = "Em ăn cơm mỗi ngày.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'cơm' và 'ngày' (huyền). 'Ăn': miệng mở rộng. 'Mỗi': giọng ngã."),
                    Lesson(name = "Phở rất ngon.", letter = "", word = "Phở rất ngon.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Phở' (hỏi). 'Rất': sắc ngắn. 'Ngon': ngang, môi tròn."),
                    Lesson(name = "Em thích bánh mì.", letter = "", word = "Em thích bánh mì.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'thích' (sắc) và 'bánh mì' (sắc + huyền). 'thích': lưỡi chạm răng bật hơi."),
                    Lesson(name = "Trứng rất bổ.", letter = "", word = "Trứng rất bổ.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Trứng' (sắc, lưỡi cong). 'Bổ': giọng hỏi, xuống rồi lên nhẹ.")
                )
                "Quần áo" -> listOf(
                    Lesson(name = "ao", letter = "A", word = "Áo", ref = topicId, isWordLesson = 1, tip = "Miệng mở rộng. Âm 'áo' — giọng sắc, lên cao. Kết thúc môi tròn dần."),
                    Lesson(name = "quan", letter = "Q", word = "Quần", ref = topicId, isWordLesson = 1, tip = "Môi tròn bật âm 'qu'. Âm 'ần' — giọng huyền, hạ thấp. Cuối lưỡi chạm vòm."),
                    Lesson(name = "vay", letter = "V", word = "Váy", ref = topicId, isWordLesson = 1, tip = "Răng trên chạm môi dưới. Âm 'áy' — giọng sắc, lên cao. Miệng mở rộng."),
                    Lesson(name = "giay", letter = "G", word = "Giày", ref = topicId, isWordLesson = 1, tip = "Âm 'gi' gần giống 'z'. Âm 'ày' — giọng huyền, hạ thấp dần."),
                    Lesson(name = "cloth_ao", letter = "", word = "áo", ref = topicId, isWordLesson = 0, tip = "Âm 'áo' — miệng mở rộng, giọng sắc lên cao, môi tròn dần."),
                    Lesson(name = "cloth_quan", letter = "", word = "quần", ref = topicId, isWordLesson = 0, tip = "Âm 'quần' — môi tròn, giọng huyền hạ thấp, kết thúc lưỡi chạm vòm."),
                    Lesson(name = "cloth_vay", letter = "", word = "váy", ref = topicId, isWordLesson = 0, tip = "Âm 'váy' — răng trên chạm môi dưới, giọng sắc."),
                    Lesson(name = "cloth_giay", letter = "", word = "giày", ref = topicId, isWordLesson = 0, tip = "Âm 'giày' — giọng huyền, hạ thấp dần. Đọc nhẹ nhàng."),
                    Lesson(name = "Em mặc áo mới.", letter = "", word = "Em mặc áo mới.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'mặc' (nặng), 'áo' (sắc), 'mới' (sắc). 'Mặc': mím môi bật, giọng nặng."),
                    Lesson(name = "Đây là cái quần.", letter = "", word = "Đây là cái quần.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Đây', 'quần' (huyền). 'Cái': giọng sắc. 'quần': môi tròn, hạ thấp."),
                    Lesson(name = "Cô ấy mặc váy đẹp.", letter = "", word = "Cô ấy mặc váy đẹp.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'Cô', 'váy' (sắc), 'đẹp' (nặng). 'Đẹp': giọng nặng, ngắt gấp."),
                    Lesson(name = "Em đi giày.", letter = "", word = "Em đi giày.", ref = topicId, isWordLesson = 3, tip = "Nhấn 'đi' và 'giày' (huyền). 'Đi': đầu lưỡi chạm răng. 'giày': giọng huyền hạ thấp.")
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

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onLearningModeChange(mode: LearningMode) { _learningMode.value = mode }
    fun onViewModeChange(mode: TopicViewMode) { _viewMode.value = mode }
}
