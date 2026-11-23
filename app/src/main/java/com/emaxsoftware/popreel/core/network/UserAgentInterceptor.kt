package com.emaxsoftware.popreel.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Wikimedia, which hosts ~250 catalog posters, answers OkHttp's default
 * User-Agent with 403 — every request identifies the app instead.
 */
class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("User-Agent", userAgent).build())
}
