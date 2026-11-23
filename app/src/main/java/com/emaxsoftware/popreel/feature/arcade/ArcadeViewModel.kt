package com.emaxsoftware.popreel.feature.arcade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emaxsoftware.popreel.core.data.SettingsRepository
import com.emaxsoftware.popreel.core.model.ArcadeStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArcadeViewModel @Inject constructor(settings: SettingsRepository) : ViewModel() {
    val stats: StateFlow<ArcadeStats> =
        settings.arcadeStats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArcadeStats())
}
