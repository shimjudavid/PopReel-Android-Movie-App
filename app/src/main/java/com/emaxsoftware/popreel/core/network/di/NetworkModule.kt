package com.emaxsoftware.popreel.core.network.di

import com.emaxsoftware.popreel.BuildConfig
import com.emaxsoftware.popreel.core.network.PopReelApiService
import com.emaxsoftware.popreel.core.network.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    /** Shared with Coil so image and API requests use one pool and the same User-Agent. */
    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(
            UserAgentInterceptor("PopReel/${BuildConfig.VERSION_NAME} (Android; ${BuildConfig.APPLICATION_ID})"),
        )
        .addInterceptor(
            // BASIC, not BODY: the catalog response is ~10 MB of JSON.
            HttpLoggingInterceptor().setLevel(
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE,
            ),
        )
        .build()

    @Provides
    @Singleton
    fun apiService(client: OkHttpClient, json: Json): PopReelApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(PopReelApiService::class.java)
}
