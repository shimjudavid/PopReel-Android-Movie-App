package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emaxsoftware.popreel.ui.theme.PopReelTheme

/** The PopReel reel mark, drawn from the Stitch SVG (48×48 viewBox). */
@Composable
fun PopReelLogoMark(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val brand = PopReelTheme.extendedColors
    val fill = MaterialTheme.colorScheme.surfaceContainerLow
    val cutout = MaterialTheme.colorScheme.surfaceContainerLowest

    Canvas(modifier.size(size)) {
        val u = this.size.minDimension / 48f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        val center = p(24f, 24f)
        val gradient = Brush.linearGradient(
            colors = listOf(brand.brandStart, brand.brandEnd),
            start = p(6f, 6f),
            end = p(42f, 42f),
        )

        drawCircle(color = fill, radius = 20 * u, center = center)
        drawCircle(brush = gradient, radius = 20 * u, center = center, style = Stroke(3 * u))
        drawCircle(brush = gradient, radius = 8 * u, center = center)

        drawCircle(brand.brandStart, 2.5f * u, p(24f, 11f))
        drawCircle(brand.brandEnd, 2.5f * u, p(24f, 37f))
        drawCircle(brand.brandStart, 2.5f * u, p(11f, 24f))
        drawCircle(brand.brandEnd, 2.5f * u, p(37f, 24f))

        val play = Path().apply {
            moveTo(22 * u, 20 * u)
            lineTo(28 * u, 24 * u)
            lineTo(22 * u, 28 * u)
            close()
        }
        drawPath(play, cutout)

        val stroke = 2.5f * u
        drawLine(brand.brandStart, p(34f, 14f), p(37f, 11f), stroke, StrokeCap.Round)
        drawLine(brand.brandStart, p(38f, 16f), p(42f, 14f), stroke, StrokeCap.Round)
    }
}

/** Logo mark + "PopReel" wordmark. */
@Composable
fun PopReelLogo(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        PopReelLogoMark()
        Spacer(Modifier.width(PopReelTheme.spacing.sm))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.onSurface)) { append("Pop") }
                withStyle(SpanStyle(color = colors.primaryContainer)) { append("Reel") }
            },
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview
@Composable
private fun PopReelLogoPreview() {
    PopReelTheme(darkTheme = true) { PopReelLogo() }
}
