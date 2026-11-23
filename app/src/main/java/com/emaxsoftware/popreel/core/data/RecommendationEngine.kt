package com.emaxsoftware.popreel.core.data

import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.Recommendation
import com.emaxsoftware.popreel.core.model.TasteFactor
import kotlin.math.roundToInt

/**
 * Content-based recommendations: likes add weight to their genres, director and cast,
 * dislikes subtract it, and unmarked candidates are scored by the weights they share.
 * Pure and deterministic, so the reasons shown always match the scoring.
 */
object RecommendationEngine {

    private const val GENRE_LIKE = 1.0
    private const val GENRE_DISLIKE = -0.8
    private const val DIRECTOR_LIKE = 3.0
    private const val DIRECTOR_DISLIKE = -2.0
    private const val ACTOR_LIKE = 1.5
    private const val ACTOR_DISLIKE = -1.0
    private const val MAX_CAST_CONSIDERED = 4

    fun tasteProfile(liked: List<Movie>, disliked: List<Movie>): Map<TasteFactor, Double> {
        val weights = HashMap<TasteFactor, Double>()
        fun add(factor: TasteFactor, weight: Double) = weights.merge(factor, weight, Double::plus)
        liked.forEach { movie -> movie.factors().forEach { add(it, it.weight(like = true)) } }
        disliked.forEach { movie -> movie.factors().forEach { add(it, it.weight(like = false)) } }
        return weights
    }

    fun recommend(
        liked: List<Movie>,
        disliked: List<Movie>,
        candidates: List<Movie>,
        limit: Int,
    ): List<Recommendation> {
        if (liked.isEmpty()) return emptyList()
        val profile = tasteProfile(liked, disliked)

        val scored = candidates.mapNotNull { candidate ->
            val matches = candidate.factors().mapNotNull { f -> profile[f]?.takeIf { it > 0 }?.let { f to it } }
            if (matches.isEmpty()) return@mapNotNull null
            val penalty = candidate.factors().sumOf { f -> (profile[f] ?: 0.0).coerceAtMost(0.0) }
            // Small quality term so equally matched films favour the better-rated one.
            val score = matches.sumOf { it.second } + penalty + (candidate.rating ?: 5.0) / 10.0
            if (score <= 0) null else Triple(candidate, score, matches.sortedByDescending { it.second })
        }.sortedByDescending { it.second }.take(limit)

        val best = scored.firstOrNull()?.second ?: return emptyList()
        return scored.map { (movie, score, matches) ->
            val strongest = matches.map { it.first }
            Recommendation(
                movie = movie,
                matchPercent = (60 + 39 * (score / best)).roundToInt().coerceIn(60, 99),
                headline = headline(strongest, liked),
                explanation = explanation(strongest),
                factors = strongest.toSet(),
            )
        }
    }

    private fun headline(factors: List<TasteFactor>, liked: List<Movie>): String {
        val sources = liked.filter { movie -> movie.factors().any { it in factors.take(3) } }.take(2).map { it.title }
        return if (sources.isEmpty()) "Matches your taste" else "Because you liked ${sources.joinToString(" & ")}"
    }

    private fun explanation(factors: List<TasteFactor>): String {
        val director = factors.firstOrNull { it.type == TasteFactor.Type.Director }
        val actors = factors.filter { it.type == TasteFactor.Type.Actor }.take(2)
        val genres = factors.filter { it.type == TasteFactor.Type.Genre }.take(2)
        return buildList {
            director?.let { add("directed by ${it.name}, whose films you liked") }
            if (actors.isNotEmpty()) add("stars ${actors.joinToString(" and ") { it.name }}")
            if (genres.isNotEmpty()) add("fits your love of ${genres.joinToString(" & ") { it.name }}")
        }.joinToString("; ").replaceFirstChar { it.uppercase() } + "."
    }

    private fun Movie.factors(): List<TasteFactor> = buildList {
        genres.split(" • ").filter { it.isNotBlank() }.forEach { add(TasteFactor(TasteFactor.Type.Genre, it)) }
        director?.let { add(TasteFactor(TasteFactor.Type.Director, it)) }
        cast.take(MAX_CAST_CONSIDERED).forEach { add(TasteFactor(TasteFactor.Type.Actor, it)) }
    }

    private fun TasteFactor.weight(like: Boolean): Double = when (type) {
        TasteFactor.Type.Genre -> if (like) GENRE_LIKE else GENRE_DISLIKE
        TasteFactor.Type.Director -> if (like) DIRECTOR_LIKE else DIRECTOR_DISLIKE
        TasteFactor.Type.Actor -> if (like) ACTOR_LIKE else ACTOR_DISLIKE
    }
}
