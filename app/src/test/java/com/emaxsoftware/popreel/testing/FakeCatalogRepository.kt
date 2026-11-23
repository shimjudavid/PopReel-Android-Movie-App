package com.emaxsoftware.popreel.testing

import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.CatalogSyncState
import com.emaxsoftware.popreel.core.data.MovieFeed
import com.emaxsoftware.popreel.core.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FakeCatalogRepository(
    movies: List<Movie> = emptyList(),
    /** Movie ids the fake treats as marked when `excludeMarked` is requested. */
    private val markedIds: Flow<Set<String>> = MutableStateFlow(emptySet()),
) : CatalogRepository {

    data class FeedRequest(val feed: MovieFeed, val genre: String?, val excludeMarked: Boolean)

    val movies = MutableStateFlow(movies)
    val feedRequests = mutableListOf<FeedRequest>()
    val searchRequests = mutableListOf<String>()
    var syncCalls = 0
    var syncResult: Result<Unit> = Result.success(Unit)

    override val syncState = MutableStateFlow<CatalogSyncState>(CatalogSyncState.Synced)
    override val movieCount: Flow<Int> = this.movies.map { it.size }
    override val genres: Flow<List<String>> = MutableStateFlow(listOf("Drama", "Science Fiction"))

    override fun observeFeed(feed: MovieFeed, genre: String?, limit: Int, excludeMarked: Boolean): Flow<List<Movie>> {
        feedRequests += FeedRequest(feed, genre, excludeMarked)
        return combine(movies, markedIds) { all, marked ->
            all.filter { !excludeMarked || it.id !in marked }.take(limit)
        }
    }

    override fun observeSearch(query: String, genre: String?, limit: Int): Flow<List<Movie>> {
        searchRequests += query
        return movies.map { all -> all.filter { it.title.contains(query, ignoreCase = true) }.take(limit) }
    }

    override fun observeMovie(id: String): Flow<Movie?> = movies.map { all -> all.firstOrNull { it.id == id } }

    override fun observeMovies(ids: List<String>): Flow<List<Movie>> = movies.map { all -> all.filter { it.id in ids } }

    override fun observeSimilar(movie: Movie, limit: Int): Flow<List<Movie>> =
        movies.map { all -> all.filter { it.id != movie.id }.take(limit) }

    override suspend fun randomPuzzleMovie(excludeId: String?): Result<Movie?> =
        Result.success(movies.value.firstOrNull { it.id != excludeId })

    override suspend fun sync(): Result<Unit> {
        syncCalls++
        return syncResult
    }
}
