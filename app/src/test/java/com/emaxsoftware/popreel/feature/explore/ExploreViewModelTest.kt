package com.emaxsoftware.popreel.feature.explore

import androidx.compose.runtime.snapshots.Snapshot
import com.emaxsoftware.popreel.core.data.CatalogSyncState
import com.emaxsoftware.popreel.core.data.DataError
import com.emaxsoftware.popreel.core.data.MovieFeed
import com.emaxsoftware.popreel.testing.FakeCatalogRepository
import com.emaxsoftware.popreel.testing.FakeMovieChoiceRepository
import com.emaxsoftware.popreel.testing.MainDispatcherRule
import com.emaxsoftware.popreel.testing.testMovie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val choices = FakeMovieChoiceRepository()
    private val catalog = FakeCatalogRepository((1..15).map { testMovie("m$it") }, choices.markedIds)

    private fun TestScope.createViewModel(): ExploreViewModel {
        val viewModel = ExploreViewModel(catalog, choices)
        backgroundScope.launch { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun ExploreViewModel.type(text: String) {
        onQueryChange(text)
        Snapshot.sendApplyNotifications() // what the Compose frame clock does in the app
    }

    @Test
    fun `feed shows the first movie as hero and ranks the reel`() = runTest(mainRule.dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("m1", state.featured?.id)
        assertEquals(10, state.spotlight.size)
        assertEquals("#2 Trending", state.spotlight.first().badge?.text)
        assertEquals("#1 Rated", state.acclaimed.items.first().badge?.text)
        assertEquals(null, state.acclaimed.items[3].badge)
    }

    @Test
    fun `selecting a genre and sort re-queries with them`() = runTest(mainRule.dispatcher) {
        val viewModel = createViewModel()
        viewModel.onGenreSelect("Drama")
        viewModel.onSortSelect(CatalogSort.HiddenGems)
        advanceUntilIdle()

        assertEquals(
            FakeCatalogRepository.FeedRequest(MovieFeed.HiddenGems, "Drama", excludeMarked = false),
            catalog.feedRequests.last { it.feed != MovieFeed.TopRated },
        )
        assertEquals("Drama", viewModel.uiState.value.selectedGenre)
    }

    @Test
    fun `search waits for typing to pause`() = runTest(mainRule.dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.type("m")
        advanceTimeBy(100)
        viewModel.type("m1")
        advanceTimeBy(100)
        viewModel.type("m12")
        advanceUntilIdle()

        assertEquals(listOf("m12"), catalog.searchRequests)
        assertEquals(listOf("m12"), viewModel.uiState.value.searchResults?.items?.map { it.id })

        viewModel.type("")
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.searchResults)
    }

    @Test
    fun `first launch offline asks for initial setup and retries the sync`() = runTest(mainRule.dispatcher) {
        catalog.movies.value = emptyList()
        catalog.syncState.value = CatalogSyncState.Failed(DataError.Network(IOException()))
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.catalog.needsInitialSetup)

        viewModel.onRetrySync()
        advanceUntilIdle()
        assertEquals(1, catalog.syncCalls)
    }
}
