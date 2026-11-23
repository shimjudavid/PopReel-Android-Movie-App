package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.util.Locale

enum class PopReelDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Explore("Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    Swipe("Swipe", Icons.Filled.ViewCarousel, Icons.Outlined.ViewCarousel),
    Genie("Genie", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    Arcade("Arcade", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
}

@Composable
fun PopReelBottomBar(
    current: PopReelDestination,
    onNavigate: (PopReelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier) {
        HorizontalDivider(thickness = 1.dp, color = colors.surfaceContainerHigh)
        NavigationBar(
            containerColor = colors.surfaceContainerLowest.copy(alpha = 0.95f),
            tonalElevation = 0.dp,
        ) {
            PopReelDestination.entries.forEach { destination ->
                val selected = destination == current
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(destination) },
                    icon = {
                        Icon(
                            if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            destination.label.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onPrimaryContainer,
                        selectedTextColor = colors.primaryContainer,
                        indicatorColor = colors.primaryContainer,
                        unselectedIconColor = colors.outline,
                        unselectedTextColor = colors.outline,
                    ),
                )
            }
        }
    }
}

/** Expanded-width alternative to [PopReelBottomBar]. */
@Composable
fun PopReelNavigationRail(
    current: PopReelDestination,
    onNavigate: (PopReelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    NavigationRail(modifier = modifier, containerColor = colors.surfaceContainerLowest) {
        PopReelDestination.entries.forEach { destination ->
            val selected = destination == current
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(if (selected) destination.selectedIcon else destination.unselectedIcon, contentDescription = null)
                },
                label = { Text(destination.label.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.onPrimaryContainer,
                    selectedTextColor = colors.primaryContainer,
                    indicatorColor = colors.primaryContainer,
                    unselectedIconColor = colors.outline,
                    unselectedTextColor = colors.outline,
                ),
            )
        }
    }
}
