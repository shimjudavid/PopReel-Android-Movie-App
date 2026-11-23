package com.emaxsoftware.popreel.core.data

import com.emaxsoftware.popreel.core.database.entity.GenreEntity
import com.emaxsoftware.popreel.core.network.dto.MovieDto
import org.junit.Assert.assertEquals
import org.junit.Test

class GenresTest {

    @Test
    fun `aliases fold into one genre without duplicates`() {
        assertEquals(
            listOf("Animation", "History", "Comedy"),
            normalizeGenres(listOf("Animated", " Animation ", "Historical", "Comedy", "")),
        )
    }

    @Test
    fun `stored movies use the normalized names`() {
        val entity = MovieDto(id = "a", title = "a", genres = listOf("Animated", "Family")).toEntity()
        assertEquals("|Animation|Family|", entity.genres)
    }

    @Test
    fun `browsable genres drop thinly stocked ones, most movies first`() {
        val movies = listOf(
            MovieDto(id = "1", title = "1", genres = listOf("Western", "Drama")),
            MovieDto(id = "2", title = "2", genres = listOf("Drama")),
            MovieDto(id = "3", title = "3", genres = listOf("Action", "Drama", "Western")),
        ).map { it.toEntity() }

        assertEquals(
            listOf(GenreEntity("Drama", 3), GenreEntity("Western", 2)),
            browsableGenres(movies, minMovies = 2),
        )
    }
}
