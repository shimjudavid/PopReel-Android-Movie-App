package com.emaxsoftware.popreel.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movies",
    indices = [Index("popularity"), Index("rating"), Index("year")],
)
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val year: Int,
    val synopsis: String,
    /** Pipe-delimited with leading/trailing pipes ("|Drama|Crime|") so `LIKE '%|Drama|%'` matches whole names. */
    val genres: String,
    val director: String?,
    /** Pipe-delimited, same format as [genres]. */
    val cast: String,
    val runtimeMinutes: Int?,
    /** 0 when unrated. */
    val rating: Double,
    val posterUrl: String,
    val sourceUrl: String?,
    val voteCount: Int,
    val popularity: Double,
)
