package com.emaxsoftware.popreel.core.data

import android.database.sqlite.SQLiteException
import com.emaxsoftware.popreel.core.database.dao.MovieChoiceDao
import com.emaxsoftware.popreel.core.database.entity.MovieChoiceEntity
import com.emaxsoftware.popreel.core.model.MovieChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomMovieChoiceRepository @Inject constructor(
    private val dao: MovieChoiceDao,
) : MovieChoiceRepository {

    override val history: Flow<List<Pair<String, MovieChoice>>> = dao.observeAll()
        .map { rows -> rows.map { it.movieId to it.choice } }
        .catch { e -> throw if (e is SQLiteException) DataError.Local(e) else e }

    override val choices: Flow<Map<String, MovieChoice>> = history.map { it.toMap() }

    override suspend fun setChoice(movieId: String, choice: MovieChoice?): Result<Unit> = try {
        if (choice == null) {
            dao.delete(movieId)
        } else {
            dao.upsert(MovieChoiceEntity(movieId, choice, System.currentTimeMillis()))
        }
        Result.success(Unit)
    } catch (e: SQLiteException) {
        Result.failure(DataError.Local(e))
    }
}
