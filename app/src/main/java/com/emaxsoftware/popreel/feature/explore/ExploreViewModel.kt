package com.emaxsoftware.popreel.feature.explore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.MovieChoiceRepository
import com.emaxsoftware.popreel.core.data.MovieFeed
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.core.model.MovieTag
import com.emaxsoftware.popreel.core.model.TagStyle
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.ListState
import com.emaxsoftware.popreel.ui.state.asListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    choiceRepository: MovieChoiceRepository,
) : ViewModel(), ExploreActions {

    /** Snapshot state, not a flow: the text field must see each keystroke synchronously. */
    var query by mutableStateOf("")
        private set

    private val selectedGenre = MutableStateFlow(ExploreUiState.ALL_GENRES)
    private val sort = MutableStateFlow(CatalogSort.Trending)

    private val genreFilter: Flow<String?> =
        selectedGenre.map { genre -> genre.takeUnless { it == ExploreUiState.ALL_GENRES } }

    private val feed: Flow<ListState<Movie>> = combine(genreFilter, sort, ::Pair)
        .flatMapLatest { (genre, sort) ->
            catalog.observeFeed(sort.feed, genre, FEED_SIZE)
                .map { movies -> movies.withRankBadges(sort) }
                .asListState()
        }

    private val acclaimed: Flow<ListState<Movie>> = genreFilter.flatMapLatest { genre ->
        catalog.observeFeed(MovieFeed.TopRated, genre, ACCLAIMED_SIZE)
            .map { movies -> movies.withRankBadges(CatalogSort.TopRated, maxRanked = 3) }
            .asListState()
    }

    private val searchResults: Flow<ListState<Movie>?> =
        combine(snapshotFlow { query.trim() }, genreFilter, ::Pair).flatMapLatest { (text, genre) ->
            if (text.isEmpty()) {
                flowOf(null)
            } else {
                flow {
                    emit(ListState())
                    delay(SEARCH_DEBOUNCE_MS) // flatMapLatest cancels this wait when the user keeps typing
                    emitAll(catalog.observeSearch(text, genre, SEARCH_LIMIT).asListState())
                }
            }
        }

    private val catalogStatus: Flow<CatalogStatus> =
        combine(catalog.movieCount, catalog.syncState, ::CatalogStatus)

    private val filters: Flow<Filters> = combine(
        catalog.genres.catch { emit(emptyList()) },
        selectedGenre,
        sort,
    ) { genres, genre, sort -> Filters(listOf(ExploreUiState.ALL_GENRES) + genres, genre, sort) }

    val uiState: StateFlow<ExploreUiState> = combine(
        catalogStatus,
        filters,
        feed,
        acclaimed,
        combine(searchResults, choiceRepository.choices.catch { emit(emptyMap()) }, ::Pair),
    ) { status, filters, feed, acclaimed, (search, choices) ->
        ExploreUiState(
            catalog = status,
            genres = filters.genres,
            selectedGenre = filters.selectedGenre,
            sort = filters.sort,
            feed = feed,
            acclaimed = acclaimed,
            searchResults = search,
            choices = choices,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    override fun onQueryChange(query: String) {
        this.query = query
    }

    override fun onGenreSelect(genre: String) {
        selectedGenre.value = genre
    }

    override fun onSortSelect(sort: CatalogSort) {
        this.sort.value = sort
    }

    override fun onRetrySync() {
        viewModelScope.launch { catalog.sync() }
    }

    private data class Filters(val genres: List<String>, val selectedGenre: String, val sort: CatalogSort)

    private companion object {
        const val FEED_SIZE = 11 // 1 hero + 10 reel cards
        const val ACCLAIMED_SIZE = 20
        const val SEARCH_LIMIT = 40
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}

private fun List<Movie>.withRankBadges(sort: CatalogSort, maxRanked: Int = Int.MAX_VALUE): List<Movie> =
    mapIndexed { i, movie ->
        if (i >= maxRanked) return@mapIndexed movie
        val badge = when (sort) {
            CatalogSort.Trending -> MovieTag("#${i + 1} Trending", TagStyle.Accent)
            CatalogSort.TopRated -> MovieTag("#${i + 1} Rated", TagStyle.Accent)
            CatalogSort.Newest -> MovieTag("New ${movie.year}", TagStyle.Accent)
            CatalogSort.HiddenGems -> MovieTag("Hidden Gem", TagStyle.Accent)
        }
        movie.copy(badge = badge)
    }
