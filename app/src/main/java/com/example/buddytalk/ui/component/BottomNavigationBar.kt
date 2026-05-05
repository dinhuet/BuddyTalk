package com.example.buddytalk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.buddytalk.ui.navigation.Routes

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Trang chủ",
                icon = Icons.Default.Home,
                isSelected = currentRoute == Routes.Home.route,
                onClick = {
                    if (currentRoute != Routes.Home.route) {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )

            BottomNavItem(
                label = "Học",
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                isSelected = currentRoute?.startsWith("topics") == true || currentRoute?.startsWith("lesson") == true,
                onClick = {
                    if (currentRoute != Routes.Topics.route) {
                        navController.navigate(Routes.Topics.createRoute("learn")) {
                            launchSingleTop = true
                        }
                    }
                }
            )

            BottomNavItem(
                label = "Thống kê",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                isSelected = currentRoute == Routes.Analytics.route,
                onClick = {
                    if (currentRoute != Routes.Analytics.route) {
                        navController.navigate(Routes.Analytics.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )

            BottomNavItem(
                label = "Trang cá nhân",
                icon = Icons.Default.Person,
                isSelected = currentRoute == Routes.Profile.route,
                onClick = {
                    if (currentRoute != Routes.Profile.route) {
                        navController.navigate(Routes.Profile.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.Black else Color(0xFF9CA3AF),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else Color(0xFF9CA3AF)
        )
    }
}
