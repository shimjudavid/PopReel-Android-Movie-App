package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice

enum class PosterCardStyle {
    /** Horizontal reel card: badge pinned to the poster's bottom edge. */
    Compact,

    /** Grid card: badge under the rating, genre/feature captions along the bottom. */
    Detailed,
}

/** 2:3 poster with rating, badges and the user's choice, plus title and credit below. */
@Composable
fun MoviePosterCard(
    movie: Movie,
    choice: MovieChoice?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PosterCardStyle = PosterCardStyle.Detailed,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.extraSmall

    Column(modifier.clickable(onClick = onClick, onClickLabel = "View ${movie.title} details")) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(shape)
                .background(colors.surfaceContainer)
                .border(1.dp, colors.surfaceContainerHighest, shape),
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = "${movie.title} poster",
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(colors.surfaceContainer),
                error = ColorPainter(colors.surfaceContainer),
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to colors.surfaceContainerLowest.copy(alpha = 0.9f),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                movie.rating?.let { RatingBadge(it) }
                if (style == PosterCardStyle.Detailed) movie.badge?.let { TagBadge(it) }
            }

            choice?.let {
                ChoiceBadge(
                    it,
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }

            when (style) {
                PosterCardStyle.Compact -> movie.badge?.let {
                    TagBadge(
                        it,
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                    )
                }

                PosterCardStyle.Detailed -> PosterCaptions(
                    movie,
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(8.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            movie.title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            movie.creditLine(short = style == PosterCardStyle.Compact),
            style = MaterialTheme.typography.bodySmall,
            color = colors.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PosterCaptions(movie: Movie, modifier: Modifier = Modifier) {
    if (movie.caption == null && movie.captionTag == null) return
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            movie.caption.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        movie.captionTag?.let { tag ->
            Text(tag.text, style = MaterialTheme.typography.labelSmall, color = tag.style.contentColor(), maxLines = 1)
        }
    }
}
