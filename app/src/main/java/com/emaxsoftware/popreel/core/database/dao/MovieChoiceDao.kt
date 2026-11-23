package com.emaxsoftware.popreel.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.emaxsoftware.popreel.core.database.entity.MovieChoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieChoiceDao {

    @Query("SELECT * FROM movie_choices ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<MovieChoiceEntity>>

    @Upsert
    suspend fun upsert(choice: MovieChoiceEntity)

    @Query("DELETE FROM movie_choices WHERE movieId = :movieId")
    suspend fun delete(movieId: String)
}
