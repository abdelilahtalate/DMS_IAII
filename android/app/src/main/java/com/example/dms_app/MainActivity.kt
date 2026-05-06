package com.example.dms_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dms_app.core.navigation.AppNavGraph
import com.example.dms_app.core.ui.theme.DriveMindTheme
import com.example.dms_app.core.ui.theme.ThemeViewModel
import com.example.dms_app.core.ui.theme.ThemeViewModelFactory

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appTheme by themeViewModel.currentTheme.collectAsStateWithLifecycle()

            DriveMindTheme(appTheme = appTheme) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.dms_app.core.ui.theme.LocalAppColors.current.background
                ) {
                    AppNavGraph(onToggleTheme = themeViewModel::toggleTheme)
                }
            }
        }
    }
}