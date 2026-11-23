package com.emaxsoftware.popreel.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateList

/** Full-screen destinations pushed above the tabs; each has its own back navigation. */
sealed interface Screen {
    data class MovieDetails(val movieId: String) : Screen
    data object Trivia : Screen
    data object PosterPuzzle : Screen
    data object Settings : Screen
}

private const val DETAILS_PREFIX = "details:"

private fun Screen.encode(): String = when (this) {
    is Screen.MovieDetails -> DETAILS_PREFIX + movieId
    Screen.Trivia -> "trivia"
    Screen.PosterPuzzle -> "puzzle"
    Screen.Settings -> "settings"
}

private fun decode(value: String): Screen? = when {
    value.startsWith(DETAILS_PREFIX) -> Screen.MovieDetails(value.removePrefix(DETAILS_PREFIX))
    value == "trivia" -> Screen.Trivia
    value == "puzzle" -> Screen.PosterPuzzle
    value == "settings" -> Screen.Settings
    else -> null
}

/** Survives configuration changes and process death as a list of strings. */
val ScreenStackSaver: Saver<SnapshotStateList<Screen>, ArrayList<String>> = Saver(
    save = { stack -> ArrayList(stack.map { it.encode() }) },
    restore = { saved -> mutableStateListOf<Screen>().apply { addAll(saved.mapNotNull(::decode)) } },
)
