package com.emaxsoftware.popreel.feature.genie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import com.emaxsoftware.popreel.core.data.CatalogSyncState
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.core.model.Recommendation
import com.emaxsoftware.popreel.core.model.TasteFactor
import com.emaxsoftware.popreel.ui.components.EmptyBlock
import com.emaxsoftware.popreel.ui.components.ErrorBlock
import com.emaxsoftware.popreel.ui.components.GenreChipRow
import com.emaxsoftware.popreel.ui.components.LoadingBlock
import com.emaxsoftware.popreel.ui.components.scrimSurface
import com.emaxsoftware.popreel.ui.preview.SampleCatalog
import com.emaxsoftware.popreel.ui.state.CHOICE_SAVE_FAILED
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.INITIAL_SETUP_MESSAGE
import com.emaxsoftware.popreel.ui.state.INITIAL_SETUP_TITLE
import com.emaxsoftware.popreel.ui.state.ListState
import com.emaxsoftware.popreel.ui.state.choiceSavedMessage
import com.emaxsoftware.popreel.ui.state.userMessage
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import java.util.Locale

@Composable
fun GenieScreen(
    contentPadding: PaddingValues,
    onOpenMovie: (String) -> Unit,
    onOpenSwipe: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: GenieViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                currentOnShowMessage(
                    when (effect) {
                        is GenieEffect.ChoiceSaved -> choiceSavedMessage(effect.movieTitle, effect.choice)
                        GenieEffect.ChoiceFailed -> CHOICE_SAVE_FAILED
                    },
                )
            }
        }
    }
    GenieContent(
        uiState = uiState,
        contentPadding = contentPadding,
        onOpenMovie = onOpenMovie,
        onOpenSwipe = onOpenSwipe,
        onFactorSelect = viewModel::onFactorSelect,
        onChoice = viewModel::onChoice,
        onRetrySync = viewModel::onRetrySync,
    )
}

