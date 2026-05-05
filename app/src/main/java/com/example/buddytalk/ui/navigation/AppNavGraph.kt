package com.example.buddytalk.ui.navigation

import androidx.compose.runtime.*
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
import com.example.buddytalk.ui.screen.LessonScreen
import com.example.buddytalk.ui.screen.AnalyticsScreen
import com.example.buddytalk.ui.component.StreakDialog

@Composable
fun AppNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    var showStreakDialog by remember { mutableStateOf(false) }
    var currentStreak by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        userViewModel.streakUpdatedEvent.collect { streak ->
            currentStreak = streak
            showStreakDialog = true
        }
    }

    if (showStreakDialog) {
        StreakDialog(
            streakCount = currentStreak,
            onDismiss = { showStreakDialog = false }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier
    ) {
        composable(Routes.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Routes.Analytics.route) {
            AnalyticsScreen(navController = navController)
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
                onTopicClick = { topicId, learningMode ->
                    if (mode == "practice") {
                        navController.navigate(Routes.PracticePronunciation.createRoute(topicId, learningMode.lowercase()))
                    } else {
                        navController.navigate(Routes.Lesson.createRoute(topicId, learningMode))
                    }
                }
            )
        }
        
        composable(
            Routes.Lesson.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.LongType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
            val mode = backStackEntry.arguments?.getString("mode") ?: "IMAGE"
            LessonScreen(
                navController = navController,
                topicId = topicId,
                mode = mode,
                onLessonComplete = { userViewModel.completeLesson() }
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
                type = type,
                onLessonComplete = { userViewModel.completeLesson() }
            )
        }
        
        composable(Routes.Splash.route) {
            // SplashScreen(...)
        }
    }
}
