package com.emaxsoftware.popreel.feature.puzzle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.audio.GameSound
import com.emaxsoftware.popreel.core.audio.GameSounds
import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.SettingsRepository
import com.emaxsoftware.popreel.core.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class PosterPuzzleViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val settingsRepository: SettingsRepository,
    private val sounds: GameSounds,
) : ViewModel() {

    internal var random: Random = Random.Default

    private val _state = MutableStateFlow<PuzzleUiState>(PuzzleUiState.Loading)
    val state: StateFlow<PuzzleUiState> = _state.asStateFlow()

    private val settings = settingsRepository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    private var clockJob: Job? = null
    private var started = false

    fun onStart() {
        if (started) return
        started = true
        loadPuzzle(excludeId = null)
    }

    fun onNextPuzzle() {
        loadPuzzle(excludeId = (_state.value as? PuzzleUiState.Playing)?.movie?.id)
    }

    fun onTileTap(position: Int) {
        val playing = _state.value as? PuzzleUiState.Playing ?: return
        if (playing.solved) return
        val selected = playing.selected
        val next = when (selected) {
            null -> playing.copy(selected = position)
            position -> playing.copy(selected = null)
            else -> playing.copy(tiles = PuzzleRules.swap(playing.tiles, selected, position), selected = null, moves = playing.moves + 1)
        }
        _state.value = next
        if (next.solved) onSolved() else play(GameSound.Tap)
    }

    /** Stops the clock while the screen is in the background. */
    fun onPause() {
        clockJob?.cancel()
    }

    fun onResume() {
        val playing = _state.value as? PuzzleUiState.Playing ?: return
        if (!playing.solved) startClock()
    }

    private fun loadPuzzle(excludeId: String?) {
        clockJob?.cancel()
        _state.value = PuzzleUiState.Loading
        viewModelScope.launch {
            val movie = catalog.randomPuzzleMovie(excludeId).getOrNull()
            _state.value = if (movie == null) {
                PuzzleUiState.Unavailable
            } else {
                PuzzleUiState.Playing(movie, PuzzleRules.shuffledTiles(random))
            }
            startClock()
        }
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _state.update { s -> if (s is PuzzleUiState.Playing && !s.solved) s.copy(elapsedSeconds = s.elapsedSeconds + 1) else s }
            }
        }
    }

    private fun onSolved() {
        clockJob?.cancel()
        play(GameSound.Complete)
        viewModelScope.launch { settingsRepository.recordPuzzleSolved() }
    }

    private fun play(sound: GameSound) {
        if (settings.value.gameSoundEnabled) sounds.play(sound)
    }
}
