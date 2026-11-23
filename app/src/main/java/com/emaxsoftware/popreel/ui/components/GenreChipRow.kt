package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val colors = MaterialTheme.colorScheme
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(PopReelTheme.spacing.sm),
    ) {
        items(genres, key = { it }) { genre ->
            val isSelected = genre == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(genre) },
                label = { Text(genre, style = MaterialTheme.typography.labelMedium) },
                shape = shape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.surfaceContainer,
                    labelColor = colors.onSurfaceVariant,
                    selectedContainerColor = colors.primaryContainer,
                    selectedLabelColor = colors.onPrimaryContainer,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = colors.surfaceContainerHighest,
                    selectedBorderColor = colors.primaryContainer,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp,
                ),
            )
        }
    }
}
