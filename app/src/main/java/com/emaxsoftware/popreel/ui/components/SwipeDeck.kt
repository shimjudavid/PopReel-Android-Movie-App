package com.emaxsoftware.popreel.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class SwipeDirection { Right, Left, Down }

/**
 * Drives the top card of a [SwipeDeck]. Hoist it to trigger swipes from
 * buttons via [swipe].
 */
@Stable
class SwipeDeckState {
    internal val offset = Animatable(Offset.Zero, Offset.VectorConverter)
    internal var size by mutableStateOf(IntSize.Zero)
    internal var onSwiped: (SwipeDirection) -> Unit = {}

    var isSwiping by mutableStateOf(false)
        private set

    private val horizontalThreshold get() = size.width * 0.28f
    private val verticalThreshold get() = size.height * 0.18f

    /** Flings the top card off-screen, then reports [direction] to the deck. */
    suspend fun swipe(direction: SwipeDirection) {
        if (isSwiping || size == IntSize.Zero) return
        isSwiping = true
        try {
            val current = offset.value
            val target = when (direction) {
                SwipeDirection.Right -> Offset(size.width * 1.5f, current.y)
                SwipeDirection.Left -> Offset(-size.width * 1.5f, current.y)
                SwipeDirection.Down -> Offset(current.x, size.height * 1.3f)
            }
            offset.animateTo(target, tween(durationMillis = 280, easing = FastOutLinearInEasing))
            onSwiped(direction)
            offset.snapTo(Offset.Zero)
        } finally {
            isSwiping = false
        }
    }

    internal suspend fun dragBy(delta: Offset) {
        // A queued drag update must not interrupt a fling that already started.
        if (isSwiping) return
        offset.snapTo(offset.value + delta)
    }

    /** On release: commit if past a threshold, otherwise spring back. */
    internal suspend fun settle() {
        val direction = pendingDecision()
        if (direction != null) {
            swipe(direction)
        } else {
            offset.animateTo(
                Offset.Zero,
                spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
            )
        }
    }

    private fun pendingDecision(): SwipeDirection? {
        val (x, y) = offset.value
        return when {
            x > horizontalThreshold -> SwipeDirection.Right
            x < -horizontalThreshold -> SwipeDirection.Left
            y > verticalThreshold && y > abs(x) -> SwipeDirection.Down
            else -> null
        }
    }

    /** 0..1 progress of the current drag towards [direction]. */
    internal fun progress(direction: SwipeDirection): Float {
        if (size == IntSize.Zero) return 0f
        val (x, y) = offset.value
        val raw = when (direction) {
            SwipeDirection.Right -> x / horizontalThreshold
            SwipeDirection.Left -> -x / horizontalThreshold
            SwipeDirection.Down -> if (y > abs(x)) y / verticalThreshold else 0f
        }
        return raw.coerceIn(0f, 1f)
    }

    internal fun overallProgress(): Float = SwipeDirection.entries.maxOf { progress(it) }
}

@Composable
fun rememberSwipeDeckState(): SwipeDeckState = remember { SwipeDeckState() }

/**
 * Tinder-style card stack. The first item is the draggable top card; the next
 * cards peek behind it and grow into place as the top card is dragged away.
 * Reports a right, left or down fling; each direction is also offered to
 * accessibility services as a custom action named by [directionLabel].
 */
