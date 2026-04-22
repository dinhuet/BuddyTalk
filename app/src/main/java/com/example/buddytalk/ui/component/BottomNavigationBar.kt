package com.example.buddytalk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.buddytalk.ui.navigation.Routes
import com.example.buddytalk.ui.theme.HeaderBlue

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Box ngoài cùng để đảm bảo nền trắng phủ hết phần phím hệ thống (xóa nền đen)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(bottom = 8.dp) // Khoảng cách nhẹ với cạnh dưới
    ) {
        // Thanh điều hướng chính "nổi" lên
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFF3F4F6), // Màu xám rất nhẹ để nổi trên nền trắng
            shadowElevation = 4.dp // Tạo độ nổi
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Profile Item (Trái)
                BottomNavItem(
                    icon = Icons.Default.Person,
                    isSelected = currentRoute == Routes.Profile.route,
                    onClick = {
                        if (currentRoute != Routes.Profile.route) {
                            navController.navigate(Routes.Profile.route)
                        }
                    }
                )

                // 2. Home Item (Giữa)
                BottomNavItem(
                    icon = Icons.Default.Home,
                    isSelected = currentRoute == Routes.Home.route,
                    onClick = {
                        if (currentRoute != Routes.Home.route) {
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.Home.route) { inclusive = true }
                            }
                        }
                    }
                )

                // 3. Back Item (Phải)
                BottomNavItem(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    isSelected = false,
                    onClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            // Khi chọn thì hiện nền xanh nhạt, không chọn thì trong suốt
            .background(if (isSelected) HeaderBlue.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) HeaderBlue else Color(0xFF9CA3AF), // Màu icon xám dịu hơn
            modifier = Modifier.size(26.dp)
        )
    }
}
