package com.emaxsoftware.popreel.feature.explore

import com.emaxsoftware.popreel.core.data.MovieFeed
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.ListState

data class ExploreUiState(
    val catalog: CatalogStatus = CatalogStatus(),
    val genres: List<String> = listOf(ALL_GENRES),
    val selectedGenre: String = ALL_GENRES,
    val sort: CatalogSort = CatalogSort.Trending,
    /** First item is the featured hero; the rest fill the horizontal reel. */
    val feed: ListState<Movie> = ListState(),
    val acclaimed: ListState<Movie> = ListState(),
    /** Non-null while a search is active. */
    val searchResults: ListState<Movie>? = null,
    val choices: Map<String, MovieChoice> = emptyMap(),
) {
    val featured: Movie? get() = feed.items.firstOrNull()
    val spotlight: List<Movie> get() = feed.items.drop(1)

    fun findMovie(id: String): Movie? =
        sequenceOf(feed.items, acclaimed.items, searchResults?.items.orEmpty())
            .flatten()
            .firstOrNull { it.id == id }

    companion object {
        const val ALL_GENRES = "All"
    }
}

enum class CatalogSort(val label: String, val feed: MovieFeed, val subtitle: String) {
    Trending("Trending Now", MovieFeed.Trending, "Most popular right now"),
    TopRated("Top Rated", MovieFeed.TopRated, "Highest rated with 1k+ votes"),
    Newest("Release Date", MovieFeed.Newest, "Latest releases in the catalog"),
    HiddenGems("Hidden Gems", MovieFeed.HiddenGems, "Well loved, less seen"),
}
