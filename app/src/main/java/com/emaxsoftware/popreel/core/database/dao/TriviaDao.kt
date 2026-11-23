package com.emaxsoftware.popreel.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.emaxsoftware.popreel.core.database.entity.TriviaWithMovie

@Dao
interface TriviaDao {

    @Query("SELECT COUNT(*) FROM trivia")
    suspend fun count(): Int

    /** Only questions whose movie is still in the catalog, in random order. */
    @Query(
        "SELECT t.id, t.movieId, t.kind, t.question, t.options, t.answerIndex, " +
            "m.title AS movieTitle, m.posterUrl AS posterUrl " +
            "FROM trivia t JOIN movies m ON m.id = t.movieId ORDER BY RANDOM() LIMIT :count",
    )
    suspend fun randomRound(count: Int): List<TriviaWithMovie>
}
