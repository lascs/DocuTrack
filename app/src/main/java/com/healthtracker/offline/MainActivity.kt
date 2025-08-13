package com.healthtracker.offline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.healthtracker.offline.ui.theme.OfflineDoctorTrackerTheme
import com.healthtracker.offline.ui.navigation.OfflineDoctorTrackerNavigation
import com.healthtracker.offline.ui.components.shouldShowBottomNavigation
import com.healthtracker.offline.ui.components.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Offline Doctor Tracker app.
 * Entry point for the application with Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfflineDoctorTrackerTheme {
                OfflineDoctorTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDoctorTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomNavigation(currentRoute)) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        OfflineDoctorTrackerNavigation(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}