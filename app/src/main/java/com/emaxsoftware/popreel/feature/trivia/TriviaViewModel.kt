package com.emaxsoftware.popreel.feature.trivia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.audio.GameSound
import com.emaxsoftware.popreel.core.audio.GameSounds
import com.emaxsoftware.popreel.core.data.SettingsRepository
import com.emaxsoftware.popreel.core.data.TriviaRepository
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
class TriviaViewModel @Inject constructor(
    private val repository: TriviaRepository,
    private val settingsRepository: SettingsRepository,
    private val sounds: GameSounds,
) : ViewModel(), TriviaActions {

    internal var random: Random = Random.Default

    private val _state = MutableStateFlow(TriviaUiState())
    val state: StateFlow<TriviaUiState> = _state.asStateFlow()

    private val settings = settingsRepository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private var timerJob: Job? = null
    private var advanceJob: Job? = null
    private var roundStarted = false

    /** Called when the screen first shows; a no-op if a round is already under way (e.g. after rotation). */
    fun onStart() {
        if (roundStarted) return
        roundStarted = true
        startRound()
    }

    override fun onPlayAgain() = startRound()

    override fun onSelect(option: Int) {
        val s = _state.value
        if (s.phase != TriviaPhase.Question || s.paused || option in s.eliminated) return
        _state.update { it.copy(selected = option) }
        play(GameSound.Tap)
    }

    override fun onLockIn() {
        val s = _state.value
        if (s.phase != TriviaPhase.Question || s.paused) return
        s.selected?.let(::lock)
    }

    override fun onFiftyFifty() {
        val s = _state.value
        val question = s.current ?: return
        if (s.phase != TriviaPhase.Question || s.fiftyFiftyUsed || s.paused) return
        val removed = (0 until question.options.size)
            .filter { it != question.answerIndex }
            .shuffled(random)
            .take(2)
            .toSet()
        _state.update { it.copy(eliminated = removed, fiftyFiftyUsed = true, selected = it.selected?.takeUnless { sel -> sel in removed }) }
    }

    /** While held, the answer reveal stays on screen; releasing resumes the countdown to the next question. */
    override fun onRevealHold(held: Boolean) {
        if (_state.value.phase != TriviaPhase.Revealed) return
        _state.update { it.copy(revealHeld = held) }
        if (held) advanceJob?.cancel() else scheduleAdvance(TriviaRules.REVEAL_AFTER_HOLD_MILLIS)
    }

    override fun onNext() {
        if (_state.value.phase != TriviaPhase.Revealed) return
        advanceJob?.cancel()
        advance()
    }

    override fun onPause() {
        val s = _state.value
        if (s.paused || (s.phase != TriviaPhase.Question && s.phase != TriviaPhase.Revealed)) return
        timerJob?.cancel()
        advanceJob?.cancel()
        _state.update { it.copy(paused = true, revealHeld = false) }
    }

    override fun onResume() {
        if (!_state.value.paused) return
        _state.update { it.copy(paused = false) }
        when (_state.value.phase) {
            TriviaPhase.Question -> startTimer()
            TriviaPhase.Revealed -> scheduleAdvance(TriviaRules.REVEAL_AFTER_HOLD_MILLIS)
            else -> Unit
        }
    }

    private fun startRound() {
        timerJob?.cancel()
        advanceJob?.cancel()
        _state.value = TriviaUiState(phase = TriviaPhase.Loading)
        viewModelScope.launch {
            val questions = repository.newRound(TriviaRules.QUESTIONS_PER_ROUND).getOrDefault(emptyList())
            if (questions.isEmpty()) {
                _state.value = TriviaUiState(phase = TriviaPhase.Unavailable)
            } else {
                _state.value = TriviaUiState(phase = TriviaPhase.Question, questions = questions)
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeftMillis > 0) {
                delay(TriviaRules.TICK_MILLIS)
                _state.update { it.copy(timeLeftMillis = (it.timeLeftMillis - TriviaRules.TICK_MILLIS).coerceAtLeast(0)) }
            }
            lock(null)
        }
    }

    private fun lock(answer: Int?) {
        if (_state.value.phase != TriviaPhase.Question) return
        timerJob?.cancel()
        _state.update { s ->
            val correct = answer != null && answer == s.current?.answerIndex
            val newStreak = if (correct) s.streak + 1 else 0
            val points = if (correct) TriviaRules.points(s.timeLeftMillis, newStreak) else 0
            s.copy(
                phase = TriviaPhase.Revealed,
                lockedAnswer = answer,
                selected = answer,
                score = s.score + points,
                streak = newStreak,
                bestStreak = maxOf(s.bestStreak, newStreak),
                correctCount = s.correctCount + if (correct) 1 else 0,
                lastPoints = points,
            )
        }
        play(if (_state.value.answeredCorrectly) GameSound.Correct else GameSound.Wrong)
        scheduleAdvance(TriviaRules.REVEAL_MILLIS)
    }

    private fun scheduleAdvance(afterMillis: Long) {
        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(afterMillis)
            advance()
        }
    }

    private fun advance() {
        val s = _state.value
        if (s.index + 1 >= s.total) {
            _state.update { it.copy(phase = TriviaPhase.Finished, revealHeld = false) }
            play(GameSound.Complete)
            viewModelScope.launch { settingsRepository.recordTriviaRound(s.score, s.bestStreak) }
        } else {
            _state.update {
                it.copy(
                    phase = TriviaPhase.Question,
                    index = it.index + 1,
                    selected = null,
                    lockedAnswer = null,
                    eliminated = emptySet(),
                    timeLeftMillis = TriviaRules.QUESTION_MILLIS,
                    revealHeld = false,
                )
            }
            startTimer()
        }
    }

    private fun play(sound: GameSound) {
        if (settings.value.gameSoundEnabled) sounds.play(sound)
    }
}
