package com.emaxsoftware.popreel.core.model

data class MovieTag(val text: String, val style: TagStyle)

enum class TagStyle { Accent, Positive, Neutral }
