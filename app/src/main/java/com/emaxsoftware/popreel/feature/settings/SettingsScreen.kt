package com.emaxsoftware.popreel.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emaxsoftware.popreel.core.data.CatalogSyncState
import com.emaxsoftware.popreel.core.model.ThemeMode
import com.emaxsoftware.popreel.ui.components.BackTopBar
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.userMessage
import com.emaxsoftware.popreel.ui.theme.PopReelTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onThemeModeChange = viewModel::onThemeModeChange,
        onGameSoundChange = viewModel::onGameSoundChange,
        onRefreshCatalog = {
            viewModel.onRefreshCatalog()
            onShowMessage("Refreshing the movie catalog…")
        },
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onGameSoundChange: (Boolean) -> Unit,
    onRefreshCatalog: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colors.background,
        topBar = { BackTopBar(title = "Settings", onBack = onBack) },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                Modifier
                    .widthIn(max = 640.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SettingsSection("Appearance", "Choose how PopReel looks. System follows your device setting.") {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = uiState.settings.themeMode == mode,
                                onClick = { onThemeModeChange(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = colors.primaryContainer,
                                    activeContentColor = colors.onPrimaryContainer,
                                ),
                            ) { Text(mode.name, style = MaterialTheme.typography.labelLarge) }
                        }
                    }
                }

                SettingsSection("Game sound", null) {
                    val enabled = uiState.settings.gameSoundEnabled
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(value = enabled, role = Role.Switch, onValueChange = onGameSoundChange),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Play sounds in Arcade games", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                            Text(
                                "Answer feedback in Trivia and the completion chime in Poster Puzzle.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = enabled,
                            onCheckedChange = null,
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primaryContainer, checkedThumbColor = colors.onPrimaryContainer),
                        )
                    }
                }

                SettingsSection("Movie catalog", "Your choices and settings are stored on this device and work offline.") {
                    Text(uiState.catalog.label, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    val syncText = when (val sync = uiState.catalog.sync) {
                        CatalogSyncState.Idle -> "Not refreshed yet this session"
                        CatalogSyncState.Syncing -> "Refreshing…"
                        CatalogSyncState.Synced -> "Up to date"
                        is CatalogSyncState.Failed -> sync.error.userMessage()
                    }
                    Text(syncText, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    OutlinedButton(
                        onClick = onRefreshCatalog,
                        enabled = uiState.catalog.sync != CatalogSyncState.Syncing,
                        shape = MaterialTheme.shapes.small,
                    ) { Text("Refresh catalog", style = MaterialTheme.typography.labelLarge, color = colors.primaryContainer) }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, description: String?, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
            .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface, modifier = Modifier.semantics { heading() })
        if (description != null) Text(description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        content()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF10131C)
@Composable
private fun SettingsPreview() {
    PopReelTheme(darkTheme = true) {
        SettingsContent(
            SettingsUiState(catalog = CatalogStatus(9919, CatalogSyncState.Synced)),
            onBack = {},
            onThemeModeChange = {},
            onGameSoundChange = {},
            onRefreshCatalog = {},
        )
    }
}
