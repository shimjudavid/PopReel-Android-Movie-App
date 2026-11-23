package com.emaxsoftware.popreel.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.SettingsRepository
import com.emaxsoftware.popreel.core.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopReelAppViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /** Eagerly, so the saved theme is applied before the first frame draws where possible. */
    val settings: StateFlow<AppSettings> =
        settingsRepository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private var syncStarted = false

    /** Refreshes the local catalog once per process; screens keep showing the saved copy meanwhile. */
    fun onAppStarted() {
        if (syncStarted) return
        syncStarted = true
        viewModelScope.launch { catalog.sync() }
    }

    fun onToggleSound() {
        viewModelScope.launch { settingsRepository.setGameSoundEnabled(!settings.value.gameSoundEnabled) }
    }
}
