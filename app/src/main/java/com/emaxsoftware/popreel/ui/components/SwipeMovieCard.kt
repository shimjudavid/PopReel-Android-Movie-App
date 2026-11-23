package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.formatCompactCount
import java.util.Locale

/** Discovery card: artwork with score badges, then title, credits and synopsis. The caller sizes it. */
@Composable
fun SwipeMovieCard(
    movie: Movie,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val container = colors.surfaceContainerLow

    Card(
        onClick = onOpenDetails,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, colors.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            AsyncImage(
                model = movie.heroImageUrl,
                contentDescription = null,
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
                            0f to colors.surfaceContainerLowest.copy(alpha = 0.4f),
                            0.55f to container.copy(alpha = 0.55f),
                            1f to container,
                        ),
                    ),
            )
            CardScores(
                movie,
                Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(colors.onSurface.copy(alpha = 0.2f), CircleShape),
            )
        }
        CardDetails(movie, onOpenDetails)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardScores(movie: Movie, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        movie.rating?.let {
            ScoreBadge(icon = Icons.Filled.Star, value = String.format(Locale.US, "%.1f", it), source = "/ 10")
        }
        movie.voteCount?.let {
            ScoreBadge(icon = Icons.Filled.Groups, value = formatCompactCount(it), source = "votes")
        }
    }
}

@Composable
private fun CardDetails(movie: Movie, onOpenDetails: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Year sits outside the title so it stays visible when long titles ellipsize.
            Text(
                movie.title,
                style = typography.headlineLarge,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (movie.year > 0) {
                Text("  (${movie.year})", style = typography.titleMedium, color = colors.onSurfaceVariant, maxLines = 1)
            }
        }

        Text(
            text = buildAnnotatedString {
                val lead = movie.director ?: movie.cast.take(2).joinToString().ifEmpty { null }
                lead?.let { withStyle(SpanStyle(color = colors.onSurface, fontWeight = FontWeight.Bold)) { append(it) } }
                listOfNotNull(movie.runtime, movie.genres.ifBlank { null }).forEachIndexed { i, part ->
                    if (lead != null || i > 0) append("  •  ")
                    append(part)
                }
            },
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        movie.synopsis?.let {
            Text(
                it,
                style = typography.bodyLarge,
                color = colors.onSurface.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        HorizontalDivider(color = colors.surfaceContainerHighest)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onOpenDetails, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Details", style = typography.labelMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}
