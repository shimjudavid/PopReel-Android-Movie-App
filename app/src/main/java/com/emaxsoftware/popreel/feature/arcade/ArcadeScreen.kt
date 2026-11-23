package com.emaxsoftware.popreel.feature.arcade

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.IncompleteCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emaxsoftware.popreel.core.model.ArcadeStats
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import java.util.Locale

@Composable
fun ArcadeScreen(
    contentPadding: PaddingValues,
    onPlayTrivia: () -> Unit,
    onPlayPuzzle: () -> Unit,
    viewModel: ArcadeViewModel = hiltViewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    ArcadeContent(stats, contentPadding, onPlayTrivia, onPlayPuzzle)
}

@Composable
private fun ArcadeContent(
    stats: ArcadeStats,
    contentPadding: PaddingValues,
    onPlayTrivia: () -> Unit,
    onPlayPuzzle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .widthIn(max = 720.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Cinema Arcade",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                Text(
                    "Test your film knowledge in timed trivia rounds and rebuild classic posters tile by tile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Icons.Filled.EmojiEvents, "Points", String.format(Locale.US, "%,d", stats.triviaTotalPoints), Modifier.weight(1f))
                    StatCard(Icons.Filled.LocalFireDepartment, "Best streak", "${stats.triviaBestStreak}", Modifier.weight(1f), accent = true)
                    StatCard(Icons.Filled.Extension, "Solved", "${stats.puzzlesSolved}", Modifier.weight(1f))
                }
            }

            GameCard(
                tag = "Fast-paced quiz • 10 questions",
                tagIcon = Icons.Outlined.Timer,
                badgeIcon = Icons.Filled.Movie,
                badgeText = "4×",
                title = "CineTrivia Showdown",
                subtitle = "Four-choice film questions against the clock, with streak bonuses",
                features = listOf(
                    Icons.Filled.CheckCircle to "4-choice questions",
                    Icons.Outlined.Balance to "50:50 lifeline",
                    Icons.Filled.LocalFireDepartment to "Streak bonuses",
                    Icons.Outlined.Pause to "Pause any time",
                ),
                footnote = if (stats.triviaRoundsPlayed > 0) {
                    "Best round: ${String.format(Locale.US, "%,d", stats.triviaBestScore)} pts over ${stats.triviaRoundsPlayed} rounds"
                } else {
                    "Questions come from your downloaded catalog — no connection needed"
                },
                cta = "Play trivia now",
                onPlay = onPlayTrivia,
            )

            GameCard(
                tag = "Visual reconstruction • 3×3 tiles",
                tagIcon = Icons.Outlined.GridView,
                badgeIcon = Icons.Filled.Extension,
                badgeText = "3×3",
                title = "Poster Puzzle",
                subtitle = "Swap shuffled tiles to rebuild a famous movie poster",
                features = listOf(
                    Icons.Outlined.Image to "Real catalog posters",
                    Icons.Outlined.IncompleteCircle to "Live progress",
                    Icons.Outlined.Lightbulb to "Tap two tiles to swap",
                    Icons.Filled.PlayArrow to "Instant next puzzle",
                ),
                footnote = "Solved so far: ${stats.puzzlesSolved}",
                cta = "Launch poster puzzle",
                onPlay = onPlayPuzzle,
            )
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.small)
            .border(1.dp, if (accent) colors.primaryContainer.copy(alpha = 0.3f) else colors.surfaceContainerHigh, MaterialTheme.shapes.small)
            .padding(vertical = 14.dp, horizontal = 8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = if (accent) colors.primaryContainer else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GameCard(
    tag: String,
    tagIcon: ImageVector,
    badgeIcon: ImageVector,
    badgeText: String,
    title: String,
    subtitle: String,
    features: List<Pair<ImageVector, String>>,
    footnote: String,
    cta: String,
    onPlay: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.small)
            .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.small)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(colors.surfaceContainer, MaterialTheme.shapes.small)
                .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(tagIcon, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(tag.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(colors.surfaceContainer, MaterialTheme.shapes.small)
                    .border(1.dp, colors.primaryContainer.copy(alpha = 0.4f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(badgeIcon, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(30.dp))
                Text(
                    badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(colors.primaryContainer, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface, modifier = Modifier.semantics { heading() })
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        features.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (icon, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.surfaceContainer, MaterialTheme.shapes.small)
                            .padding(10.dp),
                    ) {
                        Icon(icon, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Text(footnote, style = MaterialTheme.typography.bodySmall, color = colors.outline)
        Button(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(cta.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF10131C, heightDp = 1400)
@Composable
private fun ArcadePreview() {
    PopReelTheme(darkTheme = true) {
        ArcadeContent(ArcadeStats(3450, 1200, 6, 4, 12), PaddingValues(), {}, {})
    }
}
