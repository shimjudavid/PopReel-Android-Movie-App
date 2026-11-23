package com.emaxsoftware.popreel.ui.state

import com.emaxsoftware.popreel.core.data.CatalogSyncState
import com.emaxsoftware.popreel.ui.components.ConnectionStatus
import java.util.Locale

/** Local catalog availability, which decides between setup, offline and live presentation. */
data class CatalogStatus(
    /** Null until the local count is known. */
    val movieCount: Int? = null,
    val sync: CatalogSyncState = CatalogSyncState.Idle,
) {
    val hasLocalCatalog: Boolean get() = (movieCount ?: 0) > 0

    /** First launch without a catalog, and the download failed (typically offline). */
    val needsInitialSetup: Boolean get() = movieCount == 0 && sync is CatalogSyncState.Failed

    /** First launch, still downloading (or about to). */
    val isSettingUp: Boolean get() = movieCount == 0 && sync !is CatalogSyncState.Failed

    val connection: ConnectionStatus
        get() = when (sync) {
            CatalogSyncState.Synced -> ConnectionStatus.Live
            is CatalogSyncState.Failed -> ConnectionStatus.Offline
            CatalogSyncState.Idle, CatalogSyncState.Syncing -> ConnectionStatus.Syncing
        }

    val label: String
        get() = when {
            hasLocalCatalog -> String.format(Locale.US, "%,d films in catalog", movieCount)
            needsInitialSetup -> "No catalog on this device"
            else -> "Preparing catalog…"
        }
}
