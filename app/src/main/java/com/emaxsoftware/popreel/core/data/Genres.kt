package com.emaxsoftware.popreel.core.data

import com.emaxsoftware.popreel.core.database.entity.GenreEntity
import com.emaxsoftware.popreel.core.database.entity.MovieEntity

/**
 * The catalog mixes TMDB and Wikipedia genre names; fold the stray spellings into the
 * TMDB name that most movies already use, so one filter finds them all.
 */
private val GENRE_ALIASES = mapOf(
    "Animated" to "Animation",
    "Historical" to "History",
    "Musical" to "Music",
)

internal fun normalizeGenres(names: List<String>): List<String> =
    names.map { it.trim() }.filter { it.isNotEmpty() }.map { GENRE_ALIASES[it] ?: it }.distinct()

/** Genres with too few movies make filters that show a near-empty screen. */
internal const val MIN_MOVIES_PER_GENRE = 20

/** Genres offered as filters: those with at least [minMovies] movies, most movies first. */
internal fun browsableGenres(movies: List<MovieEntity>, minMovies: Int = MIN_MOVIES_PER_GENRE): List<GenreEntity> =
    movies.flatMap { it.genreNames() }
        .groupingBy { it }.eachCount()
        .filterValues { it >= minMovies }
        .map { (name, count) -> GenreEntity(name, count) }
        .sortedWith(compareByDescending<GenreEntity> { it.movieCount }.thenBy { it.name })
