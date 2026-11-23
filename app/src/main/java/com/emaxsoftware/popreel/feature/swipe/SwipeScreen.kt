package com.emaxsoftware.popreel.feature.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.emaxsoftware.popreel.core.data.CatalogSyncState
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.ui.components.EmptyBlock
import com.emaxsoftware.popreel.ui.components.ErrorBlock
import com.emaxsoftware.popreel.ui.components.GenreFilterBar
import com.emaxsoftware.popreel.ui.components.LoadingBlock
import com.emaxsoftware.popreel.ui.components.Pill
import com.emaxsoftware.popreel.ui.components.StatusDot
import com.emaxsoftware.popreel.ui.components.SwipeActionBar
import com.emaxsoftware.popreel.ui.components.SwipeDeck
import com.emaxsoftware.popreel.ui.components.SwipeDirection
import com.emaxsoftware.popreel.ui.components.SwipeMovieCard
import com.emaxsoftware.popreel.ui.components.rememberSwipeDeckState
import com.emaxsoftware.popreel.ui.preview.SampleCatalog
import com.emaxsoftware.popreel.ui.state.CHOICE_SAVE_FAILED
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.INITIAL_SETUP_MESSAGE
import com.emaxsoftware.popreel.ui.state.INITIAL_SETUP_TITLE
import com.emaxsoftware.popreel.ui.state.ListState
import com.emaxsoftware.popreel.ui.state.choiceSavedMessage
import com.emaxsoftware.popreel.ui.state.choiceUndoneMessage
import com.emaxsoftware.popreel.ui.state.userMessage
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import kotlinx.coroutines.launch

@Composable
fun SwipeScreen(
    contentPadding: PaddingValues,
    onOpenMovie: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SwipeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                currentOnShowMessage(
                    when (effect) {
                        is SwipeEffect.Undone -> choiceUndoneMessage(effect.movieTitle, effect.choice)
                        is SwipeEffect.ChoiceSaved -> choiceSavedMessage(effect.movieTitle, effect.choice)
                        SwipeEffect.ChoiceFailed -> CHOICE_SAVE_FAILED
                    },
                )
            }
        }
    }
    SwipeContent(uiState, contentPadding, onOpenMovie, actions = viewModel, modifier = modifier)
}

/** Non-scrolling: the deck takes the space between the status row and the action bar. */
@Composable
private fun SwipeContent(
    uiState: SwipeUiState,
    contentPadding: PaddingValues,
    onOpenMovie: (String) -> Unit,
    actions: SwipeActions,
    modifier: Modifier = Modifier,
) {
    var showReview by rememberSaveable { mutableStateOf(false) }
    val remaining = uiState.remaining
    val current = remaining.firstOrNull()
    val queue = uiState.queue
    val deckState = rememberSwipeDeckState()
    val scope = rememberCoroutineScope()
    val spacing = PopReelTheme.spacing
    val gutter = spacing.gutter
    val deckReady = uiState.catalog.hasLocalCatalog && !queue.isLoading && queue.error == null && current != null

    fun fling(direction: SwipeDirection) {
        scope.launch { deckState.swipe(direction) }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = gutter, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Pill {
                StatusDot(Modifier.size(8.dp), pulsing = queue.isLoading)
                Text("QUEUE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (queue.isLoading) "–" else uiState.remainingLabel, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.weight(1f))
            Pill(
                onClick = actions::onUndo,
                enabled = uiState.canUndo,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Undo", style = MaterialTheme.typography.labelLarge)
            }
            Pill(onClick = { showReview = true }) {
                Icon(
                    Icons.Filled.DoneAll,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text("${uiState.ratedCount} Rated", style = MaterialTheme.typography.labelLarge)
            }
        }

        GenreFilterBar(
            leading = DeckFilter.Mode.entries.map { it.label },
            genres = uiState.genres,
            selected = uiState.filter.key,
            onSelect = { key -> actions.onFilterSelect(DeckFilter.fromKey(key)) },
            maxGenres = 4,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(bottom = spacing.sm),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f), Color.Transparent),
                    ),
                )
                .padding(horizontal = gutter, vertical = spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.catalog.needsInitialSetup ->
                    ErrorBlock(INITIAL_SETUP_MESSAGE, actions::onRetrySync, Modifier.fillMaxWidth(), title = INITIAL_SETUP_TITLE)

                uiState.catalog.isSettingUp || queue.isLoading ->
                    LoadingBlock(Modifier.fillMaxSize(), message = "Shuffling the deck…")

                queue.error != null -> ErrorBlock(queue.error.userMessage(), actions::onRetrySync, Modifier.fillMaxWidth())

                remaining.isEmpty() -> CaughtUp()

                else -> SwipeDeck(
                    items = remaining,
                    itemKey = { it.id },
                    state = deckState,
                    onSwiped = { movie, direction -> actions.onSwiped(movie, direction.choice) },
                    directionLabel = { it.label },
                    // Leave room for the fanned back cards below the top card.
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                ) { movie, _ ->
                    SwipeMovieCard(movie = movie, onOpenDetails = { onOpenMovie(movie.id) }, modifier = Modifier.fillMaxSize())
                }
            }
        }

        SwipeActionBar(
            enabled = deckReady,
            onDislike = { fling(SwipeDirection.Left) },
            onUnseen = { fling(SwipeDirection.Down) },
            onLike = { fling(SwipeDirection.Right) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = gutter, vertical = spacing.sm),
        )
        SwipeHint(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = gutter)
                .padding(bottom = spacing.sm),
        )
    }

    if (showReview) {
        ChoiceReviewSheet(
            items = uiState.review,
            onChoiceChange = actions::onChoiceChange,
            onOpenMovie = {
                showReview = false
                onOpenMovie(it)
            },
            onDismiss = { showReview = false },
        )
    }
}

private val SwipeDirection.choice: MovieChoice
    get() = when (this) {
        SwipeDirection.Right -> MovieChoice.Liked
        SwipeDirection.Left -> MovieChoice.Disliked
        SwipeDirection.Down -> MovieChoice.Unseen
    }

private val SwipeDirection.label: String
    get() = when (this) {
        SwipeDirection.Right -> "Like"
        SwipeDirection.Left -> "Dislike"
        SwipeDirection.Down -> "Not seen"
    }

@Composable
private fun SwipeHint(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val key = SpanStyle(color = colors.onSurface, fontWeight = FontWeight.SemiBold)
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = colors.primaryContainer, fontWeight = FontWeight.Bold)) { append("→ ") }
            withStyle(key) { append("Like") }
            append("   •   ← ")
            withStyle(key) { append("Dislike") }
            append("   •   ↓ ")
            withStyle(key) { append("Not seen") }
        },
        style = MaterialTheme.typography.bodySmall,
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun CaughtUp() {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.DoneAll, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text("You're all caught up", style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
        EmptyBlock("You've marked every film in this filter. Try another one for more picks.")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF10131C, heightDp = 860)
@Composable
private fun SwipeContentPreview() {
    PopReelTheme(darkTheme = true) {
        SwipeContent(
            uiState = SwipeUiState(
                catalog = CatalogStatus(movieCount = 9919, sync = CatalogSyncState.Synced),
                queue = ListState.loaded(SampleCatalog.swipeQueue),
            ),
            contentPadding = PaddingValues(),
            onOpenMovie = {},
            actions = object : SwipeActions {},
        )
    }
}
