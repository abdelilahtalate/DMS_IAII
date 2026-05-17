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

import com.example.dms_app.core.ui.theme.AppTheme

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    previewUrl: String,
    appTheme: AppTheme,
    onToggleTheme: () -> Unit,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Scoped to MainScreen lifecycle to persist drive state across tabs
    val liveDriveVm: com.example.dms_app.feature_livedrive.presentation.LiveDriveViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val currentDestination = when {
        currentRoute == "dashboard" -> NavDestination.HOME
        currentRoute == "livedrive" -> NavDestination.DRIVE
        currentRoute == "trips" || currentRoute?.startsWith("trip_details") == true -> NavDestination.TRIPS
        currentRoute == "insights" -> NavDestination.INSIGHTS
        currentRoute == "profile" -> NavDestination.PROFILE
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
                    onTripClick = { tripId ->
                        navController.navigate("trip_details/$tripId")
                    },
                    onStartDrive = {
                        navController.navigate("livedrive")
                    },
                    isDarkTheme = appTheme == AppTheme.DARK,
                    onToggleTheme = onToggleTheme,
                    modifier = Modifier.padding(bottom = 90.dp)
                )
            }
            
            composable("livedrive") {
                LiveDriveScreen(
                    vm = liveDriveVm,
                    onNavigateToPreview = {
                        val encoded = URLEncoder.encode(previewUrl, StandardCharsets.UTF_8.toString())
                        rootNavController.navigate("preview/$encoded")
                    },
                    modifier = Modifier.padding(bottom = 90.dp) // Space for BottomNavBar
                )
            }
            
            composable("trips") {
                com.example.dms_app.feature_trips.presentation.TripsListScreen(
                    onNavigateToDetails = { tripId ->
                        navController.navigate("trip_details/$tripId")
                    },
                    modifier = Modifier.padding(bottom = 90.dp)
                )
            }
            
            composable("trip_details/{tripId}") { backStack ->
                val tripId = backStack.arguments?.getString("tripId") ?: ""
                com.example.dms_app.feature_trips.presentation.TripDetailsScreen(
                    tripId = tripId,
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.padding(bottom = 90.dp)
                )
            }
            
            composable("insights") {
                com.example.dms_app.feature_insights.presentation.InsightsScreen(
                    modifier = Modifier.padding(bottom = 90.dp)
                )
            }
            
            composable("profile") {
                com.example.dms_app.feature_profile.presentation.ProfileScreen(
                    onToggleTheme = onToggleTheme,
                    isDarkTheme = appTheme == AppTheme.DARK,
                    onSignOut = onSignOut,
                    onNavigateToSettings = {
                        navController.navigate("alert_settings")
                    },
                    modifier = Modifier.padding(bottom = 90.dp)
                )
            }

            composable("alert_settings") {
                com.example.dms_app.feature_settings.presentation.AlertSettingsScreen(
                    onNavigate = { dest ->
                        val route = when(dest) {
                            NavDestination.HOME -> "dashboard"
                            NavDestination.TRIPS -> "trips"
                            NavDestination.INSIGHTS -> "insights"
                            NavDestination.PROFILE -> "profile"
                            NavDestination.DRIVE -> "livedrive"
                        }
                        navController.navigate(route)
                    },
                    modifier = Modifier.padding(bottom = 90.dp)
                )
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
    com.example.dms_app.core.ui.components.DriveMindBackground {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val colors = com.example.dms_app.core.ui.theme.LocalAppColors.current
            androidx.compose.material3.Text(
                text = "$name Screen coming soon", 
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = colors.foreground
            )
        }
    }
}
