package com.emaxsoftware.popreel.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TriviaDto(
    val id: String,
    val movieId: String,
    val kind: String = "",
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
)
