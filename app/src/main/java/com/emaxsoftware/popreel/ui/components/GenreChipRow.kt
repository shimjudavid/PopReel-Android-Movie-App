package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.emaxsoftware.popreel.ui.theme.PopReelTheme

/** Horizontally scrolling single-select genre filter. Bleeds to screen edges. */
@Composable
fun GenreChipRow(
    genres: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = PopReelTheme.spacing.gutter),
    shape: Shape = MaterialTheme.shapes.extraSmall,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(PopReelTheme.spacing.sm),
    ) {
        items(genres, key = { it }) { genre ->
            PopReelFilterChip(
                label = genre,
                selected = genre == selected,
                onClick = { onSelect(genre) },
                shape = shape,
            )
        }
    }
}

/** The app's filter chip styling, shared by chip rows and the genre sheet. */
@Composable
fun PopReelFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingText: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, style = MaterialTheme.typography.labelMedium)
                if (trailingText != null) {
                    Text(
                        trailingText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) colors.onPrimaryContainer.copy(alpha = 0.75f) else colors.outline,
                    )
                }
            }
        },
        leadingIcon = leadingIcon,
        shape = shape,
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surfaceContainer,
            labelColor = colors.onSurfaceVariant,
            iconColor = colors.onSurfaceVariant,
            selectedContainerColor = colors.primaryContainer,
            selectedLabelColor = colors.onPrimaryContainer,
            selectedLeadingIconColor = colors.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.surfaceContainerHighest,
            selectedBorderColor = colors.primaryContainer,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
        ),
    )
}
