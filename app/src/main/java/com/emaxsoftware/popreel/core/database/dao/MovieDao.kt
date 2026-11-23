package com.emaxsoftware.popreel.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.emaxsoftware.popreel.core.database.entity.GenreEntity
import com.emaxsoftware.popreel.core.database.entity.MovieEntity
import com.emaxsoftware.popreel.core.database.entity.TriviaEntity
import kotlinx.coroutines.flow.Flow

private const val GENRE_FILTER = "(:genre IS NULL OR genres LIKE '%|' || :genre || '|%')"
private const val FEED_FILTER =
    "$GENRE_FILTER AND (:excludeMarked = 0 OR id NOT IN (SELECT movieId FROM movie_choices))"

@Dao
abstract class MovieDao {

    @Query("SELECT COUNT(*) FROM movies")
    abstract fun observeCount(): Flow<Int>

    @Query("SELECT name FROM genres ORDER BY name")
    abstract fun observeGenres(): Flow<List<String>>

    @Query("SELECT * FROM movies WHERE id = :id")
    abstract fun observeMovie(id: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id IN (:ids)")
    abstract fun observeMovies(ids: List<String>): Flow<List<MovieEntity>>

    /** A well-known movie with a poster, for the poster puzzle. */
    @Query("SELECT * FROM movies WHERE posterUrl != '' AND voteCount >= 1000 AND id != :excludeId ORDER BY RANDOM() LIMIT 1")
    abstract suspend fun randomWellKnownWithPoster(excludeId: String): MovieEntity?

    @Query("SELECT * FROM movies WHERE $FEED_FILTER ORDER BY popularity DESC, id LIMIT :limit")
    abstract fun observeTrending(genre: String?, excludeMarked: Boolean, limit: Int): Flow<List<MovieEntity>>

    /** Films with 1k+ votes rank first; 100–999-vote films only fill niche genres that run short. */
    @Query(
        "SELECT * FROM movies WHERE $FEED_FILTER AND rating > 0 AND voteCount >= 100 " +
            "ORDER BY (voteCount >= 1000) DESC, rating DESC, voteCount DESC, id LIMIT :limit",
    )
    abstract fun observeTopRated(genre: String?, excludeMarked: Boolean, limit: Int): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE $FEED_FILTER ORDER BY year DESC, popularity DESC, id LIMIT :limit")
    abstract fun observeNewest(genre: String?, excludeMarked: Boolean, limit: Int): Flow<List<MovieEntity>>

    @Query(
        "SELECT * FROM movies WHERE $FEED_FILTER AND rating >= 7 AND voteCount BETWEEN 100 AND 999 " +
            "ORDER BY rating DESC, voteCount DESC, id LIMIT :limit",
    )
    abstract fun observeHiddenGems(genre: String?, excludeMarked: Boolean, limit: Int): Flow<List<MovieEntity>>

    /** [pattern] is a LIKE pattern whose `%`/`_`/`\` were escaped with `\`. */
    @Query(
        "SELECT * FROM movies WHERE (title LIKE :pattern ESCAPE '\\' OR director LIKE :pattern ESCAPE '\\') " +
            "AND $GENRE_FILTER ORDER BY popularity DESC, id LIMIT :limit",
    )
    abstract fun observeSearch(pattern: String, genre: String?, limit: Int): Flow<List<MovieEntity>>

    /** Replaces the catalog atomically, so readers never observe a half-written sync. */
    @Transaction
    open suspend fun replaceCatalog(
        movies: List<MovieEntity>,
        genres: List<GenreEntity>,
        trivia: List<TriviaEntity> = emptyList(),
    ) {
        deleteAllMovies()
        insertMovies(movies)
        deleteAllGenres()
        insertGenres(genres)
        deleteAllTrivia()
        insertTrivia(trivia)
    }

    @Query("DELETE FROM trivia")
    protected abstract suspend fun deleteAllTrivia()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertTrivia(trivia: List<TriviaEntity>)

    @Query("DELETE FROM movies")
    protected abstract suspend fun deleteAllMovies()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM genres")
    protected abstract suspend fun deleteAllGenres()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertGenres(genres: List<GenreEntity>)
}
