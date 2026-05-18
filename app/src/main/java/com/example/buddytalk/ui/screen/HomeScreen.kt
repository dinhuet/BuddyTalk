package com.example.buddytalk.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buddytalk.data.viewModel.HomeViewModel
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.ui.navigation.Routes
import com.example.buddytalk.ui.theme.HeaderBlue

@Composable
fun HomeScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user by userViewModel.user.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HeaderBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("B", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "BuddyTalk",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF5F5),
                    border = BorderStroke(1.dp, Color(0xFFFFE0E0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user?.streak ?: 0}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4B4B)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Greeting
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.greeting,
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hôm nay bạn muốn làm\ngì?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF333333),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 36.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconCard(
                    icon = "📚",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Topics.createRoute("learn")) }
                )
                IconCard(
                    icon = "🎯",
                    color = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Topics.createRoute("practice")) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconCard(
                    icon = "🧩",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.QuizMenu.route) }
                )
                IconCard(
                    icon = "📊",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Analytics.route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DailyMissionItem()
    }
}

@Composable
fun IconCard(
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.25f),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.5f)),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 36.sp)
            }
        }
    }
}

@Composable
fun DailyMissionItem() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF7ED),
        border = BorderStroke(1.dp, Color(0xFFFFE0B0))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎁", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Nhiệm vụ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF333333).copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
