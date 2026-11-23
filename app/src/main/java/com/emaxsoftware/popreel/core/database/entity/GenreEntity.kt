package com.emaxsoftware.popreel.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
data class GenreEntity(@PrimaryKey val name: String)
