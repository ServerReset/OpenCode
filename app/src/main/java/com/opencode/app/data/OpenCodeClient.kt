package com.opencode.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Base64
import java.util.concurrent.TimeUnit

@Serializable
data class HealthResponse(val healthy: Boolean, val version: String? = null)

@Serializable
data class SessionData(
    val id: String,
    val title: String? = null,
    val directory: String? = null,
    val time: SessionTime? = null,
    val agent: String? = null,
)

@Serializable
data class SessionTime(val created: Long = 0, val updated: Long = 0)

@Serializable
data class CreateSessionPayload(val title: String? = null)

@Serializable
data class SessionListResponse(val data: List<SessionData>)

@Serializable
data class SessionResponse(val data: SessionData)

@Serializable
data class MessageData(
    val info: MessageInfo,
    val parts: List<MessagePart> = emptyList(),
)

@Serializable
data class MessageInfo(val id: String, val role: String, val sessionID: String? = null)

@Serializable
data class MessagePart(
    val type: String,
    val text: String? = null,
    val tool: String? = null,
    val state: ToolState? = null,
    val url: String? = null,
    val id: String? = null,
)

@Serializable
data class ToolState(val status: String = "", val input: String? = null, val output: String? = null)

@Serializable
data class PromptPayload(
    val parts: List<PromptPart>,
    val agent: String? = null,
    val model: ModelRef? = null,
)

@Serializable
data class PromptPart(val type: String = "text", val text: String? = null)

@Serializable
data class ModelRef(val providerID: String, val modelID: String)

data class ModelInfo(val id: String, val name: String, val provider: String, val color: Long = 0xFF65558F)

val availableModels = listOf(
    ModelInfo("claude-sonnet", "Claude Sonnet 4", "Anthropic", 0xFFD97706),
    ModelInfo("gpt-4o", "GPT-4o", "OpenAI", 0xFF10A37F),
    ModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", "Google", 0xFF4285F4),
    ModelInfo("deepseek-v4", "DeepSeek V4", "DeepSeek", 0xFF6B5CE7),
    ModelInfo("llama-4", "Llama 4 Maverick", "Meta", 0xFF0668E1),
)

class OpenCodeClient(private var baseUrl: String = "", private var password: String = "") {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true).followSslRedirects(true).build()
    private val JSON = "application/json".toMediaType()

    var connected: Boolean = false; private set
    var lastError: String? = null; private set

    fun configure(url: String, pass: String = "") {
        baseUrl = url.trimEnd('/')
        password = pass
        connected = false; lastError = null
    }

    private val authHeader: String? by lazy {
        if (password.isNotBlank()) "Basic ${Base64.getEncoder().encodeToString("opencode:$password".toByteArray())}" else null
    }

    private fun Request.Builder.auth() = apply { authHeader?.let { header("Authorization", it) } }

    private fun req(path: String, method: String = "GET", body: String? = null): Request {
        val b = body?.toRequestBody(JSON)
        return Request.Builder().url("$baseUrl$path").auth().method(method, b).addHeader("Accept", "application/json").build()
    }

    private fun parseError(res: okhttp3.Response): String = when {
        res.code == 401 -> "Unauthorized. Check password."
        res.code == 403 -> "Access denied."
        res.code == 404 -> "Endpoint not found. Check URL."
        res.code >= 500 -> "Server error (${res.code})"
        else -> "HTTP ${res.code}"
    }

    suspend fun health(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val r = client.newCall(req("/api/health")).execute()
            if (r.isSuccessful) { connected = true; lastError = null; Result.success(Unit) }
            else { connected = false; val m = parseError(r); lastError = m; Result.failure(Exception(m)) }
        } catch (e: Exception) { connected = false; lastError = e.message; Result.failure(e) }
    }

    suspend fun listSessions(): Result<List<SessionData>> = withContext(Dispatchers.IO) {
        try {
            val r = client.newCall(req("/api/session")).execute()
            if (r.isSuccessful) {
                val body = r.body?.string() ?: "{\"data\":[]}"
                Result.success(json.decodeFromString<SessionListResponse>(body).data)
            } else Result.failure(Exception(parseError(r)))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createSession(title: String? = null): Result<SessionData> = withContext(Dispatchers.IO) {
        try {
            val payload = if (title != null) json.encodeToString(CreateSessionPayload.serializer(), CreateSessionPayload(title)) else "{}"
            val r = client.newCall(req("/api/session", "POST", payload)).execute()
            if (r.isSuccessful) Result.success(json.decodeFromString<SessionResponse>(r.body?.string() ?: "{}").data)
            else Result.failure(Exception(parseError(r)))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getMessages(sessionId: String): Result<List<MessageData>> = withContext(Dispatchers.IO) {
        try {
            val r = client.newCall(req("/api/session/$sessionId/message")).execute()
            if (r.isSuccessful) {
                val body = r.body?.string() ?: "{\"data\":[]}"
                val resp = json.decodeFromString<SessionMessagesResponse>(body)
                Result.success(resp.data)
            } else Result.failure(Exception(parseError(r)))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendPrompt(sessionId: String, modelId: String, text: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = PromptPayload(parts = listOf(PromptPart(text = text)), model = ModelRef(providerID = modelId, modelID = modelId))
            val body = json.encodeToString(PromptPayload.serializer(), payload)
            val r = client.newCall(req("/api/session/$sessionId/prompt", "POST", body)).execute()
            if (r.isSuccessful) Result.success(Unit)
            else {
                val errBody = r.body?.string()
                val msg = if (errBody?.contains("conflict") == true) "Session is busy" else parseError(r)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    companion object {
        val instance = OpenCodeClient()
    }
}

@Serializable
data class SessionMessagesResponse(val data: List<MessageData> = emptyList())
