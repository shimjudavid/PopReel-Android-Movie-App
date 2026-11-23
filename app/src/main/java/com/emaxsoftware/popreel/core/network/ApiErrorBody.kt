package com.emaxsoftware.popreel.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Serializable
private data class ApiErrorBody(val error: String)

/** The server's `{"error": "…"}` message, if the error body has one. */
fun HttpException.serverMessage(json: Json): String? {
    val body = response()?.errorBody()?.string() ?: return null
    return try {
        json.decodeFromString<ApiErrorBody>(body).error
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
