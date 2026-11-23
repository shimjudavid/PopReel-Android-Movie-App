package com.emaxsoftware.popreel.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emaxsoftware.popreel.core.model.AppSettings
import com.emaxsoftware.popreel.core.model.ArcadeStats
import com.emaxsoftware.popreel.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

interface SettingsRepository {
    val settings: Flow<AppSettings>
    val arcadeStats: Flow<ArcadeStats>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setGameSoundEnabled(enabled: Boolean)
    suspend fun recordTriviaRound(score: Int, bestStreak: Int)
    suspend fun recordPuzzleSolved()
}

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private val preferences: Flow<Preferences> =
        dataStore.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    override val settings: Flow<AppSettings> = preferences.map { p ->
        AppSettings(
            themeMode = p[THEME]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.System,
            gameSoundEnabled = p[SOUND] ?: true,
        )
    }

    override val arcadeStats: Flow<ArcadeStats> = preferences.map { p ->
        ArcadeStats(
            triviaTotalPoints = p[TRIVIA_TOTAL] ?: 0,
            triviaBestScore = p[TRIVIA_BEST] ?: 0,
            triviaBestStreak = p[TRIVIA_STREAK] ?: 0,
            triviaRoundsPlayed = p[TRIVIA_ROUNDS] ?: 0,
            puzzlesSolved = p[PUZZLES] ?: 0,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME] = mode.name }
    }

    override suspend fun setGameSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[SOUND] = enabled }
    }

    override suspend fun recordTriviaRound(score: Int, bestStreak: Int) {
        dataStore.edit { p ->
            p[TRIVIA_TOTAL] = (p[TRIVIA_TOTAL] ?: 0) + score
            p[TRIVIA_BEST] = maxOf(p[TRIVIA_BEST] ?: 0, score)
            p[TRIVIA_STREAK] = maxOf(p[TRIVIA_STREAK] ?: 0, bestStreak)
            p[TRIVIA_ROUNDS] = (p[TRIVIA_ROUNDS] ?: 0) + 1
        }
    }

    override suspend fun recordPuzzleSolved() {
        dataStore.edit { p -> p[PUZZLES] = (p[PUZZLES] ?: 0) + 1 }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
        val SOUND = booleanPreferencesKey("game_sound")
        val TRIVIA_TOTAL = intPreferencesKey("trivia_total_points")
        val TRIVIA_BEST = intPreferencesKey("trivia_best_score")
        val TRIVIA_STREAK = intPreferencesKey("trivia_best_streak")
        val TRIVIA_ROUNDS = intPreferencesKey("trivia_rounds")
        val PUZZLES = intPreferencesKey("puzzles_solved")
    }
}
