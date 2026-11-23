package com.emaxsoftware.popreel.feature.swipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.ui.components.ChoiceSelector
import com.emaxsoftware.popreel.ui.components.EmptyBlock

/** "Review previous choices": every marked movie, newest first, each changeable or clearable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChoiceReviewSheet(
    items: List<ChoiceReviewItem>,
    onChoiceChange: (Movie, MovieChoice?) -> Unit,
    onOpenMovie: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow,
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("Your choices", style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Text(
                "Change or clear a choice; cleared films return to the deck.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        if (items.isEmpty()) {
            EmptyBlock("You haven't rated any films yet.", Modifier.fillMaxWidth())
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.movie.id }) { item ->
                    ReviewRow(item, onChoiceChange, onOpenMovie)
                    HorizontalDivider(Modifier.padding(top = 12.dp), color = colors.surfaceContainerHighest)
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(
    item: ChoiceReviewItem,
    onChoiceChange: (Movie, MovieChoice?) -> Unit,
    onOpenMovie: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = "Open ${item.movie.title} details") { onOpenMovie(item.movie.id) },
        ) {
            AsyncImage(
                model = item.movie.posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(colors.surfaceContainer),
                error = ColorPainter(colors.surfaceContainer),
                modifier = Modifier
                    .width(40.dp)
                    .height(60.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    item.movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(item.movie.creditLine(), style = MaterialTheme.typography.bodySmall, color = colors.outline)
            }
        }
        ChoiceSelector(
            selected = item.choice,
            onChoiceChange = { onChoiceChange(item.movie, it) },
            compact = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
