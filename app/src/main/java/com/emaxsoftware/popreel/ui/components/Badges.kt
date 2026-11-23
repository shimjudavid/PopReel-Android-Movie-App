package com.emaxsoftware.popreel.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.core.model.MovieTag
import com.emaxsoftware.popreel.core.model.TagStyle
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import java.util.Locale

@Composable
fun TagStyle.contentColor(): Color = when (this) {
    TagStyle.Accent -> MaterialTheme.colorScheme.primaryContainer
    TagStyle.Positive -> PopReelTheme.extendedColors.positive
    TagStyle.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** Accent tags are filled with the brand color; others sit on a scrim to stay readable over artwork. */
@Composable
fun TagBadge(tag: MovieTag, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.extraSmall
    if (tag.style == TagStyle.Accent) {
        Text(
            text = tag.text.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
                .background(colors.primaryContainer, shape)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
    } else {
        Text(
            text = tag.text,
            style = MaterialTheme.typography.labelSmall,
            color = tag.style.contentColor(),
            maxLines = 1,
            modifier = modifier
                .scrimSurface(shape)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun RatingBadge(rating: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .scrimSurface(MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = String.format(Locale.US, "%.1f", rating),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Small marker showing how the user marked a movie. */
@Composable
fun ChoiceBadge(choice: MovieChoice, modifier: Modifier = Modifier) {
    val (icon, tint) = choice.iconAndTint()
    Box(
        modifier = modifier.scrimSurface(MaterialTheme.shapes.extraSmall).padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = choice.label(), tint = tint, modifier = Modifier.size(16.dp))
    }
}

fun MovieChoice.label(): String = when (this) {
    MovieChoice.Liked -> "Liked"
    MovieChoice.Disliked -> "Disliked"
    MovieChoice.Unseen -> "Not seen yet"
}

@Composable
fun MovieChoice.iconAndTint(): Pair<ImageVector, Color> = when (this) {
    MovieChoice.Liked -> Icons.Filled.Favorite to MaterialTheme.colorScheme.primaryContainer
    MovieChoice.Disliked -> Icons.Filled.ThumbDown to MaterialTheme.colorScheme.onSurface
    MovieChoice.Unseen -> Icons.Filled.VisibilityOff to MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun Modifier.scrimSurface(shape: Shape): Modifier {
    val colors = MaterialTheme.colorScheme
    return this
        .background(colors.surfaceContainerLowest.copy(alpha = 0.8f), shape)
        .border(1.dp, colors.surfaceContainerHighest, shape)
}

@Composable
fun StatusDot(modifier: Modifier = Modifier, pulsing: Boolean = false) {
    val alpha = if (pulsing) {
        val transition = rememberInfiniteTransition(label = "statusDot")
        val value by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
            label = "statusDotAlpha",
        )
        value
    } else {
        1f
    }
    Box(
        modifier
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
    )
}
