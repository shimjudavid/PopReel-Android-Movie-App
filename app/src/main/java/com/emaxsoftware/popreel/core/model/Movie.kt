package com.emaxsoftware.popreel.core.model

data class Movie(
    val id: String,
    val title: String,
    val year: Int,
    /** Average rating out of 10; null when the film has no ratings. */
    val rating: Double?,
    /** Display genres, e.g. "Sci-Fi • Epic". */
    val genres: String,
    val posterUrl: String,
    /** Higher-resolution art for hero and swipe cards; falls back to [posterUrl]. */
    val largePosterUrl: String? = null,
    val director: String? = null,
    /** Short director credit for compact cards, e.g. "Nolan". */
    val directorShort: String? = director,
    val cast: List<String> = emptyList(),
    val runtime: String? = null,
    val synopsis: String? = null,
    val voteCount: Int? = null,
    val sourceUrl: String? = null,
    val badge: MovieTag? = null,
    /** Bottom-left caption on grid posters, e.g. the lead genre. */
    val caption: String? = null,
    /** Bottom-right caption on grid posters, e.g. "6.2k votes". */
    val captionTag: MovieTag? = null,
) {
    val heroImageUrl: String get() = largePosterUrl ?: posterUrl

    /** "2017 • Denis Villeneuve", falling back to the lead genre when no director is known. */
    fun creditLine(short: Boolean = false): String {
        val credit = (if (short) directorShort else director)
            ?: genres.substringBefore(" • ").ifBlank { null }
        return listOfNotNull(year.takeIf { it > 0 }?.toString(), credit).joinToString(" • ")
    }
}
