package com.emaxsoftware.popreel.testing

import com.emaxsoftware.popreel.core.data.DataError
import com.emaxsoftware.popreel.core.data.MovieChoiceRepository
import com.emaxsoftware.popreel.core.model.MovieChoice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException

class FakeMovieChoiceRepository : MovieChoiceRepository {
    override val choices = MutableStateFlow<Map<String, MovieChoice>>(emptyMap())
    override val history = choices.map { it.toList().asReversed() }
    val markedIds = choices.map { it.keys }
    var failWrites = false

    override suspend fun setChoice(movieId: String, choice: MovieChoice?): Result<Unit> {
        if (failWrites) return Result.failure(DataError.Local(IOException("disk full")))
        choices.update { if (choice == null) it - movieId else it + (movieId to choice) }
        return Result.success(Unit)
    }
}
