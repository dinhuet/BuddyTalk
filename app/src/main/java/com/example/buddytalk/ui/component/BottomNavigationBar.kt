package com.example.buddytalk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.buddytalk.ui.navigation.Routes
import com.example.buddytalk.ui.theme.HeaderBlue

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Item
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

            // Profile Item (Middle/Highlighted in original code)
            BottomNavItem(
                icon = Icons.Default.Person,
                isSelected = currentRoute == Routes.Profile.route,
                isHighlighted = true,
                onClick = {
                    if (currentRoute != Routes.Profile.route) {
                        navController.navigate(Routes.Profile.route)
                    }
                }
            )

            // Other Item (e.g. Topics or Learning)
            BottomNavItem(
                icon = Icons.Default.PlayArrow,
                isSelected = currentRoute == Routes.Topics.route,
                onClick = {
                    if (currentRoute != Routes.Topics.route) {
                        navController.navigate(Routes.Topics.route)
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    if (isHighlighted) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) HeaderBlue else HeaderBlue.copy(alpha = 0.7f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) HeaderBlue else Color.LightGray
            )
        }
    }
}

@Composable
fun IconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.clickable { onClick() }, contentAlignment = Alignment.Center) {
        content()
    }
}
