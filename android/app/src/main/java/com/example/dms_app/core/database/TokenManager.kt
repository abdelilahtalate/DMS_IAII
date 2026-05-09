package com.example.dms_app.core.database

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth")

class TokenManager(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
        val USER_ID_KEY = intPreferencesKey("user_id")
        val EMAIL_KEY = stringPreferencesKey("email")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserId(userId: Int) {
        context.dataStore.edit {
            it[USER_ID_KEY] = userId
        }
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit {
            it[EMAIL_KEY] = email
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(USER_ID_KEY)
            it.remove(EMAIL_KEY)
        }
    }

    val tokenFlow = context.dataStore.data.map {
        it[TOKEN_KEY]
    }

    val userIdFlow = context.dataStore.data.map {
        it[USER_ID_KEY] ?: -1
    }

    val emailFlow = context.dataStore.data.map {
        it[EMAIL_KEY] ?: ""
    }
}