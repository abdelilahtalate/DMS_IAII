package com.example.dms_app.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class AppColors(
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val foreground: Color,
    val mutedForeground: Color,
    val cyan: Color,
    val cyanGlow: Color,
    val success: Color,
    val amberWarn: Color,
    val redAlert: Color,
    val border: Color,
    val cardBg: Color,
    val isDark: Boolean
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        background = DarkColors.Background,
        surface = DarkColors.Surface,
        surface2 = DarkColors.Surface2,
        surface3 = DarkColors.Surface3,
        foreground = DarkColors.Foreground,
        mutedForeground = DarkColors.MutedForeground,
        cyan = DarkColors.Cyan,
        cyanGlow = DarkColors.CyanGlow,
        success = DarkColors.Success,
        amberWarn = DarkColors.AmberWarn,
        redAlert = DarkColors.RedAlert,
        border = DarkColors.Border,
        cardBg = DarkColors.CardBg,
        isDark = true
    )
}

@Composable
fun DriveMindTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val isDark = appTheme == AppTheme.DARK
    
    val appColors = if (isDark) {
        AppColors(
            background = DarkColors.Background,
            surface = DarkColors.Surface,
            surface2 = DarkColors.Surface2,
            surface3 = DarkColors.Surface3,
            foreground = DarkColors.Foreground,
            mutedForeground = DarkColors.MutedForeground,
            cyan = DarkColors.Cyan,
            cyanGlow = DarkColors.CyanGlow,
            success = DarkColors.Success,
            amberWarn = DarkColors.AmberWarn,
            redAlert = DarkColors.RedAlert,
            border = DarkColors.Border,
            cardBg = DarkColors.CardBg,
            isDark = true
        )
    } else {
        AppColors(
            background = LightColors.Background,
            surface = LightColors.Surface,
            surface2 = LightColors.Surface2,
            surface3 = LightColors.Surface3,
            foreground = LightColors.Foreground,
            mutedForeground = LightColors.MutedForeground,
            cyan = LightColors.Cyan,
            cyanGlow = LightColors.CyanGlow,
            success = LightColors.Success,
            amberWarn = LightColors.AmberWarn,
            redAlert = LightColors.RedAlert,
            border = LightColors.Border,
            cardBg = LightColors.CardBg,
            isDark = false
        )
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            background = DarkColors.Background,
            surface = DarkColors.Surface,
            surfaceVariant = DarkColors.Surface2,
            primary = DarkColors.Cyan,
            onPrimary = Color.Black,
            secondary = DarkColors.Success,
            onSecondary = Color.Black,
            tertiary = DarkColors.AmberWarn,
            onTertiary = Color.Black,
            error = DarkColors.RedAlert,
            onError = Color.White,
            outline = DarkColors.Border,
            onBackground = DarkColors.Foreground,
            onSurface = DarkColors.Foreground,
            onSurfaceVariant = DarkColors.MutedForeground
        )
    } else {
        lightColorScheme(
            background = LightColors.Background,
            surface = LightColors.Surface,
            surfaceVariant = LightColors.Surface2,
            primary = LightColors.Cyan,
            onPrimary = Color.White,
            secondary = LightColors.Success,
            onSecondary = Color.White,
            tertiary = LightColors.AmberWarn,
            onTertiary = Color.White,
            error = LightColors.RedAlert,
            onError = Color.White,
            outline = LightColors.Border,
            onBackground = LightColors.Foreground,
            onSurface = LightColors.Foreground,
            onSurfaceVariant = LightColors.MutedForeground
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DriveMindTypography,
            shapes = DriveMindShapes,
            content = content
        )
    }
}
