package com.example.buddytalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.ui.navigation.AppNavGraph
import com.example.buddytalk.ui.theme.BuddyTalkTheme

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuddyTalkTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    userViewModel = userViewModel
                )
            }
        }
    }
}