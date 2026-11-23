package com.emaxsoftware.popreel.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogDto(
    val movies: List<MovieDto>,
    val trivia: List<TriviaDto> = emptyList(),
)
