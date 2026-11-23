package com.emaxsoftware.popreel.core.data

sealed interface CatalogSyncState {
    data object Idle : CatalogSyncState
    data object Syncing : CatalogSyncState
    data object Synced : CatalogSyncState
    data class Failed(val error: DataError) : CatalogSyncState
}
