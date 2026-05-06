package com.example.dms_app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.dms_app.feature_auth.presentation.login.LoginScreen
import com.example.dms_app.feature_auth.presentation.signup.SignupScreen
import com.example.dms_app.feature_auth.presentation.splash.SplashScreen
import com.example.dms_app.feature_device.presentation.pairing.DevicePairingScreen
import com.example.dms_app.feature_device.presentation.qr_scan.QrScannerScreen
import com.example.dms_app.feature_device.presentation.pairing.PairingScreen
import com.example.dms_app.feature_drive.presentation.camera_preview.CameraPreviewScreen
import com.example.dms_app.feature_livedrive.presentation.LiveDriveScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import  com.example.dms_app.core.navigation.MainScreen

import com.example.dms_app.feature_auth.presentation.onboarding.OnboardingScreen

@Composable
fun AppNavGraph(onToggleTheme: () -> Unit) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("qr")
                },
                onNavigateToSignup = {
                    navController.navigate("signup")
                }
            )
        }

        composable("device_pairing") {
            DevicePairingScreen(
                onNavigateHome = {
                    // Navigate to home with a placeholder URL
                    navController.navigate("home/placeholder") {
                        popUpTo("device_pairing") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate("qr") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("qr") {
            QrScannerScreen { deviceId, secret ->
                navController.navigate("pair/$deviceId/$secret")
            }
        }

        composable("pair/{deviceId}/{secret}") { backStack ->
            val deviceId = backStack.arguments?.getString("deviceId")!!
            val secret = backStack.arguments?.getString("secret")!!

            PairingScreen(
                deviceId = deviceId,
                secret = secret,
                onPairingSuccess = { previewUrl ->
                    val encodedUrl = URLEncoder.encode(previewUrl, StandardCharsets.UTF_8.toString())
                    navController.navigate("preview/$encodedUrl") {
                        popUpTo("qr") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

// ... in NavHost ...
        composable("home/{previewUrl}") { backStack ->
            val previewUrl = backStack.arguments?.getString("previewUrl")!!
            MainScreen(
                rootNavController = navController,
                previewUrl = previewUrl,
                onToggleTheme = onToggleTheme
            )
        }

        composable("preview/{url}") { backStack ->
            val url = backStack.arguments?.getString("url")!!
            CameraPreviewScreen(
                previewUrl = url,
                onSetupComplete = {
                    val encodedForHome = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("home/$encodedForHome") {
                        popUpTo("preview/{url}") { inclusive = true }
                    }
                }
            )
        }
    }
}