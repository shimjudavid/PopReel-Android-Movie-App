package com.emaxsoftware.popreel.feature.trivia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.emaxsoftware.popreel.core.model.TriviaQuestion
import com.emaxsoftware.popreel.ui.components.BackTopBar
import com.emaxsoftware.popreel.ui.components.EmptyBlock
import com.emaxsoftware.popreel.ui.components.LoadingBlock
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import java.util.Locale

@Composable
fun TriviaScreen(onExit: () -> Unit, viewModel: TriviaViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.onStart() }
    // Leaving the app mid-question shouldn't cost the player the round.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onPause() }
    TriviaContent(
        state = state,
        onExit = onExit,
        actions = viewModel,
    )
}

@Composable
private fun TriviaContent(state: TriviaUiState, onExit: () -> Unit, actions: TriviaActions) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colors.background,
        topBar = { BackTopBar(title = "CineTrivia Showdown", onBack = onExit, backLabel = "Back to Arcade") },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (state.phase) {
                TriviaPhase.Loading -> LoadingBlock(Modifier.padding(16.dp).fillMaxWidth().height(240.dp), message = "Shuffling questions…")
                TriviaPhase.Unavailable -> EmptyBlock(
                    "No trivia questions are on this device yet. Connect once so PopReel can download the catalog, then try again.",
                    Modifier.padding(24.dp),
                )
                TriviaPhase.Finished -> FinalScore(state, onPlayAgain = actions::onPlayAgain, onExit = onExit)
                TriviaPhase.Question, TriviaPhase.Revealed -> GameBoard(state, actions)
            }
            if (state.paused) PausedOverlay(onResume = actions::onResume, onQuit = onExit)
        }
    }
}

