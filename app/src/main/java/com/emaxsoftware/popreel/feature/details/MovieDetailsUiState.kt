package com.emaxsoftware.popreel.feature.details

import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice

sealed interface MovieDetailsUiState {
    data object Loading : MovieDetailsUiState
    data object NotFound : MovieDetailsUiState
    data class Loaded(
        val movie: Movie,
        val choice: MovieChoice?,
        val similar: List<Movie>,
        val similarChoices: Map<String, MovieChoice>,
    ) : MovieDetailsUiState
}

sealed interface MovieDetailsEffect {
    data class ChoiceSaved(val movieTitle: String, val choice: MovieChoice?) : MovieDetailsEffect
    data object ChoiceFailed : MovieDetailsEffect
}
