package com.example.buddytalk.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buddytalk.data.viewModel.LearningMode
import com.example.buddytalk.data.viewModel.TopicViewModel
import com.example.buddytalk.data.viewModel.TopicUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(
    viewModel: TopicViewModel = viewModel(),
    mode: String = "learn",
    quizType: String? = null,
    onTopicClick: (Long, String) -> Unit = { _, _ -> }
) {
    val topics by viewModel.topicsWithCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val learningMode by viewModel.learningMode.collectAsState()
    
    val headerTitle = when (mode) {
        "practice" -> "Chủ đề luyện tập"
        "quiz" -> "Chủ đề trắc nghiệm"
        else -> "Chủ đề học"
    }

    // Cập nhật mặc định cho mode practice nếu chưa được set
    LaunchedEffect(mode, quizType) {
        when (mode) {
            "practice" -> {
                if (learningMode != LearningMode.SENTENCE && learningMode != LearningMode.VOCABULARY) {
                    viewModel.onLearningModeChange(LearningMode.SENTENCE)
                }
            }
            "quiz" -> {
                val mappedMode = mapQuizTypeToLearningMode(quizType)
                if (learningMode != mappedMode) {
                    viewModel.onLearningModeChange(mappedMode)
                }
            }
            else -> {
                if (learningMode != LearningMode.IMAGE && learningMode != LearningMode.TEXT) {
                    viewModel.onLearningModeChange(LearningMode.IMAGE)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
    ) {
        HeaderSection(headerTitle, searchQuery, viewModel::onSearchQueryChange)

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            when (mode) {
                "practice" -> {
                    Text(
                        text = "Luyện tập theo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LearningModeItem(
                            title = "Cả câu",
                            icon = Icons.Default.FormatQuote,
                            isSelected = learningMode == LearningMode.SENTENCE,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onLearningModeChange(LearningMode.SENTENCE) }
                        )
                        LearningModeItem(
                            title = "Từ vựng",
                            icon = Icons.Default.Abc,
                            isSelected = learningMode == LearningMode.VOCABULARY,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onLearningModeChange(LearningMode.VOCABULARY) }
                        )
                    }
                }
                "quiz" -> {
                    val quizLabel = quizTypeLabel(quizType)
                    Text(
                        text = "Trắc nghiệm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text(
                            text = quizLabel,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Chế độ học",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LearningModeItem(
                            title = "Hình ảnh",
                            icon = Icons.Default.Image,
                            isSelected = learningMode == LearningMode.IMAGE,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onLearningModeChange(LearningMode.IMAGE) }
                        )
                        LearningModeItem(
                            title = "Từ / Câu",
                            icon = Icons.Default.TextFields,
                            isSelected = learningMode == LearningMode.TEXT,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onLearningModeChange(LearningMode.TEXT) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Danh sách",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (mode == "practice" || mode == "quiz") {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(topics) { topicState ->
                        TopicItem(
                            topicState = topicState,
                            onClick = { onTopicClick(topicState.topic.id, learningMode.name) }
                        )
                    }
                }
            } else {
                TopicTreeView(
                    topics = topics,
                    onTopicClick = { onTopicClick(it.topic.id, learningMode.name) }
                )
            }
        }
    }
}

@Composable
fun TopicTreeView(
    topics: List<TopicUiState>,
    onTopicClick: (TopicUiState) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        itemsIndexed(topics) { index, topicState ->
            TopicTreeItem(
                topicState = topicState,
                isFirst = index == 0,
                isLast = index == topics.size - 1,
                alignRight = index % 2 != 0,
                onClick = { onTopicClick(topicState) }
            )
        }
    }
}

