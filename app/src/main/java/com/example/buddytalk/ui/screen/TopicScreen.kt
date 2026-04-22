package com.example.buddytalk.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    onTopicClick: (Long) -> Unit = {}
) {
    val topics by viewModel.topicsWithCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val learningMode by viewModel.learningMode.collectAsState()

    // Cập nhật mặc định cho mode practice nếu chưa được set
    LaunchedEffect(mode) {
        if (mode == "practice" && learningMode != LearningMode.SENTENCE && learningMode != LearningMode.VOCABULARY) {
            viewModel.onLearningModeChange(LearningMode.SENTENCE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
    ) {
        HeaderSection(searchQuery, viewModel::onSearchQueryChange)

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            if (mode == "practice") {
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
            } else {
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

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Danh sách chủ đề",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(topics) { topicState ->
                    TopicItem(
                        topicState = topicState,
                        onClick = { onTopicClick(topicState.topic.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
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
                text = "Chủ đề học",
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.6f else 1f)
            .clickable(enabled = !isLocked) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
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
                    .background(Color(0xFFF0F7FF), RoundedCornerShape(12.dp)),
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
