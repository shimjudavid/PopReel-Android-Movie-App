package com.emaxsoftware.popreel.core.data

import android.database.sqlite.SQLiteException
import com.emaxsoftware.popreel.core.common.IoDispatcher
import com.emaxsoftware.popreel.core.database.dao.MovieDao
import com.emaxsoftware.popreel.core.database.entity.MovieEntity
import com.emaxsoftware.popreel.core.model.Genre
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.network.PopReelApiService
import com.emaxsoftware.popreel.core.network.serverMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class OfflineFirstCatalogRepository @Inject constructor(
    private val api: PopReelApiService,
    private val movieDao: MovieDao,
    private val json: Json,
    @param:IoDispatcher private val io: CoroutineDispatcher,
) : CatalogRepository {

    private val _syncState = MutableStateFlow<CatalogSyncState>(CatalogSyncState.Idle)
    override val syncState: StateFlow<CatalogSyncState> = _syncState.asStateFlow()

    private val syncMutex = Mutex()

    override val movieCount: Flow<Int> = movieDao.observeCount().mapStorageErrors()

    override val genres: Flow<List<Genre>> = movieDao.observeGenres()
        .map { genres -> genres.map { Genre(it.name, it.movieCount) } }
        .mapStorageErrors()

    override fun observeFeed(feed: MovieFeed, genre: String?, limit: Int, excludeMarked: Boolean): Flow<List<Movie>> {
        val rows = when (feed) {
            MovieFeed.Trending -> movieDao.observeTrending(genre, excludeMarked, limit)
            MovieFeed.TopRated -> movieDao.observeTopRated(genre, excludeMarked, limit)
            MovieFeed.Newest -> movieDao.observeNewest(genre, excludeMarked, limit)
            MovieFeed.HiddenGems -> movieDao.observeHiddenGems(genre, excludeMarked, limit)
        }
        return rows.toMovies()
    }

    override fun observeSearch(query: String, genre: String?, limit: Int): Flow<List<Movie>> =
        movieDao.observeSearch(likePattern(query), genre, limit).toMovies()

    override fun observeMovie(id: String): Flow<Movie?> =
        movieDao.observeMovie(id).map { it?.toMovie() }.mapStorageErrors()

    override fun observeMovies(ids: List<String>): Flow<List<Movie>> =
        if (ids.isEmpty()) flowOf(emptyList()) else movieDao.observeMovies(ids).toMovies()

    override fun observeSimilar(movie: Movie, limit: Int): Flow<List<Movie>> {
        val genre = movie.genres.substringBefore(" • ").ifBlank { null }
        return movieDao.observeTopRated(genre, excludeMarked = false, limit = limit + 1)
            .map { rows -> rows.filter { it.id != movie.id }.take(limit).map(MovieEntity::toMovie) }
            .mapStorageErrors()
    }

    override suspend fun randomPuzzleMovie(excludeId: String?): Result<Movie?> = withContext(io) {
        try {
            Result.success(movieDao.randomWellKnownWithPoster(excludeId.orEmpty())?.toMovie())
        } catch (e: SQLiteException) {
            Result.failure(DataError.Local(e))
        }
    }

    override suspend fun sync(): Result<Unit> = syncMutex.withLock {
        _syncState.value = CatalogSyncState.Syncing
        try {
            val result = withContext(io) { downloadAndStore() }
            _syncState.value = result.fold(
                onSuccess = { CatalogSyncState.Synced },
                onFailure = { CatalogSyncState.Failed(it as DataError) },
            )
            result
        } finally {
            // Cancelled mid-sync: don't leave the app showing a sync that is no longer running.
            if (_syncState.value == CatalogSyncState.Syncing) _syncState.value = CatalogSyncState.Idle
        }
    }

    private suspend fun downloadAndStore(): Result<Unit> = try {
        val catalog = api.catalog()
        val movies = catalog.movies.map { it.toEntity() }
        val genres = browsableGenres(movies)
        val trivia = catalog.trivia.filter { it.options.size == 4 && it.answerIndex in 0..3 }.map { it.toEntity() }
        movieDao.replaceCatalog(movies, genres, trivia)
        Result.success(Unit)
    } catch (e: IOException) {
        Result.failure(DataError.Network(e))
    } catch (e: HttpException) {
        Result.failure(DataError.Server(e.code(), e.serverMessage(json)))
    } catch (e: SerializationException) {
        Result.failure(DataError.MalformedResponse(e))
    } catch (e: SQLiteException) {
        Result.failure(DataError.Local(e))
    }

    private fun Flow<List<MovieEntity>>.toMovies(): Flow<List<Movie>> =
        map { rows -> rows.map(MovieEntity::toMovie) }.mapStorageErrors()

    private fun <T> Flow<T>.mapStorageErrors(): Flow<T> =
        catch { e -> throw if (e is SQLiteException) DataError.Local(e) else e }
}

/** Escapes LIKE wildcards so a search for "100%" matches literally. */
internal fun likePattern(query: String): String {
    val escaped = query.trim()
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
    return "%$escaped%"
}
