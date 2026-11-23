package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** "★ 8.2 IMDb"-style score chip over artwork. */
@Composable
fun ScoreBadge(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier,
    source: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = modifier
            .background(colors.surfaceContainerLowest.copy(alpha = 0.9f), shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Text(value, style = MaterialTheme.typography.labelLarge, color = contentColor)
        if (source != null) {
            Text(source, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
}
