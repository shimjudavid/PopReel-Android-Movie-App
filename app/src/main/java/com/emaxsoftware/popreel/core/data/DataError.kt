package com.emaxsoftware.popreel.core.data

sealed class DataError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable) : DataError("Network error", cause)
    class Server(val code: Int, serverMessage: String?) : DataError("Server error $code: $serverMessage")
    class MalformedResponse(cause: Throwable) : DataError("Malformed response", cause)
    class Local(cause: Throwable) : DataError("Local storage error", cause)
}
