package com.emaxsoftware.popreel

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class PopReelApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var okHttpClient: Lazy<OkHttpClient>

    /** Coil shares the app's OkHttp client: one connection pool and the app User-Agent Wikimedia requires. */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient.get() })) }
            .crossfade(true)
            .build()
}
