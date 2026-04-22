package com.example.buddytalk.ui.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Home : Routes("home")
    object Settings : Routes("settings")
    object Topics : Routes("topics")
}
