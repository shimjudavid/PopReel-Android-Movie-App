package com.emaxsoftware.popreel.feature.puzzle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.emaxsoftware.popreel.ui.components.BackTopBar
import com.emaxsoftware.popreel.ui.components.EmptyBlock
import com.emaxsoftware.popreel.ui.components.LoadingBlock
import java.util.Locale

@Composable
fun PosterPuzzleScreen(onExit: () -> Unit, viewModel: PosterPuzzleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.onStart() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onPause() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onResume() }

    val colors = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colors.background,
        topBar = { BackTopBar(title = "Poster Puzzle", onBack = onExit, backLabel = "Back to Arcade") },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (val s = state) {
                PuzzleUiState.Loading -> LoadingBlock(Modifier.padding(16.dp).fillMaxWidth().height(240.dp), message = "Picking a poster…")
                PuzzleUiState.Unavailable -> EmptyBlock(
                    "No posters are on this device yet. Connect once so PopReel can download the catalog.",
                    Modifier.padding(24.dp),
                )
                is PuzzleUiState.Playing -> PuzzleBoard(s, viewModel::onTileTap, viewModel::onNextPuzzle, onExit)
            }
        }
    }
}

@Composable
private fun PuzzleBoard(
    state: PuzzleUiState.Playing,
    onTileTap: (Int) -> Unit,
    onNextPuzzle: () -> Unit,
    onExit: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .widthIn(max = 520.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (state.solved) state.movie.title else "Mystery poster",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            Text("${state.placed}/${PuzzleRules.TILE_COUNT} placed", style = MaterialTheme.typography.labelLarge, color = colors.primaryContainer)
        }
        LinearProgressIndicator(
            progress = { state.placed / PuzzleRules.TILE_COUNT.toFloat() },
            color = colors.primaryContainer,
            trackColor = colors.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            if (state.solved) "Solved!" else "Tap a tile, then tap another to swap them.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLowest, MaterialTheme.shapes.medium)
                .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
                .padding(6.dp),
        ) {
            val gap = 4.dp
            val tileWidth = (maxWidth - gap * (PuzzleRules.GRID - 1)) / PuzzleRules.GRID
            val tileHeight = tileWidth * 1.5f
            val density = LocalDensity.current
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalPlatformContext.current)
                    .data(state.movie.heroImageUrl)
                    // Explicit size: rememberAsyncImagePainter doesn't infer it from layout.
                    .size(with(density) { (tileWidth * 3).roundToPx() }, with(density) { (tileHeight * 3).roundToPx() })
                    .build(),
            )
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                repeat(PuzzleRules.GRID) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        repeat(PuzzleRules.GRID) { col ->
                            val position = row * PuzzleRules.GRID + col
                            val piece = state.tiles[position]
                            PuzzleTile(
                                painter = painter,
                                piece = piece,
                                position = position,
                                tileWidth = tileWidth,
                                tileHeight = tileHeight,
                                selected = state.selected == position,
                                correct = piece == position,
                                enabled = !state.solved,
                                onClick = { onTileTap(position) },
                            )
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Stat(Icons.Outlined.Speed, "${state.moves} moves")
            Stat(Icons.Outlined.Timer, String.format(Locale.US, "%02d:%02d", state.elapsedSeconds / 60, state.elapsedSeconds % 60))
        }

        if (state.solved) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
                    .border(1.dp, colors.primaryContainer.copy(alpha = 0.4f), MaterialTheme.shapes.large)
                    .padding(20.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(40.dp))
                Text("Poster restored!", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                Text(
                    "You rebuilt “${state.movie.title}”${if (state.movie.year > 0) " (${state.movie.year})" else ""} in ${state.moves} moves.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Button(
                    onClick = onNextPuzzle,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text("NEXT PUZZLE", style = MaterialTheme.typography.labelLarge) }
                OutlinedButton(onClick = onExit, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("BACK TO ARCADE", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                }
            }
        }
    }
}

/** Draws one third-by-third slice of the poster: the full image, offset so only [piece]'s slice shows. */
@Composable
private fun PuzzleTile(
    painter: Painter,
    piece: Int,
    position: Int,
    tileWidth: Dp,
    tileHeight: Dp,
    selected: Boolean,
    correct: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val pieceRow = piece / PuzzleRules.GRID
    val pieceCol = piece % PuzzleRules.GRID
    val fullWidth = tileWidth * PuzzleRules.GRID
    val fullHeight = tileHeight * PuzzleRules.GRID
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(
            if (selected) 3.dp else 1.dp,
            when {
                selected -> colors.primaryContainer
                correct -> colors.primaryContainer.copy(alpha = 0.35f)
                else -> colors.surfaceContainerHigh
            },
        ),
        color = colors.surfaceContainer,
        modifier = Modifier
            .width(tileWidth)
            .height(tileHeight)
            .semantics {
                contentDescription = "Tile ${position + 1}" + if (correct) ", in place" else ""
                this.selected = selected
            },
    ) {
        Box(Modifier.clipToBounds(), contentAlignment = Alignment.Center) {
            // requiredSize centres the oversized image; shift it so this piece's slice is visible.
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .requiredSize(fullWidth, fullHeight)
                    .offset(
                        x = (fullWidth - tileWidth) / 2 - tileWidth * pieceCol,
                        y = (fullHeight - tileHeight) / 2 - tileHeight * pieceRow,
                    ),
            )
        }
    }
}

@Composable
private fun Stat(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
