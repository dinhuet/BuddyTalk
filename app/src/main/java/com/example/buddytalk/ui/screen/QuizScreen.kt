package com.example.buddytalk.ui.screen

import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buddytalk.R
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.viewModel.QuizViewModel
import kotlinx.coroutines.delay

enum class QuizType {
    MATCH_IMAGE,
    AUDIO_WORD,
    AUDIO_IMAGE;

    companion object {
        fun fromRoute(value: String): QuizType {
            return when (value.lowercase()) {
                "match_image", "quiz_match_image" -> MATCH_IMAGE
                "audio_word", "quiz_audio_word" -> AUDIO_WORD
                "audio_image", "quiz_audio_image" -> AUDIO_IMAGE
                else -> MATCH_IMAGE
            }
        }
    }
}

@Composable
fun QuizScreen(
    navController: NavController,
    topicId: Long,
    type: String,
    viewModel: QuizViewModel = viewModel(),
    onQuizComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val quizType = remember(type) { QuizType.fromRoute(type) }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var selectedOptionId by remember { mutableStateOf<Long?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }

    fun playSoundInternal(soundName: String, onComplete: () -> Unit = {}) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
        val finalResId = if (resId != 0) resId else context.resources.getIdentifier("default_sound", "raw", context.packageName)
        if (finalResId != 0) {
            mediaPlayer = MediaPlayer.create(context, finalResId).apply {
                setOnCompletionListener {
                    it.release()
                    if (mediaPlayer == it) mediaPlayer = null
                    onComplete()
                }
                start()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(topicId, quizType) {
        val targetType = when (quizType) {
            QuizType.MATCH_IMAGE -> 0
            QuizType.AUDIO_WORD -> 1
            QuizType.AUDIO_IMAGE -> 0
        }
        viewModel.loadLessons(topicId, targetType)
    }

    LaunchedEffect(uiState.currentIndex, uiState.lessons, quizType) {
        selectedOptionId = null
        isCorrect = null
        val currentLesson = uiState.lessons.getOrNull(uiState.currentIndex) ?: return@LaunchedEffect
        if (quizType != QuizType.MATCH_IMAGE) {
            val soundName = when (quizType) {
                QuizType.AUDIO_WORD -> "sound${currentLesson.id}_2"
                QuizType.AUDIO_IMAGE -> "sound${currentLesson.id}"
                QuizType.MATCH_IMAGE -> ""
            }
            if (soundName.isNotEmpty()) {
                playSoundInternal(soundName)
            }
        }
    }

    LaunchedEffect(isCorrect) {
        if (isCorrect != null) {
            delay(1100)
            viewModel.nextQuestion()
        }
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onQuizComplete()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.isFinished) {
        CompletionScreen(onBack = {
            viewModel.resetFinish()
            navController.popBackStack()
        })
        return
    }

    val currentLesson = uiState.lessons.getOrNull(uiState.currentIndex)
    if (currentLesson == null) {
        EmptyQuizState(onBack = { navController.popBackStack() })
        return
    }

    val options = remember(currentLesson.id, uiState.lessons) {
        buildOptions(uiState.lessons, currentLesson, 4)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizHeader(
                title = quizHeaderTitle(quizType),
                progress = "${uiState.currentIndex + 1}/${uiState.lessons.size}",
                onBack = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                QuizPrompt(
                    quizType = quizType,
                    word = currentLesson.word,
                    onReplay = {
                        val soundName = when (quizType) {
                            QuizType.AUDIO_WORD -> "sound${currentLesson.id}_2"
                            QuizType.AUDIO_IMAGE -> "sound${currentLesson.id}"
                            QuizType.MATCH_IMAGE -> ""
                        }
                        if (soundName.isNotEmpty()) {
                            playSoundInternal(soundName)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                QuizOptionsGrid(
                    quizType = quizType,
                    options = options,
                    selectedOptionId = selectedOptionId,
                    isCorrect = isCorrect,
                    onOptionClick = { lesson ->
                        if (selectedOptionId == null) {
                            selectedOptionId = lesson.id
                            isCorrect = lesson.id == currentLesson.id
                        }
                    }
                )
            }
        }

        if (isCorrect != null) {
            ResultOverlay(isSuccess = isCorrect == true)
        }
    }
}

@Composable
private fun QuizHeader(title: String, progress: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TRẮC NGHIỆM",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = progress,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun QuizPrompt(quizType: QuizType, word: String, onReplay: () -> Unit) {
    when (quizType) {
        QuizType.MATCH_IMAGE -> {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(2.dp, Color(0xFFE3F2FD))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = word.uppercase(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E3A8A),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        QuizType.AUDIO_WORD, QuizType.AUDIO_IMAGE -> {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(2.dp, Color(0xFFE3F2FD))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (quizType == QuizType.AUDIO_WORD) "Nghe và chọn từ đúng" else "Nghe và chọn hình đúng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color(0xFFDBEAFE)
                    ) {
                        IconButton(onClick = onReplay) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Replay",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizOptionsGrid(
    quizType: QuizType,
    options: List<Lesson>,
    selectedOptionId: Long?,
    isCorrect: Boolean?,
    onOptionClick: (Lesson) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { lesson ->
                    val isSelected = selectedOptionId == lesson.id
                    if (quizType == QuizType.AUDIO_WORD) {
                        QuizWordOption(
                            lesson = lesson,
                            isSelected = isSelected,
                            isCorrect = isCorrect,
                            modifier = Modifier.weight(1f),
                            onClick = { onOptionClick(lesson) }
                        )
                    } else {
                        QuizImageOption(
                            lesson = lesson,
                            isSelected = isSelected,
                            isCorrect = isCorrect,
                            modifier = Modifier.weight(1f),
                            onClick = { onOptionClick(lesson) }
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuizImageOption(
    lesson: Lesson,
    isSelected: Boolean,
    isCorrect: Boolean?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val borderColor = when {
        isSelected && isCorrect == true -> Color(0xFF22C55E)
        isSelected && isCorrect == false -> Color(0xFFEF4444)
        else -> Color(0xFFE5E7EB)
    }
    val imageName = "image${lesson.id}"
    val imageRes = getDrawableId(context, imageName)
    val finalImageRes = if (imageRes != 0) imageRes else context.resources.getIdentifier("default_image", "drawable", context.packageName)

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = isCorrect == null, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Image(
            painter = painterResource(id = if (finalImageRes != 0) finalImageRes else R.drawable.ic_launcher_foreground),
            contentDescription = lesson.word,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun QuizWordOption(
    lesson: Lesson,
    isSelected: Boolean,
    isCorrect: Boolean?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected && isCorrect == true -> Color(0xFF22C55E)
        isSelected && isCorrect == false -> Color(0xFFEF4444)
        else -> Color(0xFFE5E7EB)
    }
    Surface(
        modifier = modifier
            .height(90.dp)
            .clickable(enabled = isCorrect == null, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = lesson.word.uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultOverlay(isSuccess: Boolean) {
    val accent = if (isSuccess) Color(0xFF22C55E) else Color(0xFFEF4444)
    val soft = if (isSuccess) Color(0xFFECFDF3) else Color(0xFFFFF1F2)
    val title = if (isSuccess) "Chính xác" else "Chưa đúng"
    val subtitle = if (isSuccess) "Bé làm rất tốt!" else "Thử lại nhé!"
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = soft
                ) {
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = title,
                    color = Color(0xFF111827),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyQuizState(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Không có câu hỏi phù hợp", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text(text = "QUAY LẠI", fontWeight = FontWeight.Bold)
        }
    }
}

private fun buildOptions(allLessons: List<Lesson>, current: Lesson, targetSize: Int): List<Lesson> {
    if (allLessons.isEmpty()) return emptyList()
    val others = allLessons.filter { it.id != current.id }.shuffled()
    val picks = others.take((targetSize - 1).coerceAtLeast(0))
    return (picks + current).shuffled()
}

private fun quizHeaderTitle(quizType: QuizType): String {
    return when (quizType) {
        QuizType.MATCH_IMAGE -> "Ghép từ ngữ và hình ảnh"
        QuizType.AUDIO_WORD -> "Nghe audio -> Chọn từ ngữ"
        QuizType.AUDIO_IMAGE -> "Nghe audio -> Chọn hình ảnh"
    }
}

