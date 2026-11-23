package com.emaxsoftware.popreel.testing

import com.emaxsoftware.popreel.core.model.Movie

fun testMovie(id: String, title: String = id.replaceFirstChar { it.uppercase() }, year: Int = 2020) = Movie(
    id = id,
    title = title,
    year = year,
    rating = 7.5,
    genres = "Drama",
    posterUrl = "",
)
