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
        .build()
    private val JSON = "application/json".toMediaType()

    var connected: Boolean = false
        private set

    fun setUrl(url: String) {
        baseUrl = url.trimEnd('/')
        connected = false
    }

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/api/health").get().build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                connected = true
                Result.success(res.body?.string() ?: "connected")
            } else {
                connected = false
                Result.failure(Exception("HTTP ${res.code}: ${res.message}"))
            }
        } catch (e: Exception) {
            connected = false
            Result.failure(e)
        }
    }

    suspend fun chat(request: ChatRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = json.encodeToString(ChatRequest.serializer(), request)
                .toRequestBody(JSON)
            val req = Request.Builder()
                .url("$baseUrl/api/chat")
                .post(body)
                .build()
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
