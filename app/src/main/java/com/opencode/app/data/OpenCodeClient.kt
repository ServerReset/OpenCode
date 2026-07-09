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
data class ServerSession(val id: String, val title: String? = null, val agent: String? = null)
@Serializable
data class SessionListResp(val data: List<ServerSession>)
@Serializable
data class SessionResp(val data: ServerSession)
@Serializable
data class ServerMsg(val info: ServerMsgInfo, val parts: List<ServerMsgPart> = emptyList())
@Serializable
data class ServerMsgInfo(val id: String, val role: String)
@Serializable
data class ServerMsgPart(val type: String, val text: String? = null)
@Serializable
data class MsgListResp(val data: List<ServerMsg>)
@Serializable
data class PromptReq(val parts: List<PromptPart>)
@Serializable
data class PromptPart(val type: String = "text", val text: String? = null)
@Serializable
data class AccountInfo(val email: String = "", val plan: String = "Free", val monthlyLimit: Int = 0, val monthlyUsed: Int = 0, val weeklyLimit: Int = 0, val weeklyUsed: Int = 0)

class OpenCodeClient {
    private var baseUrl = ""
    private var password = ""
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).build()
    private val JSON = "application/json".toMediaType()

    var connected = false; private set
    var lastError: String? = null; private set

    fun configure(url: String, pass: String = "") {
        baseUrl = url.trimEnd('/'); password = pass; connected = false; lastError = null
    }

    private fun auth() = if (password.isNotBlank()) "Basic ${Base64.getEncoder().encodeToString("opencode:$password".toByteArray())}" else null

    private fun req(path: String, method: String = "GET", body: String? = null): Request {
        val r = Request.Builder().url("$baseUrl$path").addHeader("Accept", "application/json")
        auth()?.let { r.addHeader("Authorization", it) }
        body?.let { r.method(method, it.toRequestBody(JSON)) } ?: r.method(method, null)
        return r.build()
    }

    suspend fun health(): Result<String> = withContext(Dispatchers.IO) {
        try { val r = client.newCall(req("/api/health")).execute()
            if (r.isSuccessful) Result.success("ok")
            else { val m = "HTTP ${r.code}"; lastError = m; Result.failure(Exception(m)) }
        } catch (e: Exception) { lastError = e.message; Result.failure(e) }
    }

    suspend fun listSessions(): Result<List<ServerSession>> = withContext(Dispatchers.IO) {
        try { val r = client.newCall(req("/api/session?limit=200")).execute()
            if (r.isSuccessful) Result.success(json.decodeFromString<SessionListResp>(r.body?.string() ?: "{\"data\":[]}").data)
            else {
                // Try /experimental/session as fallback (reference app approach)
                val r2 = client.newCall(req("/experimental/session?limit=200")).execute()
                if (r2.isSuccessful) Result.success(json.decodeFromString<SessionListResp>(r2.body?.string() ?: "{\"data\":[]}").data)
                else Result.failure(Exception("HTTP ${r.code}/${r2.code}"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createSession(): Result<ServerSession> = withContext(Dispatchers.IO) {
        try { val r = client.newCall(req("/api/session", "POST", "{}")).execute()
            if (r.isSuccessful) Result.success(json.decodeFromString<SessionResp>(r.body?.string() ?: "{}").data)
            else Result.failure(Exception("Create: HTTP ${r.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getMessages(sessionId: String): Result<List<ServerMsg>> = withContext(Dispatchers.IO) {
        try {
            val r = client.newCall(req("/session/$sessionId/message")).execute()
            val rawBody = r.body?.string() ?: ""
            if (r.isSuccessful) {
                // Server returns either {"data": [...]} or just [...]
                val msgs = try {
                    json.decodeFromString<MsgListResp>(rawBody).data
                } catch (_: Exception) {
                    try { json.decodeFromString<List<ServerMsg>>(rawBody) } catch (_: Exception) { emptyList() }
                }
                Result.success(msgs)
            } else {
                val r2 = client.newCall(req("/api/session/$sessionId/message")).execute()
                val rawBody2 = r2.body?.string() ?: ""
                if (r2.isSuccessful) {
                    val msgs = try {
                        json.decodeFromString<MsgListResp>(rawBody2).data
                    } catch (_: Exception) {
                        try { json.decodeFromString<List<ServerMsg>>(rawBody2) } catch (_: Exception) { emptyList() }
                    }
                    Result.success(msgs)
                }
                else Result.failure(Exception("HTTP ${r.code}/${r2.code}: ${(rawBody + rawBody2).take(200)}"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendPrompt(sessionId: String, text: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = json.encodeToString(PromptReq.serializer(), PromptReq(parts = listOf(PromptPart(text = text))))
            // Reference app approach: POST /session/:id/message
            val r = client.newCall(req("/session/$sessionId/message", "POST", body)).execute()
            if (r.isSuccessful) Result.success(Unit)
            else {
                // Fallback: POST /api/session/:id/prompt
                val r2 = client.newCall(req("/api/session/$sessionId/prompt", "POST", body)).execute()
                if (r2.isSuccessful) Result.success(Unit)
                else {
                    val errBody = r2.body?.string() ?: r.body?.string() ?: ""
                    val msg = if (errBody.contains("conflict")) "Session busy" else "HTTP ${r.code}/${r2.code}"
                    Result.failure(Exception(msg))
                }
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    /** Fetch account info from the opencode cloud API */
    suspend fun fetchAccount(apiKey: String): Result<AccountInfo> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(Exception("No API key"))
        // Try multiple opencode.ai endpoints
        val endpoints = listOf(
            "https://api.opencode.ai/v1/account",
            "https://api.opencode.ai/v1/user",
            "https://api.opencode.ai/v1/zen",
            "https://api.opencode.ai/account",
            "https://api.opencode.ai/user",
        )
        for (url in endpoints) {
            try {
                val r = client.newCall(Request.Builder().url(url).addHeader("Authorization", "Bearer $apiKey").addHeader("Accept", "application/json").get().build()).execute()
                if (r.isSuccessful) {
                    val body = r.body?.string() ?: "{}"
                    val info = try { json.decodeFromString<AccountInfo>(body) } catch (_: Exception) { AccountInfo() }
                    return@withContext Result.success(info)
                }
            } catch (_: Exception) { continue }
        }
        Result.failure(Exception("Could not fetch account from any opencode.ai endpoint"))
    }

    companion object { val instance = OpenCodeClient() }
}
