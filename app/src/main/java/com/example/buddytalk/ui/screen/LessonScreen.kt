package com.example.buddytalk.ui.screen

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buddytalk.R
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.viewModel.LessonViewModel

@Composable
fun LessonScreen(
    navController: androidx.navigation.NavController,
    topicId: Long,
    mode: String,
    viewModel: LessonViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Quản lý MediaPlayer duy nhất để có thể ngắt âm thanh cũ
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Giải phóng MediaPlayer khi thoát màn hình
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun playSoundInternal(soundName: String, onComplete: () -> Unit = {}) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        
        val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
        // Nếu không tìm thấy file theo định dạng, dùng default_sound
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

    LaunchedEffect(topicId, mode) {
        viewModel.loadLessons(topicId, mode)
    }

    // Tự động phát âm thanh khi chuyển câu
    LaunchedEffect(uiState.currentIndex, uiState.lessons) {
        val currentLesson = uiState.lessons.getOrNull(uiState.currentIndex)
        currentLesson?.let { lesson ->
            if (mode == "TEXT") {
                // Thẻ chữ: sound{lessonId}_1.mp3 và sound{lessonId}_2.mp3
                playSoundInternal("sound${lesson.id}_1") {
                    playSoundInternal("sound${lesson.id}_2")
                }
            } else {
                // Thẻ hình ảnh: sound{lessonId}.mp3
                playSoundInternal("sound${lesson.id}")
            }
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
            .background(Color(0xFFF5F9FF))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (mode == "TEXT") "Thẻ chữ" else "Thẻ hình ảnh",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (currentLesson != null) {
                if (mode == "TEXT") {
                    TextLessonContent(
                        lesson = currentLesson,
                        onWordClick = { playSoundInternal("sound${currentLesson.id}_2") }
                    )
                } else {
                    ImageLessonContent(
                        lesson = currentLesson,
                        onImageClick = { playSoundInternal("sound${currentLesson.id}") }
                    )
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = { 
                    mediaPlayer?.stop()
                    viewModel.previousLesson() 
                },
                enabled = uiState.currentIndex > 0,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = Color.Gray)
            }

            // Sound Button (Replay)
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color(0xFF2196F3),
                shadowElevation = 4.dp,
                onClick = {
                    currentLesson?.let { lesson ->
                        if (mode == "TEXT") {
                            playSoundInternal("sound${lesson.id}_1") {
                                playSoundInternal("sound${lesson.id}_2")
                            }
                        } else {
                            playSoundInternal("sound${lesson.id}")
                        }
                    }
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Play Sound", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            // Next Button
            IconButton(
                onClick = { 
                    mediaPlayer?.stop()
                    viewModel.nextLesson() 
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun TextLessonContent(lesson: Lesson, onWordClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3F2FD))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lesson.letter,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2196F3)
                )
            }
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF3E0),
                onClick = onWordClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = lesson.word,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageLessonContent(lesson: Lesson, onImageClick: () -> Unit) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3F2FD))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = getDrawableId(context, lesson.name)
                Image(
                    painter = painterResource(id = if (imageRes != 0) imageRes else R.drawable.ic_launcher_foreground),
                    contentDescription = lesson.word,
                    modifier = Modifier.size(150.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE3F2FD),
                onClick = onImageClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = lesson.word,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

@Composable
fun CompletionScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "TUYỆT VỜI!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2196F3)
        )
        Text(
            text = "Bé đã hoàn thành bài tập rồi đó",
            fontSize = 18.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text("QUAY LẠI CHỦ ĐỀ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

fun getDrawableId(context: Context, name: String): Int {
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}
