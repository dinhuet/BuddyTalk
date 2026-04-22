package com.example.buddytalk.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buddytalk.ui.theme.HeaderBlue

@Composable
fun PracticePronunciationScreen(
    navController: NavController,
    topicId: Long,
    type: String // "sentence" or "vocabulary"
) {
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
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
            }
        }

        // Progress Steps
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepItem(number = "1", isCompleted = true, isCurrent = true)
            Divider(modifier = Modifier.weight(1f).height(2.dp).padding(horizontal = 8.dp), color = Color(0xFFE5E7EB))
            StepItem(number = "2", isCompleted = false, isCurrent = false)
            Divider(modifier = Modifier.weight(1f).height(2.dp).padding(horizontal = 8.dp), color = Color(0xFFE5E7EB))
            StepItem(number = "3", isCompleted = false, isCurrent = false)
        }

        // Word Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(120.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PHÁT ÂM TỪ", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "DOG",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "[dɔɡ]",
                            fontSize = 24.sp,
                            color = Color.LightGray
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color(0xFFDBEAFE)
                ) {
                    IconButton(onClick = { /* Play Audio */ }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
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
                // Outer circle
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = Color(0xFFDBEAFE).copy(alpha = 0.5f)
                ) {}
                // Inner blue circle
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
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDBEAFE).copy(alpha = 0.8f))
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

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Text("QUAY LẠI", color = Color.LightGray, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = { /* Next question */ },
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

@Composable
fun StepItem(number: String, isCompleted: Boolean, isCurrent: Boolean) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = if (isCurrent) Color(0xFF2563EB) else if (isCompleted) Color(0xFFDBEAFE) else Color(0xFFF3F4F6)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number,
                color = if (isCurrent) Color.White else if (isCompleted) Color(0xFF2563EB) else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
