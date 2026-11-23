package com.emaxsoftware.popreel.feature.swipe

import com.emaxsoftware.popreel.core.data.MovieFeed
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.testing.FakeCatalogRepository
import com.emaxsoftware.popreel.testing.FakeMovieChoiceRepository
import com.emaxsoftware.popreel.testing.MainDispatcherRule
import com.emaxsoftware.popreel.testing.testMovie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SwipeViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val choices = FakeMovieChoiceRepository()
    private val catalog = FakeCatalogRepository((1..5).map { testMovie("m$it") }, choices.markedIds)

    private fun TestScope.createViewModel(): Pair<SwipeViewModel, MutableList<SwipeEffect>> {
        val viewModel = SwipeViewModel(catalog, choices)
        val effects = mutableListOf<SwipeEffect>()
        backgroundScope.launch { viewModel.uiState.collect {} }
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        return viewModel to effects
    }

    @Test
    fun `queue only asks for unmarked movies, per filter`() = runTest(mainRule.dispatcher) {
        val (viewModel, _) = createViewModel()
        viewModel.onFilterSelect(DeckFilter.SciFi)
        advanceUntilIdle()

        assertEquals(
            FakeCatalogRepository.FeedRequest(MovieFeed.Trending, "Science Fiction", excludeMarked = true),
            catalog.feedRequests.last(),
        )
    }

    @Test
    fun `swiping saves the choice and removes the card`() = runTest(mainRule.dispatcher) {
        val (viewModel, _) = createViewModel()
        advanceUntilIdle()

        viewModel.onSwiped(testMovie("m1"), MovieChoice.Liked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(MovieChoice.Liked, choices.choices.value["m1"])
        assertEquals("m2", state.remaining.first().id)
        assertEquals(1, state.ratedCount)
        assertTrue(state.canUndo)
    }

    @Test
    fun `undo clears the choice and brings the card back`() = runTest(mainRule.dispatcher) {
        val (viewModel, effects) = createViewModel()
        advanceUntilIdle()
        viewModel.onSwiped(testMovie("m1"), MovieChoice.Unseen)
        advanceUntilIdle()

        viewModel.onUndo()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(emptyMap<String, MovieChoice>(), choices.choices.value)
        assertEquals("m1", state.remaining.first().id)
        assertFalse(state.canUndo)
        assertEquals(listOf<SwipeEffect>(SwipeEffect.Undone("M1", MovieChoice.Unseen)), effects)
    }

    @Test
    fun `a failed save puts the card back and reports it`() = runTest(mainRule.dispatcher) {
        choices.failWrites = true
        val (viewModel, effects) = createViewModel()
        advanceUntilIdle()

        viewModel.onSwiped(testMovie("m1"), MovieChoice.Disliked)
        advanceUntilIdle()

        assertEquals("m1", viewModel.uiState.value.remaining.first().id)
        assertFalse(viewModel.uiState.value.canUndo)
        assertEquals(listOf<SwipeEffect>(SwipeEffect.ChoiceFailed), effects)
    }
}
