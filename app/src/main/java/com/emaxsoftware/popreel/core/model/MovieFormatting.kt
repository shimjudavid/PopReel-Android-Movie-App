package com.emaxsoftware.popreel.core.model

import java.util.Locale

/** 6227 → "6.2k", 1000 → "1k", 845 → "845". */
fun formatCompactCount(count: Int): String = when {
    count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format(Locale.US, "%.1fk", count / 1_000.0)
    else -> count.toString()
}.replace(".0", "")

/** 96 → "1h 36m". */
fun formatRuntime(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "${rest}m"
        rest == 0 -> "${hours}h"
        else -> "${hours}h ${rest}m"
    }
}
