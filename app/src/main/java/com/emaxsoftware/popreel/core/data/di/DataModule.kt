package com.emaxsoftware.popreel.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.emaxsoftware.popreel.core.audio.GameSounds
import com.emaxsoftware.popreel.core.audio.ToneGameSounds
import com.emaxsoftware.popreel.core.data.CatalogRepository
import com.emaxsoftware.popreel.core.data.DataStoreSettingsRepository
import com.emaxsoftware.popreel.core.data.MovieChoiceRepository
import com.emaxsoftware.popreel.core.data.OfflineFirstCatalogRepository
import com.emaxsoftware.popreel.core.data.RoomMovieChoiceRepository
import com.emaxsoftware.popreel.core.data.RoomTriviaRepository
import com.emaxsoftware.popreel.core.data.SettingsRepository
import com.emaxsoftware.popreel.core.data.TriviaRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun catalogRepository(impl: OfflineFirstCatalogRepository): CatalogRepository

    @Binds
    @Singleton
    fun movieChoiceRepository(impl: RoomMovieChoiceRepository): MovieChoiceRepository

    @Binds
    fun triviaRepository(impl: RoomTriviaRepository): TriviaRepository

    @Binds
    @Singleton
    fun settingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    fun gameSounds(impl: ToneGameSounds): GameSounds

    companion object {
        /** One instance per file for the whole app — a second instance on the same file throws. */
        @Provides
        @Singleton
        fun preferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { context.preferencesDataStoreFile("popreel_settings") },
            )
    }
}
