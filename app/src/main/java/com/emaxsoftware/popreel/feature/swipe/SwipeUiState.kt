package com.emaxsoftware.popreel.feature.swipe

import com.emaxsoftware.popreel.core.data.MovieFeed
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.ListState

data class SwipeUiState(
    val catalog: CatalogStatus = CatalogStatus(),
    val filter: DeckFilter = DeckFilter.Trending,
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

enum class DeckFilter(val label: String, val feed: MovieFeed, val genre: String? = null) {
    Trending("Trending", MovieFeed.Trending),
    SciFi("Sci-Fi", MovieFeed.Trending, genre = "Science Fiction"),
    TopRated("Top Rated", MovieFeed.TopRated),
    NewReleases("New Releases", MovieFeed.Newest),
}
