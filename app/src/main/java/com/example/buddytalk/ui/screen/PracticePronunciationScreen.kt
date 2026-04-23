package com.example.buddytalk.ui.screen

import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buddytalk.data.viewModel.LessonViewModel
import kotlinx.coroutines.launch

@Composable
fun PracticePronunciationScreen(
    navController: NavController,
    topicId: Long,
    type: String, // "sentence" or "vocabulary"
    viewModel: LessonViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Hàm phát âm thanh
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

    // Tải bài học
    LaunchedEffect(topicId, type) {
        val mode = if (type == "vocabulary") "TEXT" else "SENTENCE"
        viewModel.loadLessons(topicId, mode)
    }

    // Tự động phát âm thanh khi vào câu mới
    LaunchedEffect(uiState.currentIndex, uiState.lessons) {
        val currentLesson = uiState.lessons.getOrNull(uiState.currentIndex)
        currentLesson?.let { lesson ->
            playSoundInternal("sound${lesson.id}_2")
            // Tự động cuộn đến item đang chọn
            if (uiState.lessons.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(uiState.currentIndex)
                }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        Text("Sẵn sàng chưa?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Question Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    coroutineScope.launch {
                        val targetIndex = maxOf(0, listState.firstVisibleItemIndex - 1)
                        listState.animateScrollToItem(targetIndex)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
            }

            LazyRow(
                state = listState,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                itemsIndexed(uiState.lessons) { index, _ ->
                    val isSelected = uiState.currentIndex == index
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
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
                }
            }

            IconButton(
                onClick = { 
                    coroutineScope.launch {
                        val targetIndex = minOf(uiState.lessons.size - 1, listState.firstVisibleItemIndex + 1)
                        listState.animateScrollToItem(targetIndex)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }

        // Word Card
        if (currentLesson != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(120.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (type == "vocabulary") "LUYỆN TẬP TỪ" else "LUYỆN TẬP CÂU", 
                            color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentLesson.word.uppercase(),
                            fontSize = if (currentLesson.word.length > 8) 32.sp else 40.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color(0xFFDBEAFE)
                    ) {
                        IconButton(onClick = { 
                            playSoundInternal("sound${currentLesson.id}_2")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Mic Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = Color(0xFFDBEAFE).copy(alpha = 0.5f)
                ) {}
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color(0xFF2563EB),
                    shadowElevation = 8.dp
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(32.dp).size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "NHẤN VÀ GIỮ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E3A8A)
            )
            Text(
                text = "để bắt đầu nói nhé!",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Feedback Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = BorderStroke(2.dp, Color(0xFFDBEAFE).copy(alpha = 0.8f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "ĐANG CHỜ BÉ...",
                    color = Color(0xFFBFDBFE),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { 
                    if (uiState.currentIndex > 0) {
                        viewModel.previousLesson()
                    } else {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Text(
                    text = if (uiState.currentIndex > 0) "QUAY LẠI" else "THOÁT", 
                    color = Color.Gray, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = { 
                    mediaPlayer?.stop()
                    viewModel.nextLesson() 
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("CÂU TIẾP", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("👉", fontSize = 16.sp)
                }
            }
        }
    }
}
