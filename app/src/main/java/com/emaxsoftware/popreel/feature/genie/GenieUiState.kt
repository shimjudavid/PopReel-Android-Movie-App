package com.emaxsoftware.popreel.feature.genie

import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.core.model.Recommendation
import com.emaxsoftware.popreel.core.model.TasteFactor
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.ListState

data class GenieUiState(
    val catalog: CatalogStatus = CatalogStatus(),
    val likedCount: Int = 0,
    val dislikedCount: Int = 0,
    val markedCount: Int = 0,
    val recommendations: ListState<Recommendation> = ListState(),
    /** Strongest positive signals, offered as filters. */
    val tasteFactors: List<TasteFactor> = emptyList(),
    val selectedFactor: TasteFactor? = null,
) {
    val hasPreferences: Boolean get() = likedCount > 0

    val visible: List<Recommendation>
        get() = selectedFactor?.let { factor -> recommendations.items.filter { factor in it.factors } }
            ?: recommendations.items

    val level: Int get() = markedCount / FILMS_PER_LEVEL + 1
    val filmsToNextLevel: Int get() = level * FILMS_PER_LEVEL - markedCount
    val levelProgress: Float get() = (markedCount % FILMS_PER_LEVEL) / FILMS_PER_LEVEL.toFloat()

    companion object {
        const val FILMS_PER_LEVEL = 10
    }
}

sealed interface GenieEffect {
    data class ChoiceSaved(val movieTitle: String, val choice: MovieChoice) : GenieEffect
    data object ChoiceFailed : GenieEffect
}
