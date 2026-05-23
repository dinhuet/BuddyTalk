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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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

    LaunchedEffect(quizType) {
        val targetType = when (quizType) {
            QuizType.MATCH_IMAGE -> 0
            QuizType.AUDIO_WORD -> 1
            QuizType.AUDIO_IMAGE -> 0
        }
        viewModel.loadLessons(targetType)
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
            CircularProgressIndicator(color = Color(0xFF2196F3))
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
    val progress = ((uiState.currentIndex + 1).toFloat() / uiState.lessons.size.coerceAtLeast(1))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F9FF), Color(0xFFEAF4FF))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizHeader(
                title = quizHeaderTitle(quizType),
                progressText = "${uiState.currentIndex + 1}/${uiState.lessons.size}",
                progressValue = progress,
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .then(
                        if (quizType == QuizType.AUDIO_IMAGE) {
                            Modifier.navigationBarsPadding()
                        } else {
                            Modifier
                        }
                    )
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

                Spacer(modifier = Modifier.height(18.dp))

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
private fun QuizHeader(
    title: String,
    progressText: String,
    progressValue: Float,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.22f), CircleShape)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TRẮC NGHIỆM",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.26f)
                ) {
                    Text(
                        text = progressText,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.28f)
            )
        }
    }
}

@Composable
private fun QuizPrompt(quizType: QuizType, word: String, onReplay: () -> Unit) {
    val accent = when (quizType) {
        QuizType.MATCH_IMAGE -> Color(0xFF4DA3FF)
        QuizType.AUDIO_WORD -> Color(0xFF3D8BFF)
        QuizType.AUDIO_IMAGE -> Color(0xFF2F80ED)
    }
    val icon = when (quizType) {
        QuizType.MATCH_IMAGE -> Icons.Default.GridView
        QuizType.AUDIO_WORD -> Icons.Default.TextFields
        QuizType.AUDIO_IMAGE -> Icons.Default.Image
    }
    val helper = when (quizType) {
        QuizType.MATCH_IMAGE -> "Chạm vào hình đúng với từ này"
        QuizType.AUDIO_WORD -> "Nghe kỹ rồi chọn chữ đúng"
        QuizType.AUDIO_IMAGE -> "Nghe kỹ rồi chọn hình đúng"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (quizType == QuizType.AUDIO_IMAGE) 120.dp else 0.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.18f)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                    }
                }
                if (quizType != QuizType.MATCH_IMAGE) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = CircleShape,
                        color = accent.copy(alpha = 0.14f)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Hearing, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (quizType == QuizType.AUDIO_IMAGE) 8.dp else 12.dp))

            Text(
                text = helper,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(if (quizType == QuizType.AUDIO_IMAGE) 8.dp else 14.dp))

            if (quizType == QuizType.MATCH_IMAGE) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFFFF7E7),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                ) {
                    Text(
                    text = word.uppercase(),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E3A8A),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.size(74.dp),
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.18f)
                ) {
                    IconButton(onClick = onReplay) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Replay",
                            tint = accent,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                if (quizType != QuizType.AUDIO_IMAGE) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Bấm loa để nghe lại",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
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
        else -> Color(0xFFE8ECF4)
    }
    val backgroundColor = when {
        isSelected && isCorrect == true -> Color(0xFFF0FDF4)
        isSelected && isCorrect == false -> Color(0xFFFFF1F2)
        else -> Color.White
    }
    val imageName = "image${lesson.id}"
    val imageRes = getDrawableId(context, imageName)
    val finalImageRes = if (imageRes != 0) imageRes else context.resources.getIdentifier("default_image", "drawable", context.packageName)

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = isCorrect == null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor),
        shadowElevation = 2.dp
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
        else -> Color(0xFFE8ECF4)
    }
    val backgroundColor = when {
        isSelected && isCorrect == true -> Color(0xFFF0FDF4)
        isSelected && isCorrect == false -> Color(0xFFFFF1F2)
        else -> Color.White
    }

    Surface(
        modifier = modifier
            .height(102.dp)
            .clickable(enabled = isCorrect == null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = lesson.word.uppercase(),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultOverlay(isSuccess: Boolean) {
    val accent = if (isSuccess) Color(0xFF22C55E) else Color(0xFFF87171)
    val soft = if (isSuccess) Color(0xFFECFDF3) else Color(0xFFFFF1F2)
    val title = if (isSuccess) "Đúng rồi!" else "Chưa đúng"
    val subtitle = if (isSuccess) "Bé chọn rất giỏi." else "Mình thử câu tiếp theo nhé."
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.24f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = soft
                ) {
                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(38.dp)
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
            .background(Color(0xFFFFFBF6))
            .background(Color(0xFFF5F9FF))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Chưa có đủ câu hỏi", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Quiz chỉ lấy từ các bài đang mở.",
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(18.dp))
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
        QuizType.MATCH_IMAGE -> "Ghép hình"
        QuizType.AUDIO_WORD -> "Nghe chọn chữ"
        QuizType.AUDIO_IMAGE -> "Nghe chọn hình"
    }
}
