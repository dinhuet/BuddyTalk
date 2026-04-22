package com.example.buddytalk.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: UserViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTopics: () -> Unit
) {
    val userState by viewModel.user.collectAsState()
    
    val user = userState ?: return

    Scaffold(
        bottomBar = { BottomNavigationBar() },
        containerColor = AppBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HeaderSection(
                userName = user.userName,
                onSettingsClick = onNavigateToSettings
            )
            Spacer(modifier = Modifier.height(16.dp))
            MainProfileCard(
                level = user.level,
                rank = user.rank,
                onViewLessonsClick = onNavigateToTopics
            )
            Spacer(modifier = Modifier.height(16.dp))
            BottomCardsRow(streak = user.streak)
        }
    }
}

@Composable
fun HeaderSection(userName: String, onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(HeaderBlue, HeaderBlueLight)
                )
            )
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Face, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = "BẠN CỦA BUDDYTALK",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun MainProfileCard(level: Int, rank: String, onViewLessonsClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(320.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = ButtonYellow)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ButtonYellow,
                    modifier = Modifier.offset(y = 4.dp)
                ) {
                    Text(
                        text = "LV $level",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF8E1),
                border = null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = ButtonYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = rank, color = ButtonYellow, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onViewLessonsClick,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonYellow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "XEM BÀI HỌC", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun BottomCardsRow(streak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "TÚI ĐỒ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "TẤT CẢ", color = HeaderBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ItemIcon(Icons.Default.Star, "TOP 1", ButtonYellow)
                    ItemIcon(Icons.Default.ThumbUp, "7 NGÀY", Color.Red)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ItemIcon(Icons.Default.Menu, "", Color.LightGray)
                    ItemIcon(Icons.Default.Info, "", Color.LightGray)
                }
            }
        }
        
        Card(
            modifier = Modifier
                .weight(1f)
                .height(180.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = listOf(StreakOrangeLight, StreakOrange)))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "CHUỖI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "$streak", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 48.sp)
                    Text(text = "Cố gắng lên!", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(60.dp).align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
fun ItemIcon(icon: ImageVector, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        if (label.isNotEmpty()) {
            Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextGray)
        }
    }
}

@Composable
fun BottomNavigationBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = Color.LightGray)
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HeaderBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
            
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = HeaderBlue)
        }
    }
}