@Composable
private fun GameBoard(state: TriviaUiState, actions: TriviaActions) {
    val question = state.current ?: return
    val colors = MaterialTheme.colorScheme
    val revealed = state.phase == TriviaPhase.Revealed
    val scroll = rememberScrollState()
    // Feedback and the Next button sit below the answers; bring them into view on reveal.
    LaunchedEffect(revealed, state.index) {
        if (revealed) scroll.animateScrollTo(scroll.maxValue) else scroll.scrollTo(0)
    }
    Column(
        Modifier
            .widthIn(max = 640.dp)
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusPill("Q ${state.questionNumber} / ${state.total}")
            if (state.streak > 1) StatusPill("🔥 ${state.streak}× STREAK", accent = true)
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(colors.surfaceContainerHigh, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Filled.Stars, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(String.format(Locale.US, "%,d PTS", state.score), style = MaterialTheme.typography.labelLarge, color = colors.primary)
            }
        }

        TimerCard(state)

        OutlinedButton(
            onClick = { actions.onFiftyFifty() },
            enabled = !state.fiftyFiftyUsed && !revealed,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Icon(Icons.Outlined.Balance, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (state.fiftyFiftyUsed) "50:50 used" else "50:50 lifeline", style = MaterialTheme.typography.labelLarge)
        }

        QuestionCard(question, revealed)

        question.options.forEachIndexed { index, option ->
            AnswerTile(
                letter = ('A' + index).toString(),
                text = option,
                state = when {
                    revealed && index == question.answerIndex -> TileState.Correct
                    revealed && index == state.lockedAnswer -> TileState.Wrong
                    index in state.eliminated -> TileState.Eliminated
                    index == state.selected -> TileState.Selected
                    else -> TileState.Idle
                },
                onClick = { actions.onSelect(index) },
                enabled = !revealed && index !in state.eliminated,
            )
        }

        if (revealed) RevealCard(state, question, actions)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = { actions.onPause() },
                shape = MaterialTheme.shapes.large,
                color = colors.surfaceContainer,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Pause, contentDescription = "Pause") }
            }
            Button(
                onClick = { if (revealed) actions.onNext() else actions.onLockIn() },
                enabled = revealed || state.selected != null,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                Icon(if (revealed) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Lock, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        !revealed -> "LOCK IN ANSWER"
                        state.questionNumber == state.total -> "SEE FINAL SCORE"
                        else -> "NEXT QUESTION"
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun TimerCard(state: TriviaUiState) {
    val colors = MaterialTheme.colorScheme
    val urgent = state.secondsLeft <= 5
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
            .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.large)
            .padding(12.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "${state.secondsLeft} seconds left"
        }) {
            CircularProgressIndicator(
                progress = { state.timeFraction },
                color = if (urgent) colors.secondaryContainer else colors.primaryContainer,
                trackColor = colors.surfaceVariant,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp),
            )
            Text("${state.secondsLeft}", style = MaterialTheme.typography.titleMedium, color = if (urgent) colors.secondary else colors.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("SPEED BONUS", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Text(
                if (state.phase == TriviaPhase.Question) "+${state.potentialPoints} pts if correct now" else "Answer locked",
                style = MaterialTheme.typography.titleSmall,
                color = colors.tertiary,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("CORRECT", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Text("${state.correctCount} / ${state.index + if (state.phase == TriviaPhase.Revealed) 1 else 0}", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
        }
    }
}

@Composable
private fun QuestionCard(question: TriviaQuestion, revealed: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
            .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            question.category.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = colors.primary,
            modifier = Modifier
                .background(colors.surfaceContainerHighest, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
        // The poster is shown only after answering: for plot and year questions it would give the answer away.
        if (revealed && question.posterUrl != null) {
            AsyncImage(
                model = question.posterUrl,
                contentDescription = question.movieTitle?.let { "$it poster" },
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(colors.surfaceContainer),
                error = ColorPainter(colors.surfaceContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
        }
        Text(
            question.question,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )
    }
}

private enum class TileState { Idle, Selected, Eliminated, Correct, Wrong }

@Composable
private fun AnswerTile(letter: String, text: String, state: TileState, onClick: () -> Unit, enabled: Boolean) {
    val colors = MaterialTheme.colorScheme
    val positive = Color(0xFF16A34A)
    val (container, border) = when (state) {
        TileState.Selected -> colors.surfaceContainerHigh to colors.primaryContainer
        TileState.Correct -> positive.copy(alpha = 0.18f) to positive
        TileState.Wrong -> colors.secondaryContainer.copy(alpha = 0.25f) to colors.secondaryContainer
        TileState.Eliminated -> colors.surfaceContainerLow to colors.surfaceContainerLow
        TileState.Idle -> colors.surfaceContainer to colors.surfaceContainerHigh
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = container,
        border = BorderStroke(1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .semantics {
                selected = state == TileState.Selected
                stateDescription = when (state) {
                    TileState.Correct -> "Correct answer"
                    TileState.Wrong -> "Your answer, incorrect"
                    TileState.Eliminated -> "Removed by 50:50"
                    else -> ""
                }
            },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Box(
                Modifier
                    .size(32.dp)
                    .background(if (state == TileState.Selected) colors.primaryContainer else colors.surfaceContainerHighest, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    letter,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state == TileState.Selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (state == TileState.Eliminated) colors.outline.copy(alpha = 0.5f) else colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            when (state) {
                TileState.Correct -> Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = positive)
                TileState.Wrong -> Icon(Icons.Filled.Cancel, contentDescription = null, tint = colors.secondaryContainer)
                TileState.Selected -> Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.primaryContainer)
                else -> Unit
            }
        }
    }
}

/** Auto-advances after a moment; press and hold to keep it on screen while reading. */
@Composable
private fun RevealCard(state: TriviaUiState, question: TriviaQuestion, actions: TriviaActions) {
    val colors = MaterialTheme.colorScheme
    val correct = state.answeredCorrectly
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLowest, MaterialTheme.shapes.large)
            .border(1.dp, if (correct) Color(0xFF16A34A) else colors.secondaryContainer, MaterialTheme.shapes.large)
            .pointerInput(actions) {
                detectTapGestures(onPress = {
                    actions.onRevealHold(true)
                    tryAwaitRelease()
                    actions.onRevealHold(false)
                })
            }
            .padding(14.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            when {
                correct -> "Correct! +${state.lastPoints} pts"
                state.lockedAnswer == null -> "Time's up"
                else -> "Not quite"
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (correct) Color(0xFF16A34A) else colors.secondary,
        )
        Text(
            "Answer: ${question.options[question.answerIndex]}" + (question.movieTitle?.let { " · from “$it”" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.TouchApp, contentDescription = null, tint = colors.outline, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                if (state.revealHeld) "Holding — release to continue" else "Press and hold to keep reading",
                style = MaterialTheme.typography.bodySmall,
                color = colors.outline,
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, accent: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = if (accent) colors.secondary else colors.onSurface,
        modifier = Modifier
            .background(if (accent) colors.secondaryContainer.copy(alpha = 0.2f) else colors.surfaceContainerHigh, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun PausedOverlay(onResume: () -> Unit, onQuit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLowest.copy(alpha = 0.92f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Paused", style = MaterialTheme.typography.displaySmall, color = colors.onSurface, modifier = Modifier.semantics { heading() })
            Text("The clock is stopped.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Button(
                onClick = onResume,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
                modifier = Modifier.widthIn(min = 200.dp).height(52.dp),
            ) { Text("RESUME", style = MaterialTheme.typography.labelLarge) }
            OutlinedButton(onClick = onQuit, shape = MaterialTheme.shapes.large, modifier = Modifier.widthIn(min = 200.dp).height(52.dp)) {
                Text("QUIT TO ARCADE", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
            }
        }
    }
}

@Composable
private fun FinalScore(state: TriviaUiState, onPlayAgain: () -> Unit, onExit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .widthIn(max = 480.dp)
            .padding(24.dp)
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
            .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.large)
            .padding(24.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Filled.Stars, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(48.dp))
        Text("Round complete", style = MaterialTheme.typography.headlineMedium, color = colors.onSurface, modifier = Modifier.semantics { heading() })
        Text(String.format(Locale.US, "%,d", state.score), style = MaterialTheme.typography.displayLarge, color = colors.primaryContainer)
        Text("points", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        Text(
            "${state.correctCount} of ${state.total} correct · best streak ${state.bestStreak}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onPlayAgain,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text("PLAY AGAIN", style = MaterialTheme.typography.labelLarge) }
        OutlinedButton(onClick = onExit, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("BACK TO ARCADE", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF10131C, heightDp = 1100)
@Composable
private fun TriviaPreview() {
    PopReelTheme(darkTheme = true) {
        TriviaContent(
            state = TriviaUiState(
                phase = TriviaPhase.Question,
                questions = listOf(
                    TriviaQuestion("q", "m", "director", "Who directed 12 Angry Men?", listOf("Paul Thomas Anderson", "Sidney Lumet", "Sam Peckinpah", "Brad Bird"), 1),
                ),
                selected = 1,
                eliminated = setOf(2),
                fiftyFiftyUsed = true,
                timeLeftMillis = 14_000,
                score = 1420,
                streak = 3,
            ),
            onExit = {},
            actions = object : TriviaActions {},
        )
    }
}
