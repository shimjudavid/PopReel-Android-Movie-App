package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Spinner on a surface; the caller sizes it. */
@Composable
fun LoadingBlock(modifier: Modifier = Modifier, message: String? = null) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.small)
            .padding(24.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        CircularProgressIndicator(color = colors.primaryContainer, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
        if (message != null) {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ErrorBlock(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    Column(
        modifier = modifier
            .background(colors.surfaceContainerLow, shape)
            .border(1.dp, colors.surfaceContainerHighest, shape)
            .padding(20.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = colors.outline, modifier = Modifier.size(28.dp))
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, textAlign = TextAlign.Center)
        }
        Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onRetry, shape = MaterialTheme.shapes.extraSmall) {
            Text("RETRY", style = MaterialTheme.typography.labelLarge, color = colors.primaryContainer)
        }
    }
}

@Composable
fun EmptyBlock(message: String, modifier: Modifier = Modifier) {
    Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(vertical = 32.dp),
    )
}
