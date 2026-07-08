package com.opencode.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class Role { USER, ASSISTANT, SYSTEM }

@Serializable
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "New Session",
    val messages: List<Message> = emptyList(),
    val model: String = "claude-sonnet",
    val createdAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
)

@Serializable
data class ModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val color: Long = 0xFF65558F,
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
    ModelInfo("copilot", "GitHub Copilot", "GitHub", 0xFF8957E5),
)

enum class Screen { HOME, CHAT, FILES, TERMINAL, SETTINGS }
