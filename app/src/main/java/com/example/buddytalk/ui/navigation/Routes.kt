package com.example.buddytalk.ui.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Home : Routes("home")
    object Settings : Routes("settings")
    object Analytics : Routes("analytics")
    object Topics : Routes("topics/{mode}?quizType={quizType}") {
        fun createRoute(mode: String, quizType: String? = null): String {
            return if (quizType.isNullOrBlank()) {
                "topics/$mode"
            } else {
                "topics/$mode?quizType=$quizType"
            }
        }
    }
    object Profile : Routes("profile")
    object QuizMenu : Routes("quiz_menu")
    object PracticePronunciation : Routes("practice_pronunciation/{topicId}/{type}") {
        fun createRoute(topicId: Long, type: String) = "practice_pronunciation/$topicId/$type"
    }
    object Lesson : Routes("lesson/{topicId}/{mode}") {
        fun createRoute(topicId: Long, mode: String) = "lesson/$topicId/$mode"
    }
    object Quiz : Routes("quiz/{type}") {
        fun createRoute(type: String) = "quiz/$type"
    }
}
