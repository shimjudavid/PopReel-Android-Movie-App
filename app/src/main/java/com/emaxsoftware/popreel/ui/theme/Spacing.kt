package com.emaxsoftware.popreel.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Stitch spacing tokens (`spacing` block of the project design theme). */
@Immutable
data class PopReelSpacing(
    val xs: Dp = 4.dp,      // space-xs
    val sm: Dp = 8.dp,      // space-sm
    val md: Dp = 16.dp,     // space-md
    val lg: Dp = 24.dp,     // space-lg
    val xl: Dp = 32.dp,     // space-xl
    val gutter: Dp = 16.dp, // gutter
    val margin: Dp = 16.dp, // margin
)

val LocalPopReelSpacing = staticCompositionLocalOf { PopReelSpacing() }
