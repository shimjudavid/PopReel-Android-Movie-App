package com.emaxsoftware.popreel.feature.swipe

import com.emaxsoftware.popreel.core.model.MovieChoice

sealed interface SwipeEffect {
    data class Undone(val movieTitle: String, val choice: MovieChoice) : SwipeEffect
    data class ChoiceSaved(val movieTitle: String, val choice: MovieChoice?) : SwipeEffect
    data object ChoiceFailed : SwipeEffect
}
