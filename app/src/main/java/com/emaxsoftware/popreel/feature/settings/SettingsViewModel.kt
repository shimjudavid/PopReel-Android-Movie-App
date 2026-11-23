package com.emaxsoftware.popreel.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.SettingsRepository
import com.emaxsoftware.popreel.core.model.AppSettings
import com.emaxsoftware.popreel.core.model.ThemeMode
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val catalog: CatalogStatus = CatalogStatus(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val catalog: CatalogRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        combine(catalog.movieCount, catalog.syncState, ::CatalogStatus),
        ::SettingsUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun onGameSoundChange(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setGameSoundEnabled(enabled) }
    }

    fun onRefreshCatalog() {
        viewModelScope.launch { catalog.sync() }
    }
}
