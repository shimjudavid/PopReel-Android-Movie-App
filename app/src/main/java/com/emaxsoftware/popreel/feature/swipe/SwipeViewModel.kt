package com.emaxsoftware.popreel.feature.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.MovieChoiceRepository
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.asListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val choiceRepository: MovieChoiceRepository,
) : ViewModel(), SwipeActions {

    private val filter = MutableStateFlow<DeckFilter>(DeckFilter.Mode.Trending)
    private val history = MutableStateFlow<List<SwipeRecord>>(emptyList())

    private val _effects = Channel<SwipeEffect>(Channel.BUFFERED)
    val effects: Flow<SwipeEffect> = _effects.receiveAsFlow()

    private val queue = filter.flatMapLatest { filter ->
        catalog.observeFeed(filter.feed, filter.genre, QUEUE_SIZE, excludeMarked = true).asListState()
    }

    private val review: Flow<List<ChoiceReviewItem>> = choiceRepository.history
        .catch { emit(emptyList()) }
        .flatMapLatest { marked ->
            val recent = marked.take(REVIEW_LIMIT)
            catalog.observeMovies(recent.map { it.first }).map { movies ->
                val byId = movies.associateBy { it.id }
                recent.mapNotNull { (id, choice) -> byId[id]?.let { ChoiceReviewItem(it, choice) } }
            }
        }

    val uiState: StateFlow<SwipeUiState> = combine(
        combine(catalog.movieCount, catalog.syncState, ::CatalogStatus),
        combine(filter, catalog.genres.catch { emit(emptyList()) }, ::Pair),
        queue,
        history,
        combine(choiceRepository.choices.catch { emit(emptyMap()) }, review, ::Pair),
    ) { status, (filter, genres), queue, history, (choices, review) ->
        SwipeUiState(status, filter, genres, queue, history, choices, review)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SwipeUiState())

    override fun onFilterSelect(filter: DeckFilter) {
        this.filter.value = filter
    }

    override fun onSwiped(movie: Movie, choice: MovieChoice) {
        val record = SwipeRecord(movie, choice)
        history.update { it + record }
        viewModelScope.launch {
            choiceRepository.setChoice(movie.id, choice).onFailure {
                history.update { it - record }
                _effects.send(SwipeEffect.ChoiceFailed)
            }
        }
    }

    override fun onUndo() {
        val last = history.value.lastOrNull() ?: return
        history.update { it.dropLast(1) }
        viewModelScope.launch {
            val effect = choiceRepository.setChoice(last.movie.id, null).fold(
                onSuccess = { SwipeEffect.Undone(last.movie.title, last.choice) },
                onFailure = {
                    history.update { it + last }
                    SwipeEffect.ChoiceFailed
                },
            )
            _effects.send(effect)
        }
    }

    override fun onChoiceChange(movie: Movie, choice: MovieChoice?) {
        viewModelScope.launch {
            val effect = choiceRepository.setChoice(movie.id, choice).fold(
                onSuccess = { SwipeEffect.ChoiceSaved(movie.title, choice) },
                onFailure = { SwipeEffect.ChoiceFailed },
            )
            _effects.send(effect)
        }
    }

    override fun onRetrySync() {
        viewModelScope.launch { catalog.sync() }
    }

    companion object {
        const val QUEUE_SIZE = 50
        private const val REVIEW_LIMIT = 200
    }
}
