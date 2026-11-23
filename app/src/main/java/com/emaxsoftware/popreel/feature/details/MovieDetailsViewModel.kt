package com.emaxsoftware.popreel.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.MovieChoiceRepository
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = MovieDetailsViewModel.Factory::class)
class MovieDetailsViewModel @AssistedInject constructor(
    @Assisted movieId: String,
    catalog: CatalogRepository,
    private val choiceRepository: MovieChoiceRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(movieId: String): MovieDetailsViewModel
    }

    private val _effects = Channel<MovieDetailsEffect>(Channel.BUFFERED)
    val effects: Flow<MovieDetailsEffect> = _effects.receiveAsFlow()

    private val choices = choiceRepository.choices.catch { emit(emptyMap()) }

    val uiState: StateFlow<MovieDetailsUiState> = catalog.observeMovie(movieId)
        .flatMapLatest { movie ->
            if (movie == null) {
                flowOf(MovieDetailsUiState.NotFound)
            } else {
                combine(catalog.observeSimilar(movie, SIMILAR_COUNT).catch { emit(emptyList()) }, choices) { similar, choices ->
                    MovieDetailsUiState.Loaded(movie, choices[movie.id], similar, choices)
                }
            }
        }
        .catch { emit(MovieDetailsUiState.NotFound) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MovieDetailsUiState.Loading)

    fun onChoiceChange(movie: Movie, choice: MovieChoice?) {
        viewModelScope.launch {
            val effect = choiceRepository.setChoice(movie.id, choice).fold(
                onSuccess = { MovieDetailsEffect.ChoiceSaved(movie.title, choice) },
                onFailure = { MovieDetailsEffect.ChoiceFailed },
            )
            _effects.send(effect)
        }
    }

    private companion object {
        const val SIMILAR_COUNT = 6
    }
}
