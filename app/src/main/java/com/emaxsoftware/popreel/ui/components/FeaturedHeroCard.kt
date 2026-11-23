package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.core.model.MovieTag
import com.emaxsoftware.popreel.core.model.TagStyle
import java.util.Locale

/** Spotlight card with large artwork and a details call to action. The caller sizes it. */
@Composable
fun FeaturedHeroCard(
    movie: Movie,
    choice: MovieChoice?,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String = "PopReel Spotlight",
) {
    val colors = MaterialTheme.colorScheme
    val scrim = colors.surfaceContainerLowest

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        border = BorderStroke(1.dp, colors.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
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
                            0f to Color.Transparent,
                            0.5f to scrim.copy(alpha = 0.6f),
                            1f to scrim,
                        ),
                    ),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(0f to scrim.copy(alpha = 0.9f), 0.5f to Color.Transparent)),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .background(colors.primaryContainer, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        eyebrow.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onPrimaryContainer,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    movie.badge?.let { TagBadge(it) }
                    choice?.let { ChoiceBadge(it) }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                HeroMetaRow(movie)
                Spacer(Modifier.height(6.dp))
                Text(
                    movie.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val credit = movie.director?.let { "Directed by " to it }
                    ?: movie.cast.take(2).joinToString().ifEmpty { null }?.let { "Starring " to it }
                credit?.let { (label, names) ->
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.outline)) { append(label) }
                            withStyle(SpanStyle(color = colors.onSurface, fontWeight = FontWeight.Medium)) {
                                append(names)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onOpenDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer,
                    ),
                ) {
                    Text("VIEW DETAILS", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.size(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroMetaRow(movie: Movie) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        movie.rating?.let { TagBadge(MovieTag("★ " + String.format(Locale.US, "%.1f", it), TagStyle.Accent)) }
        listOfNotNull(movie.year.takeIf { it > 0 }?.toString(), movie.runtime).forEach { part ->
            Text(part, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Box(Modifier.size(4.dp).background(colors.outline, CircleShape))
        }
        Text(
            movie.genres,
            style = MaterialTheme.typography.labelSmall,
            color = colors.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
