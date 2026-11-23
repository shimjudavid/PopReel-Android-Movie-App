package com.emaxsoftware.popreel.feature.genie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.MovieChoiceRepository
import com.emaxsoftware.popreel.core.data.MovieFeed
import com.emaxsoftware.popreel.core.data.RecommendationEngine
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.core.model.Recommendation
import com.emaxsoftware.popreel.core.model.TasteFactor
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.ListState
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GenieViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val choiceRepository: MovieChoiceRepository,
) : ViewModel() {

    private val selectedFactor = MutableStateFlow<TasteFactor?>(null)

    private val _effects = Channel<GenieEffect>(Channel.BUFFERED)
    val effects: Flow<GenieEffect> = _effects.receiveAsFlow()

    private val choices = choiceRepository.choices.catch { emit(emptyMap()) }

    private data class Taste(val recommendations: List<Recommendation>, val factors: List<TasteFactor>)

    private val taste: Flow<ListState<Taste>> = choices.flatMapLatest { marked ->
        val likedIds = marked.filterValues { it == MovieChoice.Liked }.keys.toList()
        val dislikedIds = marked.filterValues { it == MovieChoice.Disliked }.keys.toList()
        combine(
            catalog.observeMovies(likedIds),
            catalog.observeMovies(dislikedIds),
            // excludeMarked keeps liked, disliked and unseen films out of the candidates.
            catalog.observeFeed(MovieFeed.Trending, genre = null, limit = CANDIDATE_POOL, excludeMarked = true),
        ) { liked, disliked, candidates ->
            listOf(
                Taste(
                    recommendations = RecommendationEngine.recommend(liked, disliked, candidates, MAX_RECOMMENDATIONS),
                    factors = topFactors(liked, disliked),
                ),
            )
        }.asListState()
    }

    val uiState: StateFlow<GenieUiState> = combine(
        combine(catalog.movieCount, catalog.syncState, ::CatalogStatus),
        choices,
        taste,
        selectedFactor,
    ) { status, marked, taste, selected ->
        val result = taste.items.firstOrNull()
        GenieUiState(
            catalog = status,
            likedCount = marked.count { it.value == MovieChoice.Liked },
            dislikedCount = marked.count { it.value == MovieChoice.Disliked },
            markedCount = marked.size,
            recommendations = ListState(result?.recommendations.orEmpty(), taste.isLoading, taste.error),
            tasteFactors = result?.factors.orEmpty(),
            selectedFactor = selected?.takeIf { it in result?.factors.orEmpty() },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GenieUiState())

    fun onFactorSelect(factor: TasteFactor?) {
        selectedFactor.value = factor
    }

    fun onChoice(movie: Movie, choice: MovieChoice) {
        viewModelScope.launch {
            val effect = choiceRepository.setChoice(movie.id, choice).fold(
                onSuccess = { GenieEffect.ChoiceSaved(movie.title, choice) },
                onFailure = { GenieEffect.ChoiceFailed },
            )
            _effects.send(effect)
        }
    }

    fun onRetrySync() {
        viewModelScope.launch { catalog.sync() }
    }

    private fun topFactors(liked: List<Movie>, disliked: List<Movie>): List<TasteFactor> {
        val positive = RecommendationEngine.tasteProfile(liked, disliked)
            .filterValues { it > 0 }
            .entries.sortedByDescending { it.value }
            .map { it.key }
        return positive.filter { it.type == TasteFactor.Type.Director }.take(2) +
            positive.filter { it.type == TasteFactor.Type.Genre }.take(3) +
            positive.filter { it.type == TasteFactor.Type.Actor }.take(1)
    }

    private companion object {
        const val CANDIDATE_POOL = 800
        const val MAX_RECOMMENDATIONS = 30
    }
}
