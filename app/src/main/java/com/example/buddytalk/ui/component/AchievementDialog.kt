package com.example.buddytalk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.buddytalk.data.viewModel.AchievementEvent
import com.example.buddytalk.data.viewModel.AchievementType

@Composable
fun AchievementDialog(
    achievement: AchievementEvent,
    onDismiss: () -> Unit
) {
    val accent = when (achievement.type) {
        AchievementType.LEVEL_UP -> Color(0xFF7C3AED)
        AchievementType.BADGE_UP -> Color(0xFFFF9800)
    }
    val emoji = when (achievement.type) {
        AchievementType.LEVEL_UP -> "🚀"
        AchievementType.BADGE_UP -> "🏅"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(accent.copy(alpha = 0.18f), Color.White)
                        )
                    )
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(28.dp))
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    color = accent,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = emoji, fontSize = 46.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = achievement.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = achievement.message,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.82f)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Text("TUYỆT VỜI", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