@Composable
fun <T> SwipeDeck(
    items: List<T>,
    itemKey: (T) -> Any,
    state: SwipeDeckState,
    onSwiped: (item: T, direction: SwipeDirection) -> Unit,
    /** Stamp text and accessibility action label for each direction, e.g. Right → "Like". */
    directionLabel: (SwipeDirection) -> String,
    modifier: Modifier = Modifier,
    visibleCount: Int = 3,
    cardShape: Shape = MaterialTheme.shapes.medium,
    card: @Composable (item: T, isTop: Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val scrimColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val currentTop by rememberUpdatedState(items.firstOrNull())
    val currentOnSwiped by rememberUpdatedState(onSwiped)
    SideEffect {
        state.onSwiped = { direction -> currentTop?.let { currentOnSwiped(it, direction) } }
    }
    val depthOffsetPx = with(LocalDensity.current) { 12.dp.toPx() }

    Box(modifier.onSizeChanged { state.size = it }) {
        val visible = items.take(visibleCount)
        for (depth in visible.indices.reversed()) {
            val item = visible[depth]
            key(itemKey(item)) {
                val isTop = depth == 0
                Box(
                    Modifier
                        .fillMaxSize()
                        // Gesture detection must sit outside graphicsLayer: inside it,
                        // pointer positions are in the card's moving/rotating space and
                        // drag deltas feed back on themselves.
                        .then(if (isTop) Modifier.swipeable(state, scope, directionLabel) else Modifier)
                        .graphicsLayer {
                            if (isTop) {
                                val o = state.offset.value
                                translationX = o.x
                                translationY = o.y
                                rotationZ = if (size.width > 0f) o.x / size.width * 18f else 0f
                            } else {
                                // Back cards move one step forward as the top card leaves.
                                val d = depth - state.overallProgress()
                                val scale = 1f - 0.05f * d
                                scaleX = scale
                                scaleY = scale
                                translationY = depthOffsetPx * d
                                // Keep the next card opaque so deeper cards can't show
                                // through it; only the bottom-most peek fades out.
                                alpha = if (d > 1f) (1f - 0.6f * (d - 1f)).coerceIn(0f, 1f) else 1f
                                rotationZ = backCardRotation(d)
                            }
                        },
                ) {
                    card(item, isTop)
                    if (isTop) {
                        SwipeStamps(state, directionLabel)
                    } else {
                        // Dim scrim that clears as this card moves to the front.
                        Box(
                            Modifier
                                .matchParentSize()
                                .graphicsLayer {
                                    alpha = (0.3f * (depth - state.overallProgress())).coerceIn(0f, 0.6f)
                                }
                                .background(scrimColor, cardShape),
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.swipeable(
    state: SwipeDeckState,
    scope: CoroutineScope,
    directionLabel: (SwipeDirection) -> String,
): Modifier = this
    .pointerInput(state) {
        detectDragGestures(
            onDragEnd = { scope.launch { state.settle() } },
            onDragCancel = { scope.launch { state.settle() } },
        ) { change, dragAmount ->
            if (!state.isSwiping) {
                change.consume()
                scope.launch { state.dragBy(dragAmount) }
            }
        }
    }
    .semantics {
        customActions = SwipeDirection.entries.map { direction ->
            CustomAccessibilityAction(directionLabel(direction)) {
                scope.launch { state.swipe(direction) }
                true
            }
        }
    }

/** Rotation per depth step: 0° → 1.5° → -3°, matching the design's fanned stack. */
private fun backCardRotation(depth: Float): Float {
    fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    return if (depth <= 1f) lerp(0f, 1.5f, depth) else lerp(1.5f, -3f, (depth - 1f).coerceAtMost(1f))
}

@Composable
private fun BoxScope.SwipeStamps(state: SwipeDeckState, directionLabel: (SwipeDirection) -> String) {
    val colors = MaterialTheme.colorScheme
    SwipeStamp(
        text = directionLabel(SwipeDirection.Right).uppercase(),
        container = colors.primaryContainer,
        content = colors.onPrimaryContainer,
        rotation = 12f,
        alignment = Alignment.TopEnd,
        progress = { state.progress(SwipeDirection.Right) },
    )
    SwipeStamp(
        text = directionLabel(SwipeDirection.Left).uppercase(),
        container = colors.surfaceContainerHighest,
        content = colors.onSurface,
        rotation = -12f,
        alignment = Alignment.TopStart,
        progress = { state.progress(SwipeDirection.Left) },
    )
    SwipeStamp(
        text = directionLabel(SwipeDirection.Down).uppercase(),
        container = colors.surfaceContainerHighest,
        content = colors.onSurface,
        rotation = 0f,
        alignment = Alignment.Center,
        progress = { state.progress(SwipeDirection.Down) },
    )
}

@Composable
private fun BoxScope.SwipeStamp(
    text: String,
    container: Color,
    content: Color,
    rotation: Float,
    alignment: Alignment,
    progress: () -> Float,
) {
    val shape = MaterialTheme.shapes.extraSmall
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = content,
        modifier = Modifier
            .align(alignment)
            .padding(24.dp)
            .graphicsLayer {
                alpha = progress()
                rotationZ = rotation
            }
            .background(container, shape)
            .border(1.dp, content.copy(alpha = 0.3f), shape)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clearAndSetSemantics { },
    )
}
