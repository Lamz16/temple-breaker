package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "temple_breaker_prefs")

class HighScoreRepository(private val context: Context) {
    companion object {
        private val HIGH_SCORE_KEY = intPreferencesKey("key_high_score")
        private val GAMES_PLAYED_KEY = intPreferencesKey("key_games_played")
    }

    val highScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HIGH_SCORE_KEY] ?: 0
    }

    suspend fun saveHighScoreIfGreater(newScore: Int): Boolean {
        var isNewRecord = false
        context.dataStore.edit { preferences ->
            val currentHigh = preferences[HIGH_SCORE_KEY] ?: 0
            if (newScore > currentHigh) {
                preferences[HIGH_SCORE_KEY] = newScore
                isNewRecord = true
            }
            val games = preferences[GAMES_PLAYED_KEY] ?: 0
            preferences[GAMES_PLAYED_KEY] = games + 1
        }
        return isNewRecord
    }
}
