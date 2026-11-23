package com.emaxsoftware.popreel.feature.swipe

import com.emaxsoftware.popreel.core.data.MovieFeed
import com.emaxsoftware.popreel.core.model.Genre
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.ListState

data class SwipeUiState(
    val catalog: CatalogStatus = CatalogStatus(),
    val filter: DeckFilter = DeckFilter.Mode.Trending,
    /** Genres the deck can be filtered to, most movies first. */
    val genres: List<Genre> = emptyList(),
    /** Unmarked movies for [filter], as stored locally. */
    val queue: ListState<Movie> = ListState(),
    /** This session's swipes, oldest first; backs undo. */
    val history: List<SwipeRecord> = emptyList(),
    val choices: Map<String, MovieChoice> = emptyMap(),
    /** Every marked movie, newest first, for the review sheet. */
    val review: List<ChoiceReviewItem> = emptyList(),
) {
    /**
     * Cards still to swipe. Swiped movies are hidden immediately rather than waiting for the
     * database to drop them, so the deck never flashes a card that was just thrown away.
     */
    val remaining: List<Movie>
        get() {
            val swiped = history.mapTo(HashSet()) { it.movie.id }
            return queue.items.filterNot { it.id in swiped || it.id in choices }
        }

    val ratedCount: Int get() = choices.size
    val canUndo: Boolean get() = history.isNotEmpty()

    /** "50+" when the queue is capped by the query limit. */
    val remainingLabel: String
        get() = if (queue.items.size >= SwipeViewModel.QUEUE_SIZE) "${remaining.size}+" else "${remaining.size}"
}

/** What the deck deals: a feed mode, or trending films in one genre. */
sealed interface DeckFilter {
    /** Chip key: a [Mode] label or a genre name. */
    val key: String
    val feed: MovieFeed
    val genre: String?

    enum class Mode(val label: String, override val feed: MovieFeed) : DeckFilter {
        Trending("Trending", MovieFeed.Trending),
        TopRated("Top Rated", MovieFeed.TopRated),
        NewReleases("New Releases", MovieFeed.Newest),
        ;

        override val key: String get() = label
        override val genre: String? get() = null
    }

    data class ByGenre(val name: String) : DeckFilter {
        override val key: String get() = name
        override val feed: MovieFeed get() = MovieFeed.Trending
        override val genre: String get() = name
    }

    companion object {
        fun fromKey(key: String): DeckFilter = Mode.entries.firstOrNull { it.label == key } ?: ByGenre(key)
    }
}
