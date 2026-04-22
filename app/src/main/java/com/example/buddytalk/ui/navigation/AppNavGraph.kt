package com.example.buddytalk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.data.viewModel.TopicViewModel
import com.example.buddytalk.ui.screen.HomeScreen
import com.example.buddytalk.ui.screen.SettingsScreen
import com.example.buddytalk.ui.screen.TopicScreen
import androidx.lifecycle.viewmodel.compose.viewModel

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
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) },
                onNavigateToTopics = { navController.navigate(Routes.Topics.route) }
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(
                viewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Topics.route) {
            val topicViewModel: TopicViewModel = viewModel()
            TopicScreen(
                viewModel = topicViewModel,
                onTopicClick = { topicId ->
                    // Navigate to lessons of this topic
                }
            )
        }
        composable(Routes.Splash.route) {
            // SplashScreen(...)
        }
    }
}
