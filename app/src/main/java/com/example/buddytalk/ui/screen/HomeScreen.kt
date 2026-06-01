package com.example.buddytalk.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.ui.navigation.Routes

@Composable
fun HomeScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val user by userViewModel.user.collectAsState()
    val lessonCount = user?.lessonCount ?: 0
    val exerciseCount = user?.exerciseCount ?: 0
    val avatarUrl = user?.avatarUrl

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        HeaderSection(
            avatarUrl = avatarUrl,
            onLearnNow = { navController.navigate(Routes.Topics.createRoute("learn")) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tiến độ của bé",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressCard(
                    icon = Icons.Default.AutoStories,
                    title = "$lessonCount Bài học",
                    accentColor = Color(0xFF4C84FF),
                    progressFraction = (lessonCount / 10f).coerceAtMost(1f),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Topics.createRoute("learn")) }
                )
                ProgressCard(
                    icon = Icons.Default.Edit,
                    title = "$exerciseCount Bài tập",
                    accentColor = Color(0xFF15B299),
                    progressFraction = (exerciseCount / 10f).coerceAtMost(1f),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Topics.createRoute("practice")) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressCard(
                    icon = Icons.Default.Extension,
                    title = "Cấp 1",
                    accentColor = Color(0xFF0191D8),
                    starCount = 1,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.QuizMenu.route) }
                )
                ProgressCard(
                    icon = Icons.Default.Diamond,
                    title = "Thống kê",
                    accentColor = Color(0xFF7D7AFF),
                    starCount = 0,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Analytics.route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        DailyChallengeBanner(
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HeaderSection(
    avatarUrl: String?,
    onLearnNow: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF64B5F6))),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buddy's World",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
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
                            Text("🐼", fontSize = 36.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Chào bé! Hôm nay mình\ncùng học tiếng Việt nhé!",
                            fontSize = 13.sp,
                            color = Color(0xFF444444),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.clickable { onLearnNow() },
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF4C84FF)
                        ) {
                            Text(
                                text = "▶  HỌC NGAY",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    progressFraction: Float? = null,
    starCount: Int? = null
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = title.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF222222)
            )

            if (starCount != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < starCount) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else if (progressFraction != null) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.15f)
                )
            }
        }
    }
}

@Composable
private fun DailyChallengeBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEBF2FF),
        border = BorderStroke(1.dp, Color(0xFFD0E1FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF0174BE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Thử thách ngày",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "Hoàn thành bài tập chữ A để nhận quà!",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    maxLines = 2
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
