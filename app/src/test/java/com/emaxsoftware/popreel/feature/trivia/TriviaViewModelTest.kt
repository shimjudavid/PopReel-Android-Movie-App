package com.emaxsoftware.popreel.feature.trivia

import com.emaxsoftware.popreel.core.audio.GameSound
import com.emaxsoftware.popreel.testing.FakeSettingsRepository
import com.emaxsoftware.popreel.testing.FakeTriviaRepository
import com.emaxsoftware.popreel.testing.MainDispatcherRule
import com.emaxsoftware.popreel.testing.RecordingGameSounds
import com.emaxsoftware.popreel.testing.testQuestion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class TriviaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()
    private val sounds = RecordingGameSounds()

    private fun TestScope.start(questions: Int = 3): TriviaViewModel {
        val viewModel = TriviaViewModel(FakeTriviaRepository((1..questions).map { testQuestion("q$it", answer = 0) }), settings, sounds)
        viewModel.random = Random(42)
        viewModel.onStart()
        runCurrent()
        return viewModel
    }

    @Test
    fun `fast correct answer scores base plus speed bonus`() = runTest(mainRule.dispatcher) {
        val viewModel = start()
        viewModel.onSelect(0)
        viewModel.onLockIn()

        val s = viewModel.state.value
        assertEquals(TriviaPhase.Revealed, s.phase)
        assertEquals(200, s.lastPoints) // 100 base + full 100 speed bonus, no streak yet
        assertEquals(1, s.streak)
        assertEquals(GameSound.Correct, sounds.played.last())
    }

    @Test
    fun `streak adds a bonus and a wrong answer resets it`() = runTest(mainRule.dispatcher) {
        val viewModel = start()
        viewModel.onSelect(0); viewModel.onLockIn(); viewModel.onNext()
        viewModel.onSelect(0); viewModel.onLockIn()
        assertEquals(250, viewModel.state.value.lastPoints) // +50 for a 2-answer streak
        viewModel.onNext()
        viewModel.onSelect(1); viewModel.onLockIn()

        val s = viewModel.state.value
        assertEquals(0, s.streak)
        assertEquals(2, s.bestStreak)
        assertEquals(450, s.score)
    }

    @Test
    fun `timer runs out into a wrong reveal`() = runTest(mainRule.dispatcher) {
        val viewModel = start()
        advanceTimeBy(TriviaRules.QUESTION_MILLIS + TriviaRules.TICK_MILLIS)
        runCurrent()

        val s = viewModel.state.value
        assertEquals(TriviaPhase.Revealed, s.phase)
        assertEquals(null, s.lockedAnswer)
        assertEquals(0, s.lastPoints)
    }

    @Test
    fun `reveal auto-advances unless held`() = runTest(mainRule.dispatcher) {
        val viewModel = start()
        viewModel.onSelect(0); viewModel.onLockIn()
        viewModel.onRevealHold(true)
        advanceTimeBy(TriviaRules.REVEAL_MILLIS * 3)
        assertEquals(0, viewModel.state.value.index)

        viewModel.onRevealHold(false)
        advanceTimeBy(TriviaRules.REVEAL_AFTER_HOLD_MILLIS + 1)
        assertEquals(1, viewModel.state.value.index)
        assertEquals(TriviaPhase.Question, viewModel.state.value.phase)
    }

    @Test
    fun `fifty-fifty removes two wrong answers once per round`() = runTest(mainRule.dispatcher) {
        val viewModel = start()
        viewModel.onFiftyFifty()

        val removed = viewModel.state.value.eliminated
        assertEquals(2, removed.size)
        assertFalse(0 in removed)
        viewModel.onSelect(removed.first())
        assertEquals(null, viewModel.state.value.selected)

        viewModel.onSelect(0); viewModel.onLockIn(); viewModel.onNext()
        viewModel.onFiftyFifty()
        assertTrue(viewModel.state.value.eliminated.isEmpty())
    }

    @Test
    fun `pause freezes the clock`() = runTest(mainRule.dispatcher) {
        val viewModel = start()
        advanceTimeBy(2_050)
        viewModel.onPause()
        val frozen = viewModel.state.value.timeLeftMillis
        advanceTimeBy(10_000)
        assertEquals(frozen, viewModel.state.value.timeLeftMillis)

        viewModel.onResume()
        advanceTimeBy(1_050)
        assertTrue(viewModel.state.value.timeLeftMillis < frozen)
    }

    @Test
    fun `finishing a round records stats`() = runTest(mainRule.dispatcher) {
        val viewModel = start(questions = 1)
        viewModel.onSelect(0); viewModel.onLockIn(); viewModel.onNext()
        runCurrent()

        assertEquals(TriviaPhase.Finished, viewModel.state.value.phase)
        assertEquals(200, settings.arcadeStats.value.triviaBestScore)
        assertEquals(1, settings.arcadeStats.value.triviaRoundsPlayed)
    }

    @Test
    fun `sound setting is respected`() = runTest(mainRule.dispatcher) {
        settings.settings.value = settings.settings.value.copy(gameSoundEnabled = false)
        val viewModel = start()
        viewModel.onSelect(0); viewModel.onLockIn()
        assertTrue(sounds.played.isEmpty())
    }
}
