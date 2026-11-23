package com.emaxsoftware.popreel.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MovieFormattingTest {

    @Test
    fun `compact counts drop trailing zero decimals`() {
        assertEquals("845", formatCompactCount(845))
        assertEquals("6.2k", formatCompactCount(6227))
        assertEquals("1k", formatCompactCount(1000))
        assertEquals("10k", formatCompactCount(10_000))
        assertEquals("1.5M", formatCompactCount(1_500_000))
    }

    @Test
    fun `runtime formatting`() {
        assertEquals("1h 36m", formatRuntime(96))
        assertEquals("45m", formatRuntime(45))
        assertEquals("2h", formatRuntime(120))
    }

    @Test
    fun `credit line falls back to lead genre without a director`() {
        val movie = Movie(id = "a", title = "A", year = 2022, rating = null, genres = "Thriller • Drama", posterUrl = "")
        assertEquals("2022 • Thriller", movie.creditLine())
        assertEquals("2022 • Sidney Lumet", movie.copy(director = "Sidney Lumet").creditLine())
    }
}
