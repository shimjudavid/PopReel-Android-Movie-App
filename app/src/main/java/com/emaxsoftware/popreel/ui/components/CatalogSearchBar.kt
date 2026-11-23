package com.emaxsoftware.popreel.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun CatalogSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = {
            Text("Search titles or directors…", style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 6.dp),
            ) {
                IconButton(onClick = onVoiceSearchClick) {
                    Icon(Icons.Outlined.Mic, contentDescription = "Voice search")
                }
                FilledTonalIconButton(
                    onClick = onFilterClick,
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colors.surfaceContainerHighest,
                        contentColor = colors.onSurface,
                    ),
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Outlined.Tune,
                        contentDescription = "Filters",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = colors.surfaceContainer,
            focusedContainerColor = colors.surfaceContainerHigh,
            unfocusedBorderColor = colors.surfaceContainerHighest,
            focusedBorderColor = colors.primaryContainer,
            cursorColor = colors.primaryContainer,
            unfocusedLeadingIconColor = colors.outline,
            focusedLeadingIconColor = colors.outline,
            unfocusedTrailingIconColor = colors.outline,
            focusedTrailingIconColor = colors.outline,
            unfocusedPlaceholderColor = colors.outline,
            focusedPlaceholderColor = colors.outline,
        ),
    )
}
