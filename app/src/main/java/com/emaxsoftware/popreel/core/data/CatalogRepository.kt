package com.emaxsoftware.popreel.core.data

import com.emaxsoftware.popreel.core.model.Genre
import com.emaxsoftware.popreel.core.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Offline-first movie catalog: screens read the local copy, [sync] refreshes it from the API.
 * Observed flows fail with [DataError] only.
 */
interface CatalogRepository {
    val syncState: StateFlow<CatalogSyncState>
    val movieCount: Flow<Int>
    /** Filterable genres, most movies first. */
    val genres: Flow<List<Genre>>

    /** [excludeMarked] drops movies the user already liked, disliked or marked unseen. */
    fun observeFeed(feed: MovieFeed, genre: String?, limit: Int, excludeMarked: Boolean = false): Flow<List<Movie>>

    /** Title or director contains [query], most popular first. */
    fun observeSearch(query: String, genre: String?, limit: Int): Flow<List<Movie>>

    /** Null until found; completes empty if the movie is not in the local catalog. */
    fun observeMovie(id: String): Flow<Movie?>

    /** Movies for [ids]; order is not guaranteed. */
    fun observeMovies(ids: List<String>): Flow<List<Movie>>

    /** Top-rated movies sharing [movie]'s lead genre, excluding [movie] itself. */
    fun observeSimilar(movie: Movie, limit: Int): Flow<List<Movie>>

    suspend fun randomPuzzleMovie(excludeId: String?): Result<Movie?>

    /** Downloads the catalog and replaces the local copy; a failure keeps the existing copy. */
    suspend fun sync(): Result<Unit>
}
