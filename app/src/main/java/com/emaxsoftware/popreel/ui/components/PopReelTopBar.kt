package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopReelTopBar(
    soundEnabled: Boolean,
    onSoundToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val colors = MaterialTheme.colorScheme
    val barColor = colors.surfaceContainerLowest.copy(alpha = 0.95f)

    Column(modifier) {
        TopAppBar(
            title = { PopReelLogo() },
            actions = {
                IconButton(onClick = onSoundToggle) {
                    Icon(
                        if (soundEnabled) Icons.AutoMirrored.Outlined.VolumeUp else Icons.AutoMirrored.Outlined.VolumeOff,
                        contentDescription = if (soundEnabled) "Turn game sound off" else "Turn game sound on",
                        tint = colors.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = colors.onSurfaceVariant)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = barColor, scrolledContainerColor = barColor),
            scrollBehavior = scrollBehavior,
        )
        HorizontalDivider(thickness = 1.dp, color = colors.surfaceContainerHigh)
    }
}
