package com.emaxsoftware.popreel.feature.explore

import androidx.compose.runtime.Stable

@Stable
interface ExploreActions {
    fun onQueryChange(query: String) {}
    fun onGenreSelect(genre: String) {}
    fun onSortSelect(sort: CatalogSort) {}
    fun onRetrySync() {}
}
