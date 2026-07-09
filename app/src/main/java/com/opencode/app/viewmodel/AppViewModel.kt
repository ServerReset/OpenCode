package com.opencode.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.app.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AppState(
    val currentScreen: Screen = Screen.HOME,
    val isDarkMode: Boolean = false,
    val serverUrl: String = "http://10.0.2.2:4096",
    val password: String = "",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val sessions: List<Session> = emptyList(),
    val activeSessionId: String = "",
    val activeModel: String = "claude-sonnet",
    val showModelPicker: Boolean = false,
    val error: String? = null,
) {
    val activeSession: Session? get() = sessions.find { it.id == activeSessionId }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences(application)
    private val _state = MutableStateFlow(AppState(
        serverUrl = prefs.serverUrl, password = prefs.password,
        isDarkMode = prefs.isDarkMode, activeModel = prefs.activeModel,
    ))
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val api get() = OpenCodeClient.instance

    init { val u = _state.value.serverUrl; if (u.isNotBlank()) { api.configure(u, _state.value.password); connect() } }

    fun setScreen(s: Screen) { _state.update { it.copy(currentScreen = s) } }

    fun setServerUrl(url: String, pass: String) {
        prefs.serverUrl = url; prefs.password = pass
        api.configure(url, pass)
        _state.update { it.copy(serverUrl = url, password = pass, isConnected = false) }
        connect()
    }

    fun connect() {
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true, error = null) }
            api.health().fold(
                onSuccess = { _state.update { it.copy(isConnected = true, isConnecting = false) }; loadSessions() },
                onFailure = { _state.update { it.copy(isConnected = false, isConnecting = false, error = it.message) } },
            )
        }
    }

    private fun loadSessions() {
        viewModelScope.launch {
            api.listSessions().fold(
                onSuccess = { list ->
                    // Only keep sessions that have user messages (real conversations)
                    val withMessages = list.map { s -> s to api.getMessages(s.id).getOrNull() }
                    val filtered = withMessages.filter { (_, msgs) ->
                        msgs?.any { it.info.role == "user" } == true
                    }.map { (s, msgs) ->
                        val name = s.title ?: s.id.take(8)
                        val messages = msgs?.mapNotNull { m ->
                            val role = when (m.info.role) { "user" -> Role.USER; "assistant" -> Role.ASSISTANT; else -> null } ?: return@mapNotNull null
                            val text = m.parts.firstOrNull { it.type == "text" }?.text ?: ""
                            Message(id = m.info.id, role = role, content = text)
                        } ?: emptyList()
                        Session(id = s.id, name = name, messages = messages)
                    }.reversed()

                    _state.update { it.copy(sessions = filtered, activeSessionId = filtered.firstOrNull()?.id ?: "") }
                },
                onFailure = { _state.update { it.copy(error = "Failed to load sessions") } },
            )
        }
    }

    fun createSession() {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            api.createSession().fold(
                onSuccess = { s ->
                    val session = Session(id = s.id, name = s.title ?: s.id.take(8))
                    _state.update { it.copy(sessions = listOf(session) + it.sessions, activeSessionId = session.id, currentScreen = Screen.CHAT) }
                },
                onFailure = { e -> _state.update { it.copy(error = e.message) } },
            )
        }
    }

    fun switchToSession(id: String) {
        _state.update { it.copy(activeSessionId = id, currentScreen = Screen.CHAT) }
        // Refresh messages for this session
        viewModelScope.launch {
            api.getMessages(id).fold(
                onSuccess = { msgs ->
                    val messages = msgs.mapNotNull { m ->
                        val role = when (m.info.role) { "user" -> Role.USER; "assistant" -> Role.ASSISTANT; else -> null } ?: return@mapNotNull null
                        Message(id = m.info.id, role = role, content = m.parts.firstOrNull { it.type == "text" }?.text ?: "")
                    }
                    _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == id) it.copy(messages = messages) else it }) }
                },
                onFailure = { _state.update { it.copy(error = "Failed to load messages") } },
            )
        }
    }

    fun setActiveModel(m: String) { prefs.activeModel = m; _state.update { it.copy(activeModel = m, showModelPicker = false) } }
    fun toggleModelPicker() { _state.update { it.copy(showModelPicker = !it.showModelPicker) } }

    fun sendMessage(text: String) {
        val sid = _state.value.activeSessionId; if (sid.isBlank()) { _state.update { it.copy(error = "Create a session first") }; return }
        if (!_state.value.isConnected) { _state.update { it.copy(error = "Not connected") }; return }

        val userMsg = Message(role = Role.USER, content = text)
        _state.update { s -> s.copy(sessions = s.sessions.map { if (it.id == s.activeSessionId) it.copy(messages = it.messages + userMsg) else it }) }

        viewModelScope.launch {
            val aid = UUID.randomUUID().toString()
            _state.update { s -> s.copy(sessions = s.sessions.map { if (it.id == s.activeSessionId) it.copy(messages = it.messages + Message(id = aid, role = Role.ASSISTANT, content = "...")) else it }) }

            api.sendPrompt(sid, text).fold(
                onSuccess = {
                    // Poll for the response
                    var attempts = 0
                    while (attempts < 30) {
                        delay(2000)
                        api.getMessages(sid).fold(
                            onSuccess = { msgs ->
                                val text = msgs.firstOrNull { it.info.id == aid }?.parts?.firstOrNull { it.type == "text" }?.text
                                if (text != null && text.isNotBlank()) {
                                    _state.update { s -> s.copy(sessions = s.sessions.map { if (it.id == s.activeSessionId) it.copy(messages = it.messages.map { m -> if (m.id == aid) m.copy(content = text) else m }) else s }) }
                                    return@launch
                                }
                            },
                            onFailure = {},
                        )
                        attempts++
                    }
                },
                onFailure = { e -> _state.update { s -> s.copy(sessions = s.sessions.map { if (it.id == s.activeSessionId) it.copy(messages = it.messages.map { m -> if (m.id == aid) m.copy(content = "Error: ${e.message}") else m }) else s }) } },
            )
        }
    }

    fun toggleDarkMode() { val n = !_state.value.isDarkMode; prefs.isDarkMode = n; _state.update { it.copy(isDarkMode = n) } }
    fun clearError() { _state.update { it.copy(error = null) } }
}
