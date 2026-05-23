package com.example.buddytalk.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buddytalk.ui.navigation.Routes

@Composable
fun QuizMenuScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
    ) {
        QuizMenuHeader(onBack = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Bé muốn chơi kiểu nào?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Câu hỏi sẽ được lấy ngẫu nhiên từ các bài đã mở.",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QuizModeCard(
                    label = "Ghép",
                    accent = Color(0xFF8CCBFF),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Routes.Quiz.createRoute("quiz_match_image")) }
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = Color(0xFF0F4C81), modifier = Modifier.size(26.dp))
                    Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFF0F4C81), modifier = Modifier.size(30.dp))
                }

                QuizModeCard(
                    label = "Nghe chữ",
                    accent = Color(0xFF7CC7FF),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Routes.Quiz.createRoute("quiz_audio_word")) }
                ) {
                    Icon(Icons.Default.Hearing, contentDescription = null, tint = Color(0xFF0B5CAD), modifier = Modifier.size(28.dp))
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = Color(0xFF0B5CAD), modifier = Modifier.size(24.dp))
                }

                QuizModeCard(
                    label = "Nghe hình",
                    accent = Color(0xFF8DE0B8),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Routes.Quiz.createRoute("quiz_audio_image")) }
                ) {
                    Icon(Icons.Default.Hearing, contentDescription = null, tint = Color(0xFF0F7A4A), modifier = Modifier.size(28.dp))
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF0F7A4A), modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun QuizMenuHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
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
            Column {
                Text(
                    text = "TRẮC NGHIỆM",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cùng chơi nào",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun QuizModeCard(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    iconContent: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .height(104.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.28f), Color.White)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.88f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = iconContent
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = Color(0xFF1F2937),
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
