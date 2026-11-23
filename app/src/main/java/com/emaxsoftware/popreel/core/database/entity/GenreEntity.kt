package com.emaxsoftware.popreel.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey val name: String,
    /** Movies tagged with this genre; orders the filters most-stocked first. */
    @ColumnInfo(defaultValue = "0") val movieCount: Int = 0,
)
