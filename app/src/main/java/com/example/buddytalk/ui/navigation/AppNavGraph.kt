package com.example.buddytalk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.data.viewModel.TopicViewModel
import com.example.buddytalk.ui.screen.SettingsScreen
import com.example.buddytalk.ui.screen.TopicScreen
import com.example.buddytalk.ui.screen.HomeScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buddytalk.ui.screen.ProfileScreen
import com.example.buddytalk.ui.screen.PracticePronunciationScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        composable(Routes.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Routes.Profile.route) {
            ProfileScreen(
                viewModel = userViewModel,
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) },
                onNavigateToTopics = { navController.navigate(Routes.Topics.createRoute("learn")) },
                navController = navController
            )
        }
        composable(Routes.Settings.route) {
            SettingsScreen(
                viewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Routes.Topics.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "learn"
            val topicViewModel: TopicViewModel = viewModel()
            TopicScreen(
                viewModel = topicViewModel,
                mode = mode,
                onTopicClick = { topicId ->
                    if (mode == "practice") {
                        // Lấy type từ trạng thái chọn trong TopicScreen hoặc truyền mặc định
                        // Ở đây giả định nếu đang ở mode practice thì navigate tới màn luyện phát âm
                        navController.navigate(Routes.PracticePronunciation.createRoute(topicId, "vocabulary"))
                    } else {
                        // Navigate to normal lessons
                    }
                }
            )
        }
        
        composable(
            Routes.PracticePronunciation.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
            val type = backStackEntry.arguments?.getString("type") ?: "vocabulary"
            PracticePronunciationScreen(
                navController = navController,
                topicId = topicId,
                type = type
            )
        }
        
        composable(Routes.Splash.route) {
            // SplashScreen(...)
        }
    }
}
