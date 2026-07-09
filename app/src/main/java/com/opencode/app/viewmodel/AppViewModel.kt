package com.opencode.app.viewmodel

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
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

data class Todo(val id: String, val text: String, val done: Boolean = false)

data class AppState(
    val currentScreen: Screen = Screen.HOME,
    val isDarkMode: Boolean = false,
    val serverUrl: String = "http://10.0.2.2:4096",
    val password: String = "",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val sessions: List<Session> = emptyList(),
    val activeSessionId: String = "",
    val activeModel: String = "claude-sonnet",
    val showModelPicker: Boolean = false,
    val showTodos: Boolean = false,
    val todos: List<Todo> = emptyList(),
    val error: String? = null,
) {
    val activeSession: Session? get() = sessions.find { it.id == activeSessionId }
}

data class NavItem(val screen: Screen, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

val navItems = listOf(
    NavItem(Screen.HOME, "Home", Icons.Filled.Home, Icons.Filled.Home),
    NavItem(Screen.CHAT, "Chat", Icons.Filled.Chat, Icons.Filled.Chat),
    NavItem(Screen.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Filled.Settings),
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences(application)
    private val _state = MutableStateFlow(AppState(
        serverUrl = prefs.serverUrl, password = prefs.password,
        isDarkMode = prefs.isDarkMode, activeModel = prefs.activeModel,
    ))
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val api get() = OpenCodeClient.instance

    init { if (_state.value.serverUrl.isNotBlank()) { api.configure(_state.value.serverUrl, _state.value.password); connect() } }

    fun setScreen(s: Screen) { _state.update { it.copy(currentScreen = s) } }

    fun setServerUrl(url: String, pass: String) {
        prefs.serverUrl = url; prefs.password = pass
        _state.update { it.copy(serverUrl = url, password = pass, isConnected = false, connectionError = null) }
        api.configure(url, pass); connect()
    }

    fun connect() {
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true, connectionError = null, error = null) }
            api.health().fold(
                onSuccess = { _state.update { it.copy(isConnected = true, isConnecting = false) }; loadSessions() },
                onFailure = { e -> _state.update { it.copy(isConnected = false, isConnecting = false, connectionError = e.message) } },
            )
        }
    }

    private suspend fun loadSessions() {
        if (!_state.value.isConnected) return
        api.listSessions().fold(
            onSuccess = { list ->
                val existing = _state.value.sessions
                // Don't filter by messages - show all sessions
                // The server may not support per-session message fetching
                val sessions = list.map { s ->
                    existing.find { it.id == s.id } ?: Session(id = s.id, name = s.title ?: s.id.take(8), model = _state.value.activeModel)
                }.reversed()
                val activeId = sessions.firstOrNull()?.id ?: ""
                _state.update { it.copy(sessions = sessions, activeSessionId = activeId) }
                // Try to fetch messages for the active session in background
                if (activeId.isNotBlank()) fetchSessionMessages(activeId)
            },
            onFailure = { _state.update { it.copy(error = "Failed to load sessions") } },
        )
    }

    private fun fetchSessionMessages(sessionId: String) {
        viewModelScope.launch {
            api.getMessages(sessionId).fold(
                onSuccess = { msgs ->
                    val messages = msgs.mapNotNull { m ->
                        val role = when (m.info.role) { "user" -> Role.USER; "assistant" -> Role.ASSISTANT; else -> return@mapNotNull null }
                        val text = m.parts.firstOrNull { it.type == "text" }?.text ?: ""
                        Message(id = m.info.id, role = role, content = text)
                    }
                    _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == sessionId) it.copy(messages = messages) else it }) }
                },
                onFailure = { /* server may not support message listing */ },
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
                onFailure = { e -> _state.update { it.copy(error = e.message ?: "Create failed") } },
            )
        }
    }

    fun switchToSession(id: String) {
        _state.update { it.copy(activeSessionId = id, currentScreen = Screen.CHAT) }
        // Fetch messages for this session
        viewModelScope.launch {
            api.getMessages(id).fold(
                onSuccess = { msgs ->
                    val messages = msgs.mapNotNull { m ->
                        val role = when (m.info.role) { "user" -> Role.USER; "assistant" -> Role.ASSISTANT; else -> null } ?: return@mapNotNull null
                        val text = m.parts.firstOrNull { it.type == "text" }?.text ?: ""
                        Message(id = m.info.id, role = role, content = text)
                    }
                    _state.update { state ->
                        state.copy(sessions = state.sessions.map { if (it.id == id) it.copy(messages = messages) else it })
                    }
                },
                onFailure = { /* keep empty messages */ },
            )
        }
    }

    fun setActiveModel(m: String) {
        prefs.activeModel = m
        _state.update { it.copy(activeModel = m, showModelPicker = false) }
    }

    fun toggleModelPicker() { _state.update { it.copy(showModelPicker = !it.showModelPicker) } }

    fun sendMessage(content: String) {
        val s = _state.value; val sessionId = s.activeSessionId
        if (sessionId.isBlank()) { _state.update { it.copy(error = "Create a session first") }; return }
        if (!s.isConnected) { _state.update { it.copy(error = "Not connected") }; return }

        val userMsg = Message(role = Role.USER, content = content)
        _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = it.messages + userMsg) else it }) }

        viewModelScope.launch {
            val assistantId = UUID.randomUUID().toString()
            _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = it.messages + Message(id = assistantId, role = Role.ASSISTANT, content = "")) else it }) }

            api.sendPrompt(sessionId, s.activeModel, content).fold(
                onSuccess = { pollForMessages(sessionId, assistantId) },
                onFailure = { e ->
                    val msg = e.message ?: "Send failed"
                    _state.update { state -> state.copy(sessions = state.sessions.map { s -> if (s.id == state.activeSessionId) s.copy(messages = s.messages.map { m -> if (m.id == assistantId) m.copy(content = msg) else m }) else s }) }
                },
            )
        }
    }

    private fun pollForMessages(sessionId: String, assistantId: String) {
        viewModelScope.launch {
            var attempts = 0
            while (attempts < 60) {
                delay(1000)
                val r = api.getMessages(sessionId)
                if (r.isSuccess) {
                    val msgs = r.getOrThrow()
                    val text = msgs.firstOrNull { it.info.id == assistantId }?.parts?.firstOrNull { it.type == "text" }?.text
                    if (text != null) {
                        _state.update { state -> state.copy(sessions = state.sessions.map { s -> if (s.id == state.activeSessionId) s.copy(messages = s.messages.map { m -> if (m.id == assistantId) m.copy(content = text) else m }) else s }) }
                        if (text.isNotEmpty()) return@launch
                    }
                } else {
                    _state.update { it.copy(error = "Poll error") }
                }
                attempts++
            }
            _state.update { it.copy(error = "Response timeout") }
        }
    }

    fun toggleDarkMode() { val n = !_state.value.isDarkMode; prefs.isDarkMode = n; _state.update { it.copy(isDarkMode = n) } }
    fun toggleTodos() { _state.update { it.copy(showTodos = !it.showTodos) } }
    fun addTodo(text: String) { _state.update { it.copy(todos = it.todos + Todo(id = UUID.randomUUID().toString(), text = text), showTodos = true) } }
    fun toggleTodoDone(id: String) { _state.update { state -> state.copy(todos = state.todos.map { if (it.id == id) it.copy(done = !it.done) else it }) } }
    fun removeTodo(id: String) { _state.update { state -> state.copy(todos = state.todos.filter { it.id != id }) } }
    fun clearSession() { _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = emptyList()) else it }) } }
    fun clearError() { _state.update { it.copy(error = null) } }
}
