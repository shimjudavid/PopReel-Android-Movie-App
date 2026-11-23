package com.emaxsoftware.popreel.core.data

import com.emaxsoftware.popreel.core.database.entity.MovieEntity
import com.emaxsoftware.popreel.core.database.entity.TriviaEntity
import com.emaxsoftware.popreel.core.database.entity.TriviaWithMovie
import com.emaxsoftware.popreel.core.model.TriviaQuestion
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieTag
import com.emaxsoftware.popreel.core.model.TagStyle
import com.emaxsoftware.popreel.core.model.formatCompactCount
import com.emaxsoftware.popreel.core.model.formatRuntime
import com.emaxsoftware.popreel.core.network.dto.MovieDto
import com.emaxsoftware.popreel.core.network.dto.TriviaDto

internal fun MovieDto.toEntity() = MovieEntity(
    id = id,
    title = title,
    year = year,
    synopsis = synopsis.trim(),
    genres = genres.toDelimited(),
    director = director?.trim()?.takeIf { it.isNotEmpty() },
    cast = cast.toDelimited(),
    runtimeMinutes = runtime?.takeIf { it > 0 },
    rating = rating,
    posterUrl = posterUrl,
    sourceUrl = sourceUrl?.takeIf { it.isNotBlank() },
    voteCount = voteCount,
    popularity = popularity,
)

internal fun MovieEntity.toMovie(): Movie {
    val genreNames = genreNames()
    return Movie(
        id = id,
        title = title,
        year = year,
        rating = rating.takeIf { it > 0.0 },
        genres = genreNames.joinToString(" • "),
        posterUrl = posterUrl.tmdbSized("w342"),
        largePosterUrl = posterUrl.tmdbSized("w780").takeIf { it.isNotEmpty() },
        director = director,
        cast = cast.fromDelimited(),
        runtime = runtimeMinutes?.let(::formatRuntime),
        synopsis = synopsis.takeIf { it.isNotEmpty() },
        voteCount = voteCount.takeIf { it > 0 },
        sourceUrl = sourceUrl,
        caption = genreNames.firstOrNull(),
        captionTag = voteCount.takeIf { it > 0 }
            ?.let { MovieTag("${formatCompactCount(it)} votes", TagStyle.Neutral) },
    )
}

internal fun MovieEntity.genreNames(): List<String> = genres.fromDelimited()

/** The catalog links TMDB posters at original size (~2–3 MB); request a sized rendition instead. */
internal fun String.tmdbSized(size: String): String =
    if (contains("image.tmdb.org/t/p/original/")) replace("/t/p/original/", "/t/p/$size/") else this

private fun List<String>.toDelimited(): String {
    val names = map { it.trim().replace("|", "/") }.filter { it.isNotEmpty() }
    return if (names.isEmpty()) "" else names.joinToString(separator = "|", prefix = "|", postfix = "|")
}

private fun String.fromDelimited(): List<String> = split('|').filter { it.isNotEmpty() }

internal fun TriviaDto.toEntity() = TriviaEntity(id, movieId, kind, question.trim(), options, answerIndex)

internal fun TriviaWithMovie.toQuestion() = TriviaQuestion(
    id = id,
    movieId = movieId,
    kind = kind,
    question = question,
    options = options,
    answerIndex = answerIndex,
    movieTitle = movieTitle,
    posterUrl = posterUrl?.tmdbSized("w780")?.takeIf { it.isNotEmpty() },
)
