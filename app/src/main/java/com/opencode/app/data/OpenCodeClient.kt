package com.opencode.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class ChatRequest(
    val message: String,
    val sessionId: String? = null,
    val model: String = "claude-sonnet",
)

@Serializable
data class ChatResponse(
    val message: String,
    val sessionId: String? = null,
    val done: Boolean = true,
)

@Serializable
data class FileTreeEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean = false,
    val children: List<FileTreeEntry> = emptyList(),
)

@Serializable
data class TerminalResult(
    val output: String,
    val error: String? = null,
)

class OpenCodeClient(private var baseUrl: String = "") {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    private val JSON = "application/json".toMediaType()

    var connected: Boolean = false
        private set
    var lastError: String? = null
        private set

    fun setUrl(url: String) {
        baseUrl = url.trimEnd('/')
        connected = false
        lastError = null
    }

    private fun Request.Builder.addJsonHeaders() = apply {
        addHeader("Accept", "application/json")
        addHeader("Content-Type", "application/json")
        addHeader("User-Agent", "OpenCode-Android/1.0")
    }

    private fun parseError(res: okhttp3.Response): String {
        return try {
            val body = res.body?.string()
            if (body.isNullOrBlank()) "Server error (HTTP ${res.code})"
            else if (res.code == 404) "Endpoint not found. Check your server URL."
            else if (res.code >= 500) "Server error (${res.code}). Is opencode web running?"
            else body
        } catch (e: Exception) {
            "HTTP ${res.code}"
        }
    }

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/api/health")
                .addJsonHeaders().get().build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                connected = true
                lastError = null
                Result.success(res.body?.string() ?: "connected")
            } else {
                connected = false
                val msg = parseError(res)
                lastError = msg
                Result.failure(Exception(msg))
            }
        } catch (e: java.net.UnknownHostException) {
            connected = false
            lastError = "Cannot reach host. Check IP/URL."
            Result.failure(lastError?.let { Exception(it) } ?: e)
        } catch (e: java.net.ConnectException) {
            connected = false
            lastError = "Connection refused. Is the server running?"
            Result.failure(lastError?.let { Exception(it) } ?: e)
        } catch (e: javax.net.ssl.SSLException) {
            connected = false
            lastError = "SSL error. Try http:// instead of https://"
            Result.failure(lastError?.let { Exception(it) } ?: e)
        } catch (e: java.net.SocketTimeoutException) {
            connected = false
            lastError = "Connection timed out. Check IP/port."
            Result.failure(lastError?.let { Exception(it) } ?: e)
        } catch (e: Exception) {
            connected = false
            val msg = e.message ?: "Unknown error"
            lastError = when {
                msg.contains("Cleartext") -> "HTTP blocked by Android. App fix applied — rebuild."
                msg.contains("Unable to resolve host") -> "Cannot resolve hostname. Use IP address."
                msg.contains("Failed to connect") -> "Connection failed. Is the server on?"
                else -> msg
            }
            Result.failure(lastError?.let { Exception(it) } ?: e)
        }
    }

    suspend fun chat(request: ChatRequest): Result<String> = withContext(Dispatchers.IO) {
        if (!connected) return@withContext Result.failure(Exception("Not connected"))
        try {
            val body = json.encodeToString(ChatRequest.serializer(), request)
                .toRequestBody(JSON)
            val req = Request.Builder()
                .url("$baseUrl/api/chat")
                .addJsonHeaders()
                .post(body)
                .build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                Result.success(res.body?.string() ?: "")
            } else {
                val msg = if (res.code == 404) "Chat endpoint not found" else "HTTP ${res.code}"
                lastError = msg
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            lastError = e.message
            Result.failure(e)
        }
    }

    suspend fun getFileTree(): Result<List<FileTreeEntry>> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/api/files").get().build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                val body = res.body?.string() ?: "[]"
                val tree = json.decodeFromString<List<FileTreeEntry>>(body)
                Result.success(tree)
            } else {
                Result.failure(Exception("HTTP ${res.code}: ${res.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileContent(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/api/files?path=$path").get().build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                Result.success(res.body?.string() ?: "")
            } else {
                Result.failure(Exception("HTTP ${res.code}: ${res.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeCommand(command: String): Result<TerminalResult> = withContext(Dispatchers.IO) {
        try {
            val body = json.encodeToString(TerminalResult.serializer(), TerminalResult(command, null))
                .toRequestBody(JSON)
            val req = Request.Builder()
                .url("$baseUrl/api/terminal")
                .post(body)
                .build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                val result = json.decodeFromString<TerminalResult>(res.body?.string() ?: "{}")
                Result.success(result)
            } else {
                Result.failure(Exception("HTTP ${res.code}: ${res.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        val instance = OpenCodeClient()
    }
}