@Composable
private fun GenieContent(
    uiState: GenieUiState,
    contentPadding: PaddingValues,
    onOpenMovie: (String) -> Unit,
    onOpenSwipe: () -> Unit,
    onFactorSelect: (TasteFactor?) -> Unit,
    onChoice: (Movie, MovieChoice) -> Unit,
    onRetrySync: () -> Unit,
) {
    val column = Modifier
        .widthIn(max = 720.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp)

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        item { HeaderCard(uiState, column) }

        when {
            uiState.catalog.needsInitialSetup -> item {
                ErrorBlock(INITIAL_SETUP_MESSAGE, onRetrySync, column, title = INITIAL_SETUP_TITLE)
            }

            uiState.catalog.isSettingUp -> item { LoadingBlock(column.height(200.dp), message = "Downloading the movie catalog…") }

            !uiState.hasPreferences -> item { GetStartedCard(onOpenSwipe, column) }

            else -> {
                if (uiState.tasteFactors.isNotEmpty()) {
                    item {
                        val all = "All matches"
                        val labels = listOf(all) + uiState.tasteFactors.map { it.chipLabel() }
                        GenreChipRow(
                            genres = labels,
                            selected = uiState.selectedFactor?.chipLabel() ?: all,
                            onSelect = { label -> onFactorSelect(uiState.tasteFactors.firstOrNull { it.chipLabel() == label }) },
                            shape = CircleShape,
                            modifier = Modifier.widthIn(max = 720.dp),
                        )
                    }
                }
                val recs = uiState.recommendations
                when {
                    recs.isLoading -> item { LoadingBlock(column.height(240.dp), message = "Consulting the Genie…") }
                    recs.error != null -> item { ErrorBlock(recs.error.userMessage(), onRetrySync, column) }
                    uiState.visible.isEmpty() -> item {
                        EmptyBlock(
                            "No new matches right now. Like a few more films in Swipe or Explore to widen the net.",
                            column,
                        )
                    }
                    else -> items(uiState.visible, key = { it.movie.id }) { rec ->
                        RecommendationCard(
                            rec = rec,
                            onOpenMovie = onOpenMovie,
                            onChoice = onChoice,
                            modifier = column.animateItem(),
                        )
                    }
                }
                item { CalibrationCard(uiState, column) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderCard(uiState: GenieUiState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(
                Brush.linearGradient(listOf(colors.surfaceContainerHigh, colors.surfaceContainerLow, colors.surfaceContainerLowest)),
                MaterialTheme.shapes.large,
            )
            .border(1.dp, colors.surfaceContainerHighest, MaterialTheme.shapes.large)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(colors.primaryContainer.copy(alpha = 0.2f), MaterialTheme.shapes.medium)
                    .border(1.dp, colors.primaryContainer.copy(alpha = 0.4f), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = colors.primaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Genie Taste Engine",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                Text("Picks shaped by your likes and dislikes", style = MaterialTheme.typography.bodySmall, color = colors.primary)
            }
        }
        Text(
            "Every suggestion explains which genres, directors or actors it shares with films you liked. " +
                "Films you've already marked — including ones you haven't seen — never appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderPill(
                "${plural(uiState.markedCount, "rating")} · ${plural(uiState.likedCount, "like")} · " +
                    plural(uiState.dislikedCount, "dislike"),
            )
            uiState.tasteFactors.firstOrNull()?.let { HeaderPill("Top signal: ${it.name}", accent = true) }
        }
    }
}

@Composable
private fun HeaderPill(text: String, accent: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = if (accent) colors.primary else colors.onSurface,
        modifier = Modifier
            .background(colors.surfaceContainerLowest, CircleShape)
            .border(1.dp, if (accent) colors.primaryContainer.copy(alpha = 0.4f) else colors.surfaceContainerHighest, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun RecommendationCard(
    rec: Recommendation,
    onOpenMovie: (String) -> Unit,
    onChoice: (Movie, MovieChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val movie = rec.movie
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        border = BorderStroke(1.dp, colors.surfaceContainerHigh),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp),
        ) {
            AsyncImage(
                model = movie.heroImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(colors.surfaceContainer),
                error = ColorPainter(colors.surfaceContainer),
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0f to colors.surfaceContainerLowest.copy(alpha = 0.3f), 1f to colors.surfaceContainerLow)),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .scrimSurface(CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Icon(Icons.Filled.Stars, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("${rec.matchPercent}% MATCH", style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
            Row(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            ) {
                IconButton(onClick = { onChoice(movie, MovieChoice.Disliked) }) {
                    Box(Modifier.size(36.dp).scrimSurface(CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Close, contentDescription = "Dislike ${movie.title}", modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = { onChoice(movie, MovieChoice.Liked) }) {
                    Box(Modifier.size(36.dp).scrimSurface(CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Like ${movie.title}",
                            tint = colors.primaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 12.dp),
            ) {
                val chips = listOfNotNull(
                    movie.year.takeIf { it > 0 }?.toString(),
                    movie.runtime,
                    movie.genres.split(" • ").take(2).joinToString(" / ").ifBlank { null },
                )
                items(chips) { chip ->
                    Text(
                        chip,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface,
                        modifier = Modifier
                            .scrimSurface(CircleShape)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                movie.rating?.let {
                    Text(String.format(Locale.US, "★ %.1f", it), style = MaterialTheme.typography.titleSmall, color = colors.primaryContainer)
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLowest, MaterialTheme.shapes.medium)
                    .padding(14.dp),
            ) {
                Icon(Icons.Filled.Psychology, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(rec.headline.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelSmall, color = colors.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(rec.explanation, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onOpenMovie(movie.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
                ) {
                    Text("View details", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                OutlinedButton(
                    onClick = { onChoice(movie, MovieChoice.Unseen) },
                    modifier = Modifier.height(48.dp),
                    shape = CircleShape,
                ) {
                    Icon(Icons.Outlined.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Not seen", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                }
            }
        }
    }
}

@Composable
private fun GetStartedCard(onOpenSwipe: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
            .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.large)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Teach the Genie your taste", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
        Text(
            "Like a handful of films you love — and dislike a few you don't — in Swipe or from any movie's details. " +
                "The Genie then suggests films that share their genres, directors and actors, and tells you why.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Button(
            onClick = onOpenSwipe,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
        ) { Text("Open the Swipe deck", style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun CalibrationCard(uiState: GenieUiState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
            .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.large)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = colors.primaryContainer)
            Spacer(Modifier.width(8.dp))
            Text("Taste calibration", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface, modifier = Modifier.weight(1f))
            Text("Level ${uiState.level}", style = MaterialTheme.typography.labelMedium, color = colors.primary)
        }
        LinearProgressIndicator(
            progress = { uiState.levelProgress },
            color = colors.primaryContainer,
            trackColor = colors.surfaceContainerLowest,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
        Row {
            Text("${uiState.markedCount} films rated", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("${uiState.filmsToNextLevel} to level ${uiState.level + 1}", style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
        }
    }
}

private fun plural(count: Int, noun: String) = "$count $noun" + if (count == 1) "" else "s"

private fun TasteFactor.chipLabel(): String = when (type) {
    TasteFactor.Type.Director -> "🎬 $name"
    TasteFactor.Type.Genre -> "🎞 $name"
    TasteFactor.Type.Actor -> "⭐ $name"
}

@Preview(showBackground = true, backgroundColor = 0xFF10131C, heightDp = 1500)
@Composable
private fun GeniePreview() {
    val movie = SampleCatalog.criticallyAcclaimed.first()
    PopReelTheme(darkTheme = true) {
        GenieContent(
            uiState = GenieUiState(
                catalog = CatalogStatus(9919, CatalogSyncState.Synced),
                likedCount = 8,
                dislikedCount = 3,
                markedCount = 14,
                recommendations = ListState.loaded(
                    listOf(
                        Recommendation(
                            movie, 97, "Because you liked Arrival & Sicario",
                            "Directed by Denis Villeneuve, whose films you liked; fits your love of Science Fiction.",
                            setOf(TasteFactor(TasteFactor.Type.Director, "Denis Villeneuve")),
                        ),
                    ),
                ),
                tasteFactors = listOf(TasteFactor(TasteFactor.Type.Director, "Denis Villeneuve"), TasteFactor(TasteFactor.Type.Genre, "Drama")),
            ),
            contentPadding = PaddingValues(),
            onOpenMovie = {},
            onOpenSwipe = {},
            onFactorSelect = {},
            onChoice = { _, _ -> },
            onRetrySync = {},
        )
    }
}
