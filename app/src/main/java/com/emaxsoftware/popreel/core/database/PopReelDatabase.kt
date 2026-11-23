package com.emaxsoftware.popreel.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emaxsoftware.popreel.core.database.dao.MovieChoiceDao
import com.emaxsoftware.popreel.core.database.dao.MovieDao
import com.emaxsoftware.popreel.core.database.dao.TriviaDao
import com.emaxsoftware.popreel.core.database.entity.GenreEntity
import com.emaxsoftware.popreel.core.database.entity.MovieChoiceEntity
import com.emaxsoftware.popreel.core.database.entity.MovieEntity
import com.emaxsoftware.popreel.core.database.entity.TriviaEntity

@Database(
    entities = [MovieEntity::class, GenreEntity::class, MovieChoiceEntity::class, TriviaEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(Converters::class)
abstract class PopReelDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun movieChoiceDao(): MovieChoiceDao
    abstract fun triviaDao(): TriviaDao
}
