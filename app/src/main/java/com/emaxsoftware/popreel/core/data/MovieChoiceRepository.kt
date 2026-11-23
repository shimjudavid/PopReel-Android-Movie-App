package com.emaxsoftware.popreel.core.data

import com.emaxsoftware.popreel.core.model.MovieChoice
import kotlinx.coroutines.flow.Flow

interface MovieChoiceRepository {
    /** movieId → choice, persisted across sessions. */
    val choices: Flow<Map<String, MovieChoice>>

    /** Most recent first. */
    val history: Flow<List<Pair<String, MovieChoice>>>

    /** Records [choice] for the movie, or clears it when [choice] is null. */
    suspend fun setChoice(movieId: String, choice: MovieChoice?): Result<Unit>
}
