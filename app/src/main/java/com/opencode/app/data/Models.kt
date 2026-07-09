package com.opencode.app.data

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
)

enum class Role { USER, ASSISTANT, SYSTEM }

data class Session(
    val id: String,
    val name: String,
    val messages: List<Message> = emptyList(),
    val model: String = "",
)

enum class Screen { HOME, CHAT, SETTINGS }