@Composable
fun TopicTreeItem(
    topicState: TopicUiState,
    isFirst: Boolean,
    isLast: Boolean,
    alignRight: Boolean,
    onClick: () -> Unit
) {
    val isLocked = topicState.topic.isLocked
    val isCompleted = topicState.isCompleted
    
    val circleColor = when {
        isLocked -> Color(0xFFE5E7EB)
        isCompleted -> Color(0xFF10B981)
        else -> Color(0xFF2196F3)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Vertical line
        if (!isLast) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(size.width / 2, size.height / 2),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        if (!isFirst) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height / 2),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (alignRight) {
                // Info on the left
                TopicTreeInfo(topicState, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Circle with Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(if (isLocked) 0.dp else 4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(circleColor)
                    .border(
                        width = 4.dp,
                        color = if (isCompleted || !isLocked) circleColor.copy(alpha = 0.2f) else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable(enabled = !isLocked) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(topicState.topic.name) {
                        "Động vật" -> "🦁"
                        "Nghề nghiệp" -> "👩‍🍳"
                        "Gia đình" -> "👨‍👩‍👧"
                        "Thức ăn" -> "🍕"
                        "Quần áo" -> "👕"
                        else -> "📚"
                    },
                    fontSize = 32.sp,
                    modifier = Modifier.alpha(if (isLocked) 0.5f else 1f)
                )
                
                // Status icon (check or lock)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    if (isCompleted) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(20.dp).offset(x = (-4).dp, y = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    } else if (isLocked) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(20.dp).offset(x = (-4).dp, y = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            if (!alignRight) {
                // Info on the right
                Spacer(modifier = Modifier.width(16.dp))
                TopicTreeInfo(topicState, textAlign = TextAlign.Start, modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun TopicTreeInfo(
    topicState: TopicUiState,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (textAlign == TextAlign.End) Alignment.End else Alignment.Start
    ) {
        Text(
            text = topicState.topic.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (topicState.topic.isLocked) Color.Gray else Color(0xFF333333),
            textAlign = textAlign
        )
        Text(
            text = "${topicState.lessonCount} câu",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = textAlign
        )
    }
}

@Composable
fun HeaderSection(
    title: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                )
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = { Text("Tìm kiếm...", color = Color.White.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                    disabledContainerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}

@Composable
fun LearningModeItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = if (isSelected) BorderStroke(1.dp, Color(0xFF2196F3)) else null,
        shadowElevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF2196F3) else Color.LightGray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isSelected) Color(0xFF2196F3) else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TopicItem(
    topicState: TopicUiState,
    onClick: () -> Unit
) {
    val isLocked = topicState.topic.isLocked
    val isCompleted = topicState.isCompleted

    val boxBgColor = when {
        isCompleted -> Color(0xFFD1FAE5)
        else -> Color(0xFFF0F7FF)
    }

    val surfaceColor = when {
        isCompleted -> Color(0xFFD1FAE5)
        else -> Color.White
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.6f else 1f)
            .clickable(enabled = !isLocked) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        shadowElevation = if (isLocked) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(boxBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(topicState.topic.name) {
                        "Động vật" -> "🦁"
                        "Nghề nghiệp" -> "👩‍🍳"
                        "Gia đình" -> "👨‍👩‍👧"
                        "Thức ăn" -> "🍕"
                        "Quần áo" -> "👕"
                        else -> "📚"
                    },
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topicState.topic.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) Color.Gray else Color(0xFF333333)
                )
                Text(
                    text = "${topicState.lessonCount} câu",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isCompleted) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }
    }
}

private fun mapQuizTypeToLearningMode(quizType: String?): LearningMode {
    return when (quizType?.lowercase()) {
        "quiz_audio_word", "audio_word" -> LearningMode.QUIZ_AUDIO_WORD
        "quiz_audio_image", "audio_image" -> LearningMode.QUIZ_AUDIO_IMAGE
        else -> LearningMode.QUIZ_MATCH_IMAGE
    }
}

private fun quizTypeLabel(quizType: String?): String {
    return when (quizType?.lowercase()) {
        "quiz_audio_word", "audio_word" -> "Nghe audio -> Chọn từ ngữ"
        "quiz_audio_image", "audio_image" -> "Nghe audio -> Chọn hình ảnh"
        else -> "Ghép từ ngữ và hình ảnh"
    }
}
