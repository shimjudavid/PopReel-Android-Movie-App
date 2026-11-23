package com.emaxsoftware.popreel.feature.puzzle

import com.emaxsoftware.popreel.core.model.Movie

sealed interface PuzzleUiState {
    data object Loading : PuzzleUiState
    data object Unavailable : PuzzleUiState
    data class Playing(
        val movie: Movie,
        /** tiles[position] = the piece currently at that position; solved when tiles[i] == i. */
        val tiles: List<Int>,
        val selected: Int? = null,
        val moves: Int = 0,
        val elapsedSeconds: Int = 0,
    ) : PuzzleUiState {
        val placed: Int get() = tiles.withIndex().count { (position, piece) -> position == piece }
        val solved: Boolean get() = placed == tiles.size
    }
}

object PuzzleRules {
    const val GRID = 3
    const val TILE_COUNT = GRID * GRID
    private const val MAX_INITIALLY_PLACED = 2

    /** A shuffle that leaves at most two pieces already in place, so every puzzle needs real work. */
    fun shuffledTiles(random: kotlin.random.Random): List<Int> {
        while (true) {
            val tiles = (0 until TILE_COUNT).shuffled(random)
            if (tiles.withIndex().count { (i, piece) -> i == piece } <= MAX_INITIALLY_PLACED) return tiles
        }
    }

    fun swap(tiles: List<Int>, a: Int, b: Int): List<Int> =
        tiles.toMutableList().also { it[a] = tiles[b]; it[b] = tiles[a] }
}
