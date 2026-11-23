package com.emaxsoftware.popreel.core.model

data class TriviaQuestion(
    val id: String,
    val movieId: String,
    val kind: String,
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val movieTitle: String? = null,
    val posterUrl: String? = null,
) {
    val category: String
        get() = when (kind) {
            "cast", "cast_pair" -> "Cast"
            "director" -> "Director"
            "genre" -> "Genre"
            "plot" -> "Plot"
            "year" -> "Release year"
            else -> kind.replaceFirstChar { it.uppercase() }
        }
}
