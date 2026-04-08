package com.example.androidplayground.ui.apidashboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Encapsulates the result of an API call.
 */
data class ApiResponse(
    val statusCode: Int,
    val body: String,
    val responseTimeMs: Long,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

enum class HttpMethod { GET, POST }

/**
 * Lightweight OkHttp wrapper for making arbitrary API requests.
 * All calls are dispatched on [Dispatchers.IO].
 */
object ApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Executes an HTTP request and returns a structured [ApiResponse].
     *
     * @param url       The full URL string (must include scheme)
     * @param method    GET or POST
     * @param headers   Optional key-value pairs appended as request headers
     * @param postBody  Optional body string sent with POST requests (defaults to empty JSON object)
     */
    suspend fun execute(
        url: String,
        method: HttpMethod = HttpMethod.GET,
        headers: Map<String, String> = emptyMap(),
        postBody: String = ""
    ): ApiResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            val requestBuilder = Request.Builder()
                .url(url)
                .headers(headers.toHeaders())

            when (method) {
                HttpMethod.GET -> requestBuilder.get()
                HttpMethod.POST -> {
                    val body = postBody.ifBlank { "{}" }
                    requestBuilder.post(body.toRequestBody("application/json".toMediaType()))
                }
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val elapsed = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string() ?: ""

            ApiResponse(
                statusCode = response.code,
                body = responseBody,
                responseTimeMs = elapsed,
                isSuccess = response.isSuccessful
            )
        } catch (e: IllegalArgumentException) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 0,
                body = "",
                responseTimeMs = elapsed,
                isSuccess = false,
                errorMessage = "Invalid URL: ${e.message}"
            )
        } catch (e: java.net.SocketTimeoutException) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 0,
                body = "",
                responseTimeMs = elapsed,
                isSuccess = false,
                errorMessage = "Request timed out after ${elapsed}ms"
            )
        } catch (e: java.net.UnknownHostException) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 0,
                body = "",
                responseTimeMs = elapsed,
                isSuccess = false,
                errorMessage = "Cannot resolve host: ${e.message}"
            )
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 0,
                body = "",
                responseTimeMs = elapsed,
                isSuccess = false,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
}
