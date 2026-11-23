package com.emaxsoftware.popreel.core.data

import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.TasteFactor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {

    private fun movie(id: String, genres: String, director: String? = null, cast: List<String> = emptyList(), rating: Double = 7.0) =
        Movie(id = id, title = id.uppercase(), year = 2020, rating = rating, genres = genres, posterUrl = "", director = director, cast = cast)

    @Test
    fun `no likes means no recommendations`() {
        assertTrue(RecommendationEngine.recommend(emptyList(), emptyList(), listOf(movie("x", "Drama")), 10).isEmpty())
    }

    @Test
    fun `shared director outranks shared genre and is explained`() {
        val liked = listOf(movie("arrival", "Science Fiction", director = "Denis Villeneuve"))
        val sameDirector = movie("sicario", "Thriller", director = "Denis Villeneuve")
        val sameGenre = movie("alien", "Science Fiction", director = "Ridley Scott")

        val recs = RecommendationEngine.recommend(liked, emptyList(), listOf(sameGenre, sameDirector), 10)

        assertEquals(listOf("sicario", "alien"), recs.map { it.movie.id })
        assertTrue(recs.first().explanation.contains("Denis Villeneuve"))
        assertEquals("Because you liked ARRIVAL", recs.first().headline)
        assertTrue(TasteFactor(TasteFactor.Type.Director, "Denis Villeneuve") in recs.first().factors)
        assertEquals(99, recs.first().matchPercent)
    }

    @Test
    fun `disliked signals push candidates down or out`() {
        val liked = listOf(movie("a", "Comedy"))
        val disliked = listOf(movie("b", "Horror", director = "Bad Director"), movie("c", "Horror"))
        val comedyHorror = movie("ch", "Comedy • Horror", director = "Bad Director")
        val comedy = movie("co", "Comedy")

        val recs = RecommendationEngine.recommend(liked, disliked, listOf(comedyHorror, comedy), 10)

        assertEquals(listOf("co"), recs.map { it.movie.id })
    }

    @Test
    fun `unrelated candidates are never suggested`() {
        val recs = RecommendationEngine.recommend(listOf(movie("a", "Western")), emptyList(), listOf(movie("b", "Romance")), 10)
        assertTrue(recs.isEmpty())
    }
}
