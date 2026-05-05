package com.example.dms_app.core.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTheme { DARK, LIGHT }

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferenceRepository(private val context: Context) {
    private val THEME_KEY = stringPreferencesKey("app_theme")

    val themeFlow: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: AppTheme.DARK.name
            AppTheme.valueOf(themeName)
        }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}

class ThemeViewModel(private val repo: ThemePreferenceRepository) : ViewModel() {
    val currentTheme: StateFlow<AppTheme> = repo.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.DARK
        )

    fun toggleTheme() {
        viewModelScope.launch {
            val nextTheme = if (currentTheme.value == AppTheme.DARK) AppTheme.LIGHT else AppTheme.DARK
            repo.setTheme(nextTheme)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            repo.setTheme(theme)
        }
    }
}

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(ThemePreferenceRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
