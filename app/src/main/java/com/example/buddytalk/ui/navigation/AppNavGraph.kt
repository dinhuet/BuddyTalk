package com.example.buddytalk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.ui.navigation.Routes
import com.example.buddytalk.ui.screen.HomeScreen
import com.example.buddytalk.ui.screen.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                viewModel = userViewModel,
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) }
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(
                viewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        // Splash route can be added here when you have a SplashScreen
        composable(Routes.Splash.route) {
            // SplashScreen(...)
        }
    }
}