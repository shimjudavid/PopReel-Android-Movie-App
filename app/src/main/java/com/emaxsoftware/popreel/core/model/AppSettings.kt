package com.emaxsoftware.popreel.core.model

enum class ThemeMode { System, Light, Dark }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val gameSoundEnabled: Boolean = true,
)

data class ArcadeStats(
    val triviaTotalPoints: Int = 0,
    val triviaBestScore: Int = 0,
    val triviaBestStreak: Int = 0,
    val triviaRoundsPlayed: Int = 0,
    val puzzlesSolved: Int = 0,
)
