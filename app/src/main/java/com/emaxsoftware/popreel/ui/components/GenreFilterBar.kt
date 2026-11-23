package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emaxsoftware.popreel.core.model.Genre
import com.emaxsoftware.popreel.core.model.formatCompactCount
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import kotlinx.coroutines.launch

/** One chip in [GenreFilterBar]: [key] is what [onSelect] receives, [label] what is shown. */
private data class BarChip(val key: String, val label: String)

/**
 * Single-select filter row: the [leading] options (e.g. "All" or feed modes), the [maxGenres]
 * best-stocked [genres], then "More genres", which opens a sheet listing every genre with its
 * movie count. A genre picked from the sheet is pinned next to [leading] so the active filter
 * is always visible.
 *
 * [selected] is a leading option or a [Genre.name]; [onSelect] receives the same.
 */
@Composable
fun GenreFilterBar(
    leading: List<String>,
    genres: List<Genre>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxGenres: Int = 6,
    contentPadding: PaddingValues = PaddingValues(horizontal = PopReelTheme.spacing.gutter),
    shape: Shape = MaterialTheme.shapes.extraSmall,
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }

    val top = genres.take(maxGenres)
    val pinned = genres.drop(maxGenres).firstOrNull { it.name == selected }
    val chips = leading.map { BarChip(it, it) } +
        listOfNotNull(pinned).plus(top).map { BarChip(it.name, it.label) }
    val hasMore = genres.size > maxGenres

    // Bring the active chip into view, e.g. a genre just picked from the sheet and pinned up front.
    val rowState = rememberLazyListState()
    val selectedIndex = chips.indexOfFirst { it.key == selected }
    LaunchedEffect(selected, selectedIndex) {
        if (selectedIndex < 0) return@LaunchedEffect
        val visible = rowState.layoutInfo.visibleItemsInfo
        val fullyVisible = visible.any { item ->
            item.index == selectedIndex &&
                item.offset >= rowState.layoutInfo.viewportStartOffset &&
                item.offset + item.size <= rowState.layoutInfo.viewportEndOffset
        }
        if (!fullyVisible) rowState.animateScrollToItem(maxOf(0, selectedIndex - 1))
    }

    LazyRow(
        state = rowState,
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(PopReelTheme.spacing.sm),
    ) {
        items(chips, key = { it.key }) { chip ->
            PopReelFilterChip(
                label = chip.label,
                selected = chip.key == selected,
                onClick = { onSelect(chip.key) },
                shape = shape,
            )
        }
        if (hasMore) {
            item(key = MORE_KEY) {
                PopReelFilterChip(
                    label = "More genres",
                    selected = false,
                    onClick = { showSheet = true },
                    shape = shape,
                    leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
                )
            }
        }
    }

    if (showSheet) {
        GenreSheet(
            genres = genres,
            selected = selected,
            shape = shape,
            onSelect = { name ->
                onSelect(name)
                showSheet = false
            },
            onDismiss = { showSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GenreSheet(
    genres: List<Genre>,
    selected: String,
    shape: Shape,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            Text("All genres", style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Text(
                "${genres.size} genres, most films first.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PopReelTheme.spacing.sm),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                genres.forEach { genre ->
                    PopReelFilterChip(
                        label = genre.label,
                        trailingText = formatCompactCount(genre.movieCount),
                        selected = genre.name == selected,
                        // Let the sheet animate away before the screen re-filters underneath it.
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onSelect(genre.name) }
                        },
                        shape = shape,
                    )
                }
            }
        }
    }
}

private const val MORE_KEY = "\u0000more"

@Preview(showBackground = true, backgroundColor = 0xFF10131C)
@Composable
private fun GenreFilterBarPreview() {
    PopReelTheme(darkTheme = true) {
        GenreFilterBar(
            leading = listOf("All"),
            genres = listOf(
                Genre("Drama", 3749), Genre("Comedy", 3069), Genre("Action", 2679), Genre("Thriller", 2484),
                Genre("Adventure", 1825), Genre("Horror", 1477), Genre("Science Fiction", 1274), Genre("Western", 145),
            ),
            selected = "Western",
            onSelect = {},
        )
    }
}
