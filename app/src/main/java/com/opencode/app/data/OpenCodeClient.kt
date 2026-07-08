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
data class HealthResponse(val healthy: Boolean)

@Serializable
data class ModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val color: Long = 0xFF65558F,
)

@Serializable
data class SessionInfo(
    val id: String,
    val name: String? = null,
)

@Serializable
data class SessionCreateRequest(
    val id: String? = null,
    val agent: String? = null,
    val model: String? = null,
)

@Serializable
data class SessionCreateResponse(val data: SessionInfo)

@Serializable
data class SessionListResponse(
    val data: List<SessionInfo>,
    val cursor: CursorInfo? = null,
)

@Serializable
data class CursorInfo(
    val previous: String? = null,
    val next: String? = null,
)

@Serializable
data class PromptRequest(
    val prompt: String,
    val id: String? = null,
    val delivery: String? = null,
    val resume: Boolean? = null,
)

@Serializable
data class AdmittedResponse(val data: AdmittedInfo)

@Serializable
data class AdmittedInfo(val id: String)

@Serializable
data class FileSystemEntry(
    val name: String,
    val path: String? = null,
    val isDirectory: Boolean = false,
    val children: List<FileSystemEntry> = emptyList(),
)

@Serializable
data class FsListResponse(val data: List<FsEntry>)

@Serializable
data class FsEntry(
    val name: String,
    val type: String? = null,
    val path: String? = null,
)

val availableModels = listOf(
    ModelInfo("claude-opus", "Claude Opus 4", "Anthropic", 0xFFD97706),
    ModelInfo("claude-sonnet", "Claude Sonnet 4", "Anthropic", 0xFFD97706),
    ModelInfo("gpt-4o", "GPT-4o", "OpenAI", 0xFF10A37F),
    ModelInfo("gpt-4o-mini", "GPT-4o Mini", "OpenAI", 0xFF10A37F),
    ModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", "Google", 0xFF4285F4),
    ModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", "Google", 0xFF4285F4),
    ModelInfo("deepseek-v4", "DeepSeek V4", "DeepSeek", 0xFF6B5CE7),
    ModelInfo("llama-4", "Llama 4 Maverick", "Meta", 0xFF0668E1),
    ModelInfo("codestral", "Codestral", "Mistral", 0xFFF59E0B),
)

class OpenCodeClient(private var baseUrl: String = "") {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    private val JSON_MEDIA = "application/json".toMediaType()

    var connected: Boolean = false
        private set
    var lastError: String? = null
        private set

    fun setUrl(url: String) {
        baseUrl = url.trimEnd('/')
        connected = false
        lastError = null
    }

    private fun Request.Builder.jsonHeaders() = apply {
        addHeader("Accept", "application/json")
        addHeader("Content-Type", "application/json")
    }

    private fun get(path: String): Request = Request.Builder().url("$baseUrl$path").jsonHeaders().get().build()
    private fun post(path: String, body: String): Request =
        Request.Builder().url("$baseUrl$path").jsonHeaders().post(body.toRequestBody(JSON_MEDIA)).build()

    suspend fun health(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = client.newCall(get("/api/health")).execute()
            if (res.isSuccessful) {
                connected = true; lastError = null; Result.success(Unit)
            } else {
                connected = false
                val msg = when (res.code) {
                    404 -> "Server not found. Check URL."
                    401, 403 -> "Authentication required. Set password in server URL."
                    else -> "Server error (${res.code})"
                }
                lastError = msg; Result.failure(Exception(msg))
            }
        } catch (e: java.net.ConnectException) {
            connected = false; lastError = "Connection refused"; Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            connected = false; lastError = "Unknown host"; Result.failure(e)
        } catch (e: Exception) {
            connected = false; lastError = e.message; Result.failure(e)
        }
    }

    suspend fun listModels(): Result<List<ModelInfo>> = withContext(Dispatchers.IO) {
        try {
            val res = client.newCall(get("/api/model")).execute()
            if (res.isSuccessful) Result.success(json.decodeFromString(res.body?.string() ?: "[]"))
            else Result.failure(Exception("HTTP ${res.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun listSessions(): Result<List<SessionInfo>> = withContext(Dispatchers.IO) {
        try {
            val res = client.newCall(get("/api/session")).execute()
            if (res.isSuccessful) {
                val body = res.body?.string() ?: "{\"data\":[]}"
                Result.success(json.decodeFromString<SessionListResponse>(body).data)
            } else Result.failure(Exception("HTTP ${res.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createSession(modelId: String?): Result<SessionInfo> = withContext(Dispatchers.IO) {
        try {
            val req = SessionCreateRequest(model = modelId)
            val body = json.encodeToString(SessionCreateRequest.serializer(), req)
            val res = client.newCall(post("/api/session", body)).execute()
            if (res.isSuccessful) {
                val resp = json.decodeFromString<SessionCreateResponse>(res.body?.string() ?: "{}")
                Result.success(resp.data)
            } else Result.failure(Exception("HTTP ${res.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendPrompt(sessionId: String, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = PromptRequest(prompt = prompt)
            val body = json.encodeToString(PromptRequest.serializer(), req)
            val res = client.newCall(post("/api/session/$sessionId/prompt", body)).execute()
            if (res.isSuccessful) {
                val resp = json.decodeFromString<AdmittedResponse>(res.body?.string() ?: "{}")
                Result.success(resp.data.id)
            } else {
                val errBody = res.body?.string()
                val msg = if (errBody?.contains("conflict") == true) "Session busy. Wait a moment."
                else "HTTP ${res.code}"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    /** Fetch session events (SSE-style). Returns raw event lines. */
    suspend fun getSessionEvents(sessionId: String, after: Int = 0): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = client.newCall(get("/api/session/$sessionId/event?after=$after")).execute()
            if (res.isSuccessful) Result.success(res.body?.string() ?: "")
            else Result.failure(Exception("HTTP ${res.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun listFs(path: String = ""): Result<List<FsEntry>> = withContext(Dispatchers.IO) {
        try {
            val url = if (path.isBlank()) "/api/fs/list" else "/api/fs/list?path=$path"
            val res = client.newCall(get(url)).execute()
            if (res.isSuccessful) {
                val body = res.body?.string() ?: "{\"data\":[]}"
                val resp = json.decodeFromString<FsListResponse>(body)
                Result.success(resp.data)
            } else Result.failure(Exception("HTTP ${res.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun readFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res = client.newCall(get("/api/fs/read/$path")).execute()
            if (res.isSuccessful) Result.success(res.body?.string() ?: "")
            else Result.failure(Exception("HTTP ${res.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    companion object {
        val instance = OpenCodeClient()
    }
}
