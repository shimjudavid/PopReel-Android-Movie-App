package com.emaxsoftware.popreel.core.network

import com.emaxsoftware.popreel.core.network.dto.CatalogDto
import retrofit2.http.GET

interface PopReelApiService {
    /** The full catalog (~9.9k movies, ~2.3 MB compressed); synced into the local database. */
    @GET("catalog")
    suspend fun catalog(): CatalogDto
}
