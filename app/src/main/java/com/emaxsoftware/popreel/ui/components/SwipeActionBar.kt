package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

/** Dislike · Unseen · Like — the button equivalents of the three swipe gestures. */
@Composable
fun SwipeActionBar(
    onDislike: () -> Unit,
    onUnseen: () -> Unit,
    onLike: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledIconButton(
            onClick = onDislike,
            enabled = enabled,
            shape = shape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.surfaceContainerHigh,
                contentColor = colors.onSurface,
            ),
            modifier = Modifier.size(56.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Dislike", modifier = Modifier.size(28.dp))
        }

        OutlinedButton(
            onClick = onUnseen,
            enabled = enabled,
            shape = shape,
            border = BorderStroke(1.dp, colors.surfaceContainerHighest),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .height(48.dp)
                .weight(1f, fill = false),
        ) {
            Icon(Icons.Outlined.VisibilityOff, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("NOT SEEN", style = MaterialTheme.typography.labelLarge, color = colors.onSurface, maxLines = 1)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }

        FilledIconButton(
            onClick = onLike,
            enabled = enabled,
            shape = shape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
            ),
            modifier = Modifier
                .shadow(16.dp, shape, ambientColor = colors.primaryContainer, spotColor = colors.primaryContainer)
                .size(56.dp),
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = "Like", modifier = Modifier.size(30.dp))
        }
    }
}
