package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Uppercase section title with an accent icon, optional subtitle and
 * trailing action slot (e.g. "See all", sort chip, layout toggle).
 * The icon can sit before or after the title.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    leadingIcon: Boolean = false,
    subtitle: String? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val iconContent: @Composable () -> Unit = {
                    if (icon != null) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = colors.primaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                if (leadingIcon) iconContent()
                Text(
                    text = title.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                if (!leadingIcon) iconContent()
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.outline,
                )
            }
        }
        if (action != null) Row(verticalAlignment = Alignment.CenterVertically, content = action)
    }
}
