package com.example.dms_app.core.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dms_app.core.ui.components.BottomNavBar
import com.example.dms_app.core.ui.components.NavDestination
import com.example.dms_app.feature_livedrive.presentation.LiveDriveScreen

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    previewUrl: String
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentDestination = when (currentRoute) {
        "dashboard" -> NavDestination.HOME
        "livedrive" -> NavDestination.DRIVE
        "trips" -> NavDestination.TRIPS
        "insights" -> NavDestination.INSIGHTS
        "profile" -> NavDestination.PROFILE
        else -> NavDestination.HOME
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Feature Content
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("dashboard") {
                com.example.dms_app.feature_home.presentation.HomeScreen(
                    onStartDrive = {
                        navController.navigate("livedrive")
                    },
                    modifier = Modifier.padding(bottom = 90.dp)
                )
            }
            
            composable("livedrive") {
                LiveDriveScreen(
                    onNavigateToPreview = {
                        val encoded = URLEncoder.encode(previewUrl, StandardCharsets.UTF_8.toString())
                        rootNavController.navigate("preview/$encoded")
                    },
                    modifier = Modifier.padding(bottom = 90.dp) // Space for BottomNavBar
                )
            }
            
            composable("trips") {
                PlaceholderScreen("Trips", Modifier.padding(bottom = 90.dp))
            }
            
            composable("insights") {
                PlaceholderScreen("Insights", Modifier.padding(bottom = 90.dp))
            }
            
            composable("profile") {
                PlaceholderScreen("Profile", Modifier.padding(bottom = 90.dp))
            }
        }

        // Floating Bottom Navigation
        BottomNavBar(
            current = currentDestination,
            onNavigate = { dest ->
                if (dest == NavDestination.DRIVE) {
                    navController.navigate("livedrive")
                } else {
                    val route = when(dest) {
                        NavDestination.HOME -> "dashboard"
                        NavDestination.TRIPS -> "trips"
                        NavDestination.INSIGHTS -> "insights"
                        NavDestination.PROFILE -> "profile"
                        else -> "dashboard"
                    }
                    navController.navigate(route) {
                        popUpTo("dashboard") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun PlaceholderScreen(name: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text(text = "$name Screen coming soon", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
    }
}
