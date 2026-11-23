package com.emaxsoftware.popreel.core.data

import com.emaxsoftware.popreel.core.network.dto.MovieDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMappersTest {

    @Test
    fun `full record round-trips from dto to display model`() {
        val entity = MovieDto(
            id = "wiki-92605",
            title = "12 Angry Men",
            year = 1957,
            synopsis = " A jury deliberates. ",
            genres = listOf("Drama", "Crime"),
            director = "Sidney Lumet",
            cast = listOf("Lee J. Cobb", "Henry Fonda"),
            runtime = 96,
            rating = 8.5,
            posterUrl = "https://image.tmdb.org/t/p/original/x.jpg",
            sourceUrl = "https://en.wikipedia.org/wiki/12_Angry_Men",
            voteCount = 6227,
        ).toEntity()

        assertEquals("|Drama|Crime|", entity.genres)
        val movie = entity.toMovie()
        assertEquals("Drama • Crime", movie.genres)
        assertEquals(listOf("Lee J. Cobb", "Henry Fonda"), movie.cast)
        assertEquals("https://image.tmdb.org/t/p/w342/x.jpg", movie.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/x.jpg", movie.heroImageUrl)
        assertEquals("1h 36m", movie.runtime)
        assertEquals("A jury deliberates.", movie.synopsis)
        assertEquals("6.2k votes", movie.captionTag?.text)
        assertEquals("https://en.wikipedia.org/wiki/12_Angry_Men", movie.sourceUrl)
    }

    @Test
    fun `sparse record hides unknown values`() {
        val movie = MovieDto(id = "k", title = "No Exit", year = 2022, director = "  ", rating = 0.0).toEntity().toMovie()

        assertNull(movie.rating)
        assertNull(movie.director)
        assertNull(movie.runtime)
        assertNull(movie.synopsis)
        assertNull(movie.voteCount)
        assertNull(movie.captionTag)
        assertNull(movie.largePosterUrl)
        assertEquals("", movie.genres)
    }

    @Test
    fun `non-tmdb posters are left alone`() {
        val wiki = "https://upload.wikimedia.org/wikipedia/commons/b/b3/CasablancaPoster-Gold.jpg"
        assertEquals(wiki, wiki.tmdbSized("w342"))
    }

    @Test
    fun `like pattern escapes wildcards`() {
        assertEquals("%100\\%%", likePattern(" 100% "))
        assertEquals("%a\\_b%", likePattern("a_b"))
        assertEquals("%c:\\\\d%", likePattern("c:\\d"))
    }
}
