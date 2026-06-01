package com.example.buddytalk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.buddytalk.ui.screen.getBadgeBrush

@Composable
fun LevelUpDialog(
    level: Int,
    onDismiss: () -> Unit
) {
    val badgeBrush = getBadgeBrush(level)
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFE3F2FD), Color.White)
                        )
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hiệu ứng vòng hào quang badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(4.dp, badgeBrush)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "⭐", fontSize = 50.sp)
                        }
                    }
                    
                    Text(text = "✨", fontSize = 30.sp, modifier = Modifier.align(Alignment.TopStart))
                    Text(text = "🎉", fontSize = 30.sp, modifier = Modifier.align(Alignment.TopEnd))
                    Text(text = "🎈", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomStart))
                    Text(text = "🎊", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "LÊN CẤP MỚI!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E88E5)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bé đã đạt đến Cấp độ $level",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Thật tuyệt vời! Bé đang học rất tốt và trở nên thông minh hơn mỗi ngày.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Text(
                        "NHẬN THƯỞNG", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 18.sp, 
                        color = Color.White
                    )
                }
            }
        }
    }
}
