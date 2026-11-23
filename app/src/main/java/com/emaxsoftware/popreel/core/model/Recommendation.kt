package com.emaxsoftware.popreel.core.model

/** A Genie suggestion with the taste signals that produced it. */
data class Recommendation(
    val movie: Movie,
    /** 0–100, relative to the strongest suggestion in the current set. */
    val matchPercent: Int,
    /** Short heading, e.g. "Because you liked Arrival & Sicario". */
    val headline: String,
    /** Plain-language explanation of the matching genres, director or actors. */
    val explanation: String,
    val factors: Set<TasteFactor>,
)

/** One preference signal (a genre, director or actor) derived from likes and dislikes. */
data class TasteFactor(val type: Type, val name: String) {
    enum class Type { Genre, Director, Actor }
}
