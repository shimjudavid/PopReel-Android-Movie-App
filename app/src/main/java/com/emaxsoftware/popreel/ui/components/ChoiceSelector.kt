package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.emaxsoftware.popreel.core.model.MovieChoice

/** Liked / Disliked / Not seen toggles; tapping the active one clears it. */
@Composable
fun ChoiceSelector(
    selected: MovieChoice?,
    onChoiceChange: (MovieChoice?) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.extraSmall
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        MovieChoice.entries.forEach { choice ->
            val isSelected = choice == selected
            val (icon, _) = choice.iconAndTint()
            val toggleModifier = Modifier
                .weight(1f)
                .heightIn(min = if (compact) 48.dp else 60.dp)
                .semantics {
                    role = Role.Switch
                    this.selected = isSelected
                }
            val onClick = { onChoiceChange(if (isSelected) null else choice) }
            // Icon above label: three side-by-side labels don't fit a phone-width row.
            val content: @Composable () -> Unit = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    if (!compact) {
                        Spacer(Modifier.size(2.dp))
                        Text(choice.label(), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (isSelected) {
                Button(
                    onClick = onClick,
                    modifier = toggleModifier,
                    shape = shape,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
                ) { content() }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    modifier = toggleModifier,
                    shape = shape,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    border = BorderStroke(1.dp, colors.surfaceContainerHighest),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
                ) { content() }
            }
        }
    }
}
