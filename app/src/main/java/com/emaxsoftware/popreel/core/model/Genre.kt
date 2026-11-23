package com.emaxsoftware.popreel.core.model

/** A catalog genre offered as a filter. [name] is what queries match on. */
data class Genre(val name: String, val movieCount: Int) {
    /** Shorter chip text for long names. */
    val label: String get() = SHORT_LABELS[name] ?: name

    private companion object {
        val SHORT_LABELS = mapOf("Science Fiction" to "Sci-Fi")
    }
}
