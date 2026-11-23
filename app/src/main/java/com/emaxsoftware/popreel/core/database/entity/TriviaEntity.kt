package com.emaxsoftware.popreel.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trivia")
data class TriviaEntity(
    @PrimaryKey val id: String,
    val movieId: String,
    val kind: String,
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
)

/** Trivia joined with its movie's title and poster for display. */
data class TriviaWithMovie(
    val id: String,
    val movieId: String,
    val kind: String,
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val movieTitle: String?,
    val posterUrl: String?,
)
