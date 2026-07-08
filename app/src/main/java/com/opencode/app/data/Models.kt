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

data class Session(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "New Session",
    val messages: List<Message> = emptyList(),
    val model: String = "claude-sonnet",
    val createdAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
)

enum class Screen { HOME, CHAT, SETTINGS }
