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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
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

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderSection(
                userName = user.userName,
                onSettingsClick = onNavigateToSettings
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                MainProfileCard(
                    level = user.level, 
                    rank = user.rank,
                    avatarUrl = user.avatarUrl
                )
                Spacer(modifier = Modifier.height(16.dp))
                BottomCardsRow(streak = user.streak)
                
                Spacer(modifier = Modifier.weight(1f))
                // BottomNavigationBar được gọi tập trung tại MainActivity
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(HeaderBlue, HeaderBlueLight)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
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
fun MainProfileCard(level: Int, rank: String, avatarUrl: String?) {
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
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = null, 
                            modifier = Modifier.size(80.dp), 
                            tint = ButtonYellow
                        )
                    }
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
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(64.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFFD48D06), shape = RoundedCornerShape(24.dp))
                )
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonYellow),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-6).dp)
                        .height(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "XEM BÀI HỌC", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 18.sp,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
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
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFEBC85E),
                                Color(0xFF77B5FE)
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CHUỖI",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$streak",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 48.sp,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "🔥", fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Cố gắng lên!",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
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
