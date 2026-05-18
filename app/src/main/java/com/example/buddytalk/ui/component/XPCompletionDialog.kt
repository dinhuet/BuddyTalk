package com.example.buddytalk.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.buddytalk.ui.theme.ButtonYellow
import com.example.buddytalk.ui.theme.HeaderBlue
import kotlinx.coroutines.delay

@Composable
fun XPCompletionDialog(
    xpGained: Int,
    isLevelUp: Boolean,
    newLevel: Int,
    onDismiss: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    
    // Animation cho scale của Dialog
    val scale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    // Animation cho các ngôi sao bay
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        showContent = true
        delay(4000) // Tự động đóng sau 4 giây
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Hiệu ứng hạt bụi/ngôi sao mờ phía sau
            repeat(8) { i ->
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = ButtonYellow.copy(alpha = starAlpha),
                    modifier = Modifier
                        .offset(
                            x = (if (i % 2 == 0) 120 else -120).dp,
                            y = (i * 40 - 160).dp
                        )
                        .scale(0.8f)
                )
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .scale(scale)
                    .background(Color.White, RoundedCornerShape(32.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isLevelUp) "THĂNG CẤP MỚI! 🚀" else "BÉ GIỎI QUÁ! 🎉",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = HeaderBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = if (isLevelUp) "Bé đã đạt đến trình độ mới" else "Bé đã hoàn thành bài học",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                Brush.radialGradient(listOf(ButtonYellow.copy(alpha = 0.4f), Color.Transparent)),
                                CircleShape
                            )
                    )
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = ButtonYellow,
                        modifier = Modifier.size(80.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "+$xpGained XP",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = ButtonYellow
                )
                
                if (isLevelUp) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "CẤP ĐỘ HIỆN TẠI: $newLevel",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = HeaderBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderBlue),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("TIẾP TỤC HỌC", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }
}
