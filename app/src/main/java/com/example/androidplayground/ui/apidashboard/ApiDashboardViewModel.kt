package com.example.androidplayground.ui.apidashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

data class HeaderEntry(val key: String = "", val value: String = "")

data class ApiDashboardState(
    val url: String = "https://jsonplaceholder.typicode.com/todos/1",
    val method: HttpMethod = HttpMethod.GET,
    val postBody: String = "",
    val headers: List<HeaderEntry> = emptyList(),
    val showHeaders: Boolean = false,

    val isLoading: Boolean = false,
    val responseBody: String = "",
    val statusCode: Int? = null,
    val responseTimeMs: Long? = null,
    val errorMessage: String? = null,
    val isJsonResponse: Boolean = false
)

class ApiDashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(ApiDashboardState())
    val state: StateFlow<ApiDashboardState> = _state.asStateFlow()

    fun updateUrl(url: String) {
        _state.value = _state.value.copy(url = url)
    }

    fun updateMethod(method: HttpMethod) {
        _state.value = _state.value.copy(method = method)
    }

    fun updatePostBody(body: String) {
        _state.value = _state.value.copy(postBody = body)
    }

    fun toggleHeaders() {
        _state.value = _state.value.copy(showHeaders = !_state.value.showHeaders)
    }

    fun addHeader() {
        _state.value = _state.value.copy(
            headers = _state.value.headers + HeaderEntry()
        )
    }

    fun updateHeader(index: Int, key: String, value: String) {
        val updated = _state.value.headers.toMutableList()
        if (index in updated.indices) {
            updated[index] = HeaderEntry(key, value)
            _state.value = _state.value.copy(headers = updated)
        }
    }

    fun removeHeader(index: Int) {
        val updated = _state.value.headers.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _state.value = _state.value.copy(headers = updated)
        }
    }

    fun sendRequest() {
        val s = _state.value
        val url = s.url.trim()

        if (url.isBlank()) {
            _state.value = s.copy(errorMessage = "URL cannot be empty")
            return
        }

        // Basic URL validation
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _state.value = s.copy(errorMessage = "URL must start with http:// or https://")
            return
        }

        _state.value = s.copy(
            isLoading = true,
            errorMessage = null,
            responseBody = "",
            statusCode = null,
            responseTimeMs = null,
            isJsonResponse = false
        )

        val headerMap = s.headers
            .filter { it.key.isNotBlank() }
            .associate { it.key to it.value }

        viewModelScope.launch {
            val response = ApiClient.execute(
                url = url,
                method = s.method,
                headers = headerMap,
                postBody = s.postBody
            )

            if (response.errorMessage != null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = response.errorMessage,
                    statusCode = if (response.statusCode != 0) response.statusCode else null,
                    responseTimeMs = response.responseTimeMs
                )
                return@launch
            }

            // Try to pretty-print as JSON
            val (formatted, isJson) = tryFormatJson(response.body)

            _state.value = _state.value.copy(
                isLoading = false,
                responseBody = formatted,
                statusCode = response.statusCode,
                responseTimeMs = response.responseTimeMs,
                isJsonResponse = isJson,
                errorMessage = if (!response.isSuccess) "HTTP ${response.statusCode}" else null
            )
        }
    }

    private fun tryFormatJson(raw: String): Pair<String, Boolean> {
        if (raw.isBlank()) return "" to false
        return try {
            val trimmed = raw.trim()
            when (JSONTokener(trimmed).nextValue()) {
                is JSONObject -> JSONObject(trimmed).toString(2) to true
                is JSONArray -> JSONArray(trimmed).toString(2) to true
                else -> raw to false
            }
        } catch (_: Exception) {
            raw to false
        }
    }
}
