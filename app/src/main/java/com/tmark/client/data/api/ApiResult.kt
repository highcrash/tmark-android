package com.tmark.client.data.api

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>()
}

inline fun <T> safeApiCall(call: () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(response.code(), "Empty response body")
            }
        } else {
            val rawError = response.errorBody()?.string() ?: "Unknown error"
            // Try to extract the "error" field from a JSON error body like {"error":"..."}
            val errorMsg = try {
                val json = org.json.JSONObject(rawError)
                json.optString("error").ifBlank { rawError }
            } catch (_: Exception) { rawError }
            ApiResult.Error(response.code(), errorMsg)
        }
    } catch (e: Throwable) {
        ApiResult.Exception(e)
    }
}
