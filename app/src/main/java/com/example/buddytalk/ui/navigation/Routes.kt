package com.example.buddytalk.ui.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Home : Routes("home")
    object Settings : Routes("settings")
    object Topics : Routes("topics/{mode}") {
        fun createRoute(mode: String) = "topics/$mode"
    }
    object Profile : Routes("profile")
    object PracticePronunciation : Routes("practice_pronunciation/{topicId}/{type}") {
        fun createRoute(topicId: Long, type: String) = "practice_pronunciation/$topicId/$type"
    }
    object Lesson : Routes("lesson/{topicId}/{mode}") {
        fun createRoute(topicId: Long, mode: String) = "lesson/$topicId/$mode"
    }
}
