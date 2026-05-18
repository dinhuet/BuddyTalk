package com.example.buddytalk.ui.screen

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buddytalk.R
import com.example.buddytalk.data.entity.Lesson
import com.example.buddytalk.data.viewModel.LessonViewModel
import com.example.buddytalk.ui.component.CompletionScreen

@Composable
fun LessonScreen(
    navController: androidx.navigation.NavController,
    topicId: Long,
    mode: String,
    viewModel: LessonViewModel = viewModel(),
    onLessonComplete: (Long, Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun playSoundInternal(soundName: String) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
        if (resId != 0) {
            mediaPlayer = MediaPlayer.create(context, resId).apply { start() }
        }
    }

    LaunchedEffect(topicId, mode) {
        viewModel.loadLessons(topicId, mode)
    }

    LaunchedEffect(uiState.currentIndex, uiState.lessons) {
        uiState.lessons.getOrNull(uiState.currentIndex)?.let { lesson ->
            if (mode == "TEXT") playSoundInternal("sound${lesson.id}_1")
            else playSoundInternal("sound${lesson.id}")
            
            // Cộng XP cho bài học này
            onLessonComplete(lesson.id, true) 
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F9FF))) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(Brush.verticalGradient(colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)))), contentAlignment = Alignment.Center) {
            Text(text = if (mode == "TEXT") "Thẻ chữ" else "Thẻ hình ảnh", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        Box(modifier = Modifier.weight(1f).padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            currentLesson?.let { lesson ->
                if (mode == "TEXT") {
                    Text(text = lesson.word, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                } else {
                    val resId = context.resources.getIdentifier("image${lesson.id}", "drawable", context.packageName)
                    Image(painter = painterResource(id = if (resId != 0) resId else R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(200.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { viewModel.previousLesson() }, enabled = uiState.currentIndex > 0) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }
            IconButton(onClick = { viewModel.nextLesson() }) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}
