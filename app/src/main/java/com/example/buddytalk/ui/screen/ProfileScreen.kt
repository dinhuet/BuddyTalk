package com.example.buddytalk.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTopics: () -> Unit,
    navController: NavController
) {
    val userState by viewModel.user.collectAsState()
    val user = userState ?: return

    val skyGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF53B6E3), Color(0xFF8CD8F5))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(skyGradient)
    ) {
        // Họa tiết trang trí nền (đám mây mờ và sao)
        CloudDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            HeaderSection(
                userName = user.userName,
                avatarUrl = user.avatarUrl,
                onSettingsClick = onNavigateToSettings
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MainProfileCard(
                    level = user.level,
                    rank = user.rank,
                    avatarUrl = user.avatarUrl,
                    experience = user.experience,
                    maxExperience = user.maxExperience,
                    onViewLessons = onNavigateToTopics
                )

                StreakCard(streak = user.streak)
            }
        }
    }
}

@Composable
fun CloudDecorations() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.size(100.dp).offset(x = (-30).dp, y = 100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)))
        Box(modifier = Modifier.size(140.dp).align(Alignment.TopEnd).offset(x = 60.dp, y = 160.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)))
        Text("⭐", modifier = Modifier.offset(x = 220.dp, y = 120.dp), fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
        Text("⭐", modifier = Modifier.offset(x = 320.dp, y = 220.dp), fontSize = 18.sp, color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
fun HeaderSection(userName: String, avatarUrl: String?, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = "🐼", fontSize = 32.sp)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "BẠN CỦA BUDDYTALK",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun MainProfileCard(
    level: Int, 
    rank: String, 
    avatarUrl: String?, 
    experience: Int, 
    maxExperience: Int,
    onViewLessons: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar & LV Badge
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .border(4.dp, Color(0xFFEEEEEE), CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF9F9F9)),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = "🐼", fontSize = 64.sp)
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEBB12),
                    modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                ) {
                    Text(
                        text = "LV $level",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Kinh nghiệm", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val expText = if (level >= 30) "MAX" else "$experience/$maxExperience EXP"
                    Text(text = expText, color = Color(0xFF53B6E3), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (level >= 30) 1f else experience.toFloat() / maxExperience.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = Color(0xFFD3D3D3),
                    trackColor = Color(0xFFF5F5F5),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rank Badge
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFF8E1),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFEBB12), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = rank, color = Color(0xFFFEBB12), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3D Button
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp).offset(y = 4.dp).background(Color(0xFFD48D06), RoundedCornerShape(20.dp)))
                Button(
                    onClick = onViewLessons,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEBB12)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "XEM BÀI HỌC",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF1D3557)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    val streakGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF9500), Color(0xFFFF4B2B), Color(0xFF9129AD))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(streakGradient)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "CHUỖI",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$streak",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 72.sp,
                        lineHeight = 72.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "🔥", fontSize = 36.sp, modifier = Modifier.offset(y = (-10).dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Cố gắng lên!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            
            Text(
                text = "🔥",
                fontSize = 120.sp,
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = 15.dp, y = 15.dp)
            )
        }
    }
}
