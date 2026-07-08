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

data class AppState(
    val currentScreen: Screen = Screen.HOME,
    val isDarkMode: Boolean = false,
    val serverUrl: String = "http://10.0.2.2:4096",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val sessions: List<Session> = emptyList(),
    val activeSessionId: String = "",
    val activeModel: String = "claude-sonnet",
    val showModelPicker: Boolean = false,
    val error: String? = null,
) {
    val activeSession: Session?
        get() = sessions.find { it.id == activeSessionId }
}

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

val navItems = listOf(
    NavItem(Screen.HOME, "Home", Icons.Filled.Home, Icons.Filled.Home),
    NavItem(Screen.CHAT, "Chat", Icons.Filled.Chat, Icons.Filled.Chat),
    NavItem(Screen.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Filled.Settings),
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences(application)
    private val _state = MutableStateFlow(
        AppState(
            serverUrl = prefs.serverUrl,
            isDarkMode = prefs.isDarkMode,
        )
    )
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val api get() = OpenCodeClient.instance
    private var eventPollJob: kotlinx.coroutines.Job? = null
    private var lastEventSeq = 0

    init {
        val url = _state.value.serverUrl
        if (url.isNotBlank()) { api.setUrl(url); connect() }
    }

    fun setScreen(screen: Screen) { _state.update { it.copy(currentScreen = screen) } }

    fun setServerUrl(url: String) {
        prefs.serverUrl = url
        _state.update { it.copy(serverUrl = url, isConnected = false, connectionError = null) }
        api.setUrl(url)
        connect()
    }

    fun connect() {
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true, connectionError = null, error = null) }
            api.health().fold(
                onSuccess = {
                    _state.update { it.copy(isConnected = true, isConnecting = false) }
                    loadSessions()
                },
                onFailure = { err ->
                    _state.update { it.copy(isConnected = false, isConnecting = false,
                        connectionError = err.message ?: "Connection failed") }
                },
            )
        }
    }

    private suspend fun loadSessions() {
        if (!_state.value.isConnected) return
        api.listSessions().fold(
            onSuccess = { serverSessions ->
                val existing = _state.value.sessions
                val local = serverSessions.map { s ->
                    existing.find { it.id == s.id } ?: Session(id = s.id, name = s.name ?: "Session")
                }
                val active = if (_state.value.activeSessionId.isBlank()) local.firstOrNull()?.id ?: ""
                else _state.value.activeSessionId
                _state.update { it.copy(sessions = local, activeSessionId = active) }
            },
            onFailure = { /* silently ignore */ },
        )
    }

    fun createSession() {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            api.createSession(_state.value.activeModel).fold(
                onSuccess = { info ->
                    val session = Session(id = info.id, name = info.name ?: "Session", model = _state.value.activeModel)
                    _state.update { it.copy(sessions = it.sessions + session, activeSessionId = session.id, currentScreen = Screen.CHAT) }
                },
                onFailure = { _state.update { it.copy(error = "Failed to create session: ${it.message}") } },
            )
        }
    }

    fun switchToSession(id: String) {
        _state.update { it.copy(activeSessionId = id, currentScreen = Screen.CHAT) }
        stopPolling()
    }

    fun setActiveModel(model: String) {
        _state.update { it.copy(activeModel = model, showModelPicker = false) }
    }

    fun toggleModelPicker() { _state.update { it.copy(showModelPicker = !it.showModelPicker) } }

    fun sendMessage(content: String) {
        val s = _state.value
        val sessionId = s.activeSessionId
        if (sessionId.isBlank()) { _state.update { it.copy(error = "Create a session first") }; return }
        if (!s.isConnected) { _state.update { it.copy(error = "Not connected") }; return }

        val userMsg = Message(role = Role.USER, content = content)
        _state.update { state ->
            state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = it.messages + userMsg) else it })
        }

        viewModelScope.launch {
            val assistantId = UUID.randomUUID().toString()
            _state.update { state ->
                state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = it.messages + Message(id = assistantId, role = Role.ASSISTANT, content = "")) else it })
            }

            api.sendPrompt(sessionId, content).fold(
                onSuccess = {
                    lastEventSeq = 0
                    startPolling(sessionId, assistantId)
                },
                onFailure = { err ->
                    val errMsg = err.message ?: "Send failed"
                    _state.update { state ->
                        state.copy(sessions = state.sessions.map { s ->
                            if (s.id == state.activeSessionId) s.copy(messages = s.messages.map { m ->
                                if (m.id == assistantId) m.copy(content = errMsg) else m
                            }) else s
                        })
                    }
                },
            )
        }
    }

    private fun startPolling(sessionId: String, assistantId: String) {
        stopPolling()
        eventPollJob = viewModelScope.launch {
            while (true) {
                delay(500)
                val r = api.getSessionEvents(sessionId, lastEventSeq)
                if (r.isSuccess) {
                    val text = r.getOrNull() ?: continue
                    if (text.isNotBlank()) {
                        lastEventSeq++
                        _state.update { state ->
                            state.copy(sessions = state.sessions.map { s ->
                                if (s.id == state.activeSessionId) s.copy(messages = s.messages.map { m ->
                                    if (m.id == assistantId) m.copy(content = m.content + text) else m
                                }) else s
                            })
                        }
                    }
                } else {
                    _state.update { it.copy(error = "Event stream error") }
                    delay(2000)
                }
            }
        }
    }

    private fun stopPolling() { eventPollJob?.cancel(); eventPollJob = null }

    fun toggleDarkMode() {
        val next = !_state.value.isDarkMode
        prefs.isDarkMode = next
        _state.update { it.copy(isDarkMode = next) }
    }

    fun clearSession() {
        _state.update { state ->
            state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = emptyList()) else it })
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    override fun onCleared() { stopPolling(); super.onCleared() }
}
