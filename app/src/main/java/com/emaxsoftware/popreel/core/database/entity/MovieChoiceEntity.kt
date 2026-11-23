package com.emaxsoftware.popreel.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.emaxsoftware.popreel.core.model.MovieChoice

/** No foreign key to `movies`: choices must survive a catalog re-sync that replaces every movie row. */
@Entity(tableName = "movie_choices")
data class MovieChoiceEntity(
    @PrimaryKey val movieId: String,
    val choice: MovieChoice,
    val updatedAtMillis: Long,
)
