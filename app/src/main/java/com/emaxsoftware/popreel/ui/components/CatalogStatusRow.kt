package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ConnectionStatus(val label: String) { Live("Live"), Syncing("Syncing"), Offline("Offline") }

/** "● 9,919 films in catalog        ⚡ Live" strip. */
@Composable
fun CatalogStatusRow(
    label: String,
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .background(colors.surfaceContainerLow, CircleShape)
                .border(1.dp, colors.surfaceContainerHigh, CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatusDot(Modifier.size(6.dp), pulsing = status == ConnectionStatus.Syncing)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                when (status) {
                    ConnectionStatus.Live -> Icons.Filled.Bolt
                    ConnectionStatus.Syncing -> Icons.Outlined.Sync
                    ConnectionStatus.Offline -> Icons.Outlined.CloudOff
                },
                contentDescription = null,
                tint = if (status == ConnectionStatus.Offline) colors.outline else colors.primaryContainer,
                modifier = Modifier.size(14.dp),
            )
            Text(status.label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}
