package com.emaxsoftware.popreel.core.data

import android.database.sqlite.SQLiteException
import com.emaxsoftware.popreel.core.common.IoDispatcher
import com.emaxsoftware.popreel.core.database.dao.TriviaDao
import com.emaxsoftware.popreel.core.model.TriviaQuestion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TriviaRepository {
    /** A random round from the local catalog; empty when no trivia has been downloaded yet. */
    suspend fun newRound(size: Int): Result<List<TriviaQuestion>>
}

class RoomTriviaRepository @Inject constructor(
    private val dao: TriviaDao,
    @param:IoDispatcher private val io: CoroutineDispatcher,
) : TriviaRepository {

    override suspend fun newRound(size: Int): Result<List<TriviaQuestion>> = withContext(io) {
        try {
            val questions = dao.randomRound(size * 2)
                .filter { it.options.size == 4 && it.answerIndex in 0..3 }
                .distinctBy { it.movieId }
                .take(size)
                .map { it.toQuestion() }
            Result.success(questions)
        } catch (e: SQLiteException) {
            Result.failure(DataError.Local(e))
        }
    }
}
