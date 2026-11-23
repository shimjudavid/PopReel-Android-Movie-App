package com.emaxsoftware.popreel.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MovieDto(
    val id: String,
    val title: String,
    val year: Int = 0,
    val synopsis: String = "",
    val genres: List<String> = emptyList(),
    val director: String? = null,
    val cast: List<String> = emptyList(),
    /** Minutes. */
    val runtime: Int? = null,
    /** 0 when unrated. */
    val rating: Double = 0.0,
    val posterUrl: String = "",
    val sourceUrl: String? = null,
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
)
