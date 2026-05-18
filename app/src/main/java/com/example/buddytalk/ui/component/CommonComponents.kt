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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddytalk.ui.theme.ButtonYellow
import com.example.buddytalk.ui.theme.HeaderBlue
import kotlinx.coroutines.delay

@Composable
fun CompletionScreen(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Background glow
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        Brush.radialGradient(listOf(ButtonYellow.copy(alpha = 0.2f), Color.Transparent)),
                        CircleShape
                    )
            )
            Text(
                text = "🎉",
                fontSize = 100.sp,
                modifier = Modifier.scale(starScale)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "XUẤT SẮC QUÁ!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = HeaderBlue,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Bé đã hoàn thành bài học hôm nay.\nHãy tiếp tục phát huy nhé!",
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = ButtonYellow,
                    modifier = Modifier.size(48.dp).padding(4.dp)
                )
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text(
                "QUAY LẠI CHỦ ĐỀ",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}
