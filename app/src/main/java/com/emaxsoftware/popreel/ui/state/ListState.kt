package com.emaxsoftware.popreel.ui.state

import com.emaxsoftware.popreel.core.data.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

data class ListState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = true,
    val error: DataError? = null,
) {
    val isEmptyResult: Boolean get() = !isLoading && error == null && items.isEmpty()

    companion object {
        fun <T> loaded(items: List<T>) = ListState(items, isLoading = false)
    }
}

/** Loading first, then each emission; a [DataError] becomes an error state instead of failing the flow. */
fun <T> Flow<List<T>>.asListState(): Flow<ListState<T>> =
    map { ListState.loaded(it) }
        .onStart { emit(ListState()) }
        .catch { e -> if (e is DataError) emit(ListState(isLoading = false, error = e)) else throw e }
