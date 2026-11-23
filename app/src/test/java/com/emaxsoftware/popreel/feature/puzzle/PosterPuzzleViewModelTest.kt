package com.emaxsoftware.popreel.feature.puzzle

import com.emaxsoftware.popreel.testing.FakeCatalogRepository
import com.emaxsoftware.popreel.testing.FakeSettingsRepository
import com.emaxsoftware.popreel.testing.MainDispatcherRule
import com.emaxsoftware.popreel.testing.RecordingGameSounds
import com.emaxsoftware.popreel.testing.testMovie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class PosterPuzzleViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `shuffles never start nearly solved`() {
        val random = Random(7)
        repeat(200) {
            val tiles = PuzzleRules.shuffledTiles(random)
            assertEquals((0 until 9).toSet(), tiles.toSet())
            assertTrue(tiles.withIndex().count { (i, p) -> i == p } <= 2)
        }
    }

    @Test
    fun `swapping every piece home solves the puzzle and records it`() = runTest(mainRule.dispatcher) {
        val settings = FakeSettingsRepository()
        val viewModel = PosterPuzzleViewModel(FakeCatalogRepository(listOf(testMovie("jaws"))), settings, RecordingGameSounds())
        viewModel.random = Random(3)
        viewModel.onStart()
        runCurrent()

        while (true) {
            val s = viewModel.state.value as PuzzleUiState.Playing
            if (s.solved) break
            val position = s.tiles.indices.first { s.tiles[it] != it }
            val home = s.tiles.indexOf(position)
            viewModel.onTileTap(position)
            viewModel.onTileTap(home)
        }
        runCurrent()

        val solved = viewModel.state.value as PuzzleUiState.Playing
        assertEquals(9, solved.placed)
        assertTrue(solved.moves in 1..8)
        assertEquals(1, settings.arcadeStats.value.puzzlesSolved)
    }
}
