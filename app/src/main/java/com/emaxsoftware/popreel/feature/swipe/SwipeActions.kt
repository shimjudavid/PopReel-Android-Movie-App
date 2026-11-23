package com.emaxsoftware.popreel.feature.swipe

import androidx.compose.runtime.Stable
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice

@Stable
interface SwipeActions {
    fun onFilterSelect(filter: DeckFilter) {}
    fun onSwiped(movie: Movie, choice: MovieChoice) {}
    fun onUndo() {}
    fun onChoiceChange(movie: Movie, choice: MovieChoice?) {}
    fun onRetrySync() {}
}
