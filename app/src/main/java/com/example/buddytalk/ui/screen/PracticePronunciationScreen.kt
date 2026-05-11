package com.example.buddytalk.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buddytalk.data.viewModel.LessonViewModel
import com.example.buddytalk.ui.component.CompletionScreen
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun PracticePronunciationScreen(
    navController: NavController,
    topicId: Long,
    type: String, // "sentence" or "vocabulary"
    viewModel: LessonViewModel = viewModel(),
    onLessonComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Function to play sound
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

    // Show toast message when error occurs (e.g., not completed all sentences)
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // Play doprevious sound when the "complete all" error appears
            if (message == "Bé hãy hoàn thành các câu trước nhé!") {
                playSoundInternal("doprevious")
            }
            viewModel.clearError()
        }
    }

    // Status mapping based on Vosk state
    val feedbackText = when {
        !uiState.isModelLoaded -> "ĐANG TẢI DỮ LIỆU..."
        uiState.isListening -> if (uiState.partialText.isEmpty()) "CON NÓI ĐI, BÉ ĐANG NGHE..." else uiState.partialText.uppercase()
        uiState.recognizedText.isNotEmpty() -> uiState.recognizedText.uppercase()
        else -> "ĐANG CHỜ BÉ..."
    }

    val feedbackColor = when {
        !uiState.isModelLoaded -> Color.Gray
        uiState.isListening -> Color(0xFF2563EB)
        uiState.recognizedText.isNotEmpty() -> if (uiState.isCorrect) Color(0xFF00C853) else Color.Red
        else -> Color(0xFFBFDBFE)
    }

    // Load lessons
    LaunchedEffect(topicId, type) {
        val mode = if (type == "vocabulary") "TEXT" else "SENTENCE"
        viewModel.loadLessons(topicId, mode)
    }

    LaunchedEffect(uiState.currentIndex, uiState.lessons) {
        uiState.lessons.getOrNull(uiState.currentIndex)?.let { lesson ->
            val soundName = if (type == "sentence") "sound${lesson.id}" else "sound${lesson.id}_2"
            playSoundInternal(soundName)
            if (uiState.lessons.isNotEmpty()) {
                coroutineScope.launch { listState.animateScrollToItem(uiState.currentIndex) }
            }
        }
    }

    // Streak update when finished
    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onLessonComplete()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (uiState.isFinished) {
        CompletionScreen(onBack = { viewModel.resetFinish(); navController.popBackStack() })
        return
    }

    val currentLesson = uiState.lessons.getOrNull(uiState.currentIndex)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFF))) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)))).padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🦉", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("CHÀO BÉ!", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (type == "vocabulary") "LUYỆN TẬP TỪ" else "LUYỆN TẬP CÂU",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Selector Bar
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { coroutineScope.launch { listState.animateScrollToItem(maxOf(0, listState.firstVisibleItemIndex - 1)) } }, modifier = Modifier.size(32.dp).pointerHoverIcon(PointerIcon.Hand)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
            }
            LazyRow(state = listState, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                itemsIndexed(uiState.lessons) { index, _ ->
                    val isSelected = uiState.currentIndex == index
                    val isCorrect = uiState.completedIndices.contains(index)

                    Box(contentAlignment = Alignment.TopEnd) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable { viewModel.setCurrentIndex(index) },
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF3F4F6),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (index + 1).toString(),
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        if (isCorrect) {
                            Surface(
                                modifier = Modifier
                                    .size(14.dp)
                                    .offset(x = 4.dp, y = (-4).dp),
                                shape = CircleShape,
                                color = Color(0xFF00C853),
                                border = BorderStroke(1.5.dp, Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
            IconButton(onClick = { coroutineScope.launch { listState.animateScrollToItem(minOf(uiState.lessons.size - 1, listState.firstVisibleItemIndex + 1)) } }, modifier = Modifier.size(32.dp).pointerHoverIcon(PointerIcon.Hand)) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }

        // Word Card
        if (currentLesson != null) {
            Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(120.dp), shape = RoundedCornerShape(32.dp), color = Color.White, shadowElevation = 2.dp, border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        val annotatedWord = buildAnnotatedString {
                            val words = currentLesson.word.split(Regex("\\s+"))
                            words.forEachIndexed { index, word ->
                                val isWrong = uiState.mispronouncedIndices.contains(index)
                                withStyle(style = SpanStyle(
                                    color = if (isWrong) Color.Red else Color(0xFF1E3A8A)
                                )) {
                                    append(word.uppercase())
                                }
                                if (index < words.size - 1) append(" ")
                            }
                        }
                        Text(
                            text = annotatedWord,
                            fontSize = if (currentLesson.word.length > 8) 28.sp else 36.sp,
                            lineHeight = if (currentLesson.word.length > 8) 34.sp else 44.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Surface(modifier = Modifier.size(56.dp).pointerHoverIcon(PointerIcon.Hand), shape = CircleShape, color = Color(0xFFDBEAFE)) {
                        IconButton(onClick = {
                            val soundName = if (type == "sentence") "sound${currentLesson.id}" else "sound${currentLesson.id}_2"
                            playSoundInternal(soundName)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Mẹo Kiki condition
        val showTip = !uiState.isCorrect && uiState.recognizedText.isNotEmpty() && !uiState.isListening && (currentLesson?.tip?.isNotEmpty() == true)

        // Mic Section with Tip Card
        if (type == "vocabulary") {
            // Vocabulary mode: Mic bên trái, Mẹo bên phải
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Mic Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).pointerInput(uiState.isModelLoaded) {
                            if (!uiState.isModelLoaded) return@pointerInput
                            detectTapGestures(
                                onPress = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        try {
                                            viewModel.startListening()
                                            awaitRelease()
                                        } finally {
                                            viewModel.stopListening()
                                        }
                                    } else {
                                        // Request permission or show error
                                    }
                                }
                            )
                        }
                    ) {
                        Surface(modifier = Modifier.size(160.dp), shape = CircleShape, color = if (uiState.isListening) Color(0xFF2563EB).copy(alpha = 0.2f) else Color(0xFFDBEAFE).copy(alpha = 0.5f)) {}
                        Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = if (!uiState.isModelLoaded) Color.Gray else if (uiState.isListening) Color(0xFF00C853) else Color(0xFF2563EB), shadowElevation = 8.dp) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.padding(32.dp).size(48.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = if (uiState.isModelLoaded) "NHẤN & GIỮ" else "ĐANG TẢI...", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))
                }

                // Mẹo Kiki Tip Card bên phải mic
                AnimatedVisibility(
                    visible = showTip,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .widthIn(max = 160.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF0F4FF),
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.5.dp, Color(0xFFBFDBFE))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Mẹo Kiki",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E3A8A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "👅",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentLesson?.tip ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFF374151),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Sentence mode: Mic ở trên, Mẹo ở dưới
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mic Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).pointerInput(uiState.isModelLoaded) {
                        if (!uiState.isModelLoaded) return@pointerInput
                        detectTapGestures(
                            onPress = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    try {
                                        viewModel.startListening()
                                        awaitRelease()
                                    } finally {
                                        viewModel.stopListening()
                                    }
                                } else {
                                    // Request permission or show error
                                }
                            }
                        )
                    }
                ) {
                    Surface(modifier = Modifier.size(160.dp), shape = CircleShape, color = if (uiState.isListening) Color(0xFF2563EB).copy(alpha = 0.2f) else Color(0xFFDBEAFE).copy(alpha = 0.5f)) {}
                    Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = if (!uiState.isModelLoaded) Color.Gray else if (uiState.isListening) Color(0xFF00C853) else Color(0xFF2563EB), shadowElevation = 8.dp) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.padding(32.dp).size(48.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = if (uiState.isModelLoaded) "NHẤN & GIỮ" else "ĐANG TẢI...", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))

                // Mẹo Kiki Tip Card bên dưới mic (cho câu dài)
                AnimatedVisibility(
                    visible = showTip,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 24.dp, end = 24.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF0F4FF),
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.5.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "👅",
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Mẹo Kiki",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentLesson?.tip ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF374151),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // THAY THẾ: Hiển thị Perfect hoặc Feedback Box tùy thuộc vào uiState.isCorrect
        if (uiState.isCorrect) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Perfect!",
                            color = Color(0xFF00C853),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD600),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Feedback Box (Chỉ hiển thị khi chưa nói chính xác)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp).height(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(2.dp, feedbackColor.copy(alpha = 0.8f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = feedbackText,
                        color = feedbackColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Nav Buttons (Quay lại / Câu tiếp giữ nguyên vị trí)
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { if (uiState.currentIndex > 0) viewModel.previousLesson() else navController.popBackStack() }, modifier = Modifier.weight(1f).height(56.dp).pointerHoverIcon(PointerIcon.Hand), colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (uiState.currentIndex > 0) "QUAY LẠI" else "THOÁT", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
            Button(onClick = { mediaPlayer?.stop(); viewModel.nextLesson() }, modifier = Modifier.weight(1f).height(56.dp).pointerHoverIcon(PointerIcon.Hand), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)), shape = RoundedCornerShape(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("CÂU TIẾP", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
