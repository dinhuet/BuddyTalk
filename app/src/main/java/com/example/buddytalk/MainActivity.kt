package com.example.buddytalk

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.buddytalk.data.viewModel.UserViewModel
import com.example.buddytalk.ui.navigation.AppNavGraph
import com.example.buddytalk.ui.theme.BuddyTalkTheme
import com.example.buddytalk.ui.component.BottomNavigationBar
import com.example.buddytalk.ui.navigation.Routes

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        
        setContent {
            BuddyTalkTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White,
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        userViewModel = userViewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .statusBarsPadding()
                    )
                }
            }
        }
    }
}
