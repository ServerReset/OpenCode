package com.opencode.app.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
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
    val serverSessionId: String? = null,
    val error: String? = null,
    val showSessionDrawer: Boolean = false,
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

class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
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
        _state.update { it.copy(serverUrl = url, isConnected = false, connectionError = null) }
        api.setUrl(url)
        connect()
    }

    fun connect() {
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true, connectionError = null) }
            when (val r = api.health()) {
                is Result.Success -> {
                    _state.update { it.copy(isConnected = true, isConnecting = false) }
                    loadSessions()
                }
                is Result.Failure -> {
                    _state.update { it.copy(isConnected = false, isConnecting = false,
                        connectionError = r.exceptionOrNull()?.message ?: "Connection failed") }
                }
            }
        }
    }

    private suspend fun loadSessions() {
        api.listSessions().fold(
            onSuccess = { serverSessions ->
                val local = serverSessions.map { s ->
                    Session(id = s.id, name = s.name ?: "Session", model = "claude-sonnet")
                }
                _state.update {
                    it.copy(sessions = local, activeSessionId = local.firstOrNull()?.id ?: "")
                }
            },
            onFailure = { _state.update { it.copy(error = "Failed to load sessions") } },
        )
    }

    fun createSession() {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            api.createSession(null).fold(
                onSuccess = { info ->
                    val session = Session(id = info.id, name = info.name ?: "Session")
                    _state.update { it.copy(sessions = it.sessions + session, activeSessionId = session.id, serverSessionId = session.id) }
                },
                onFailure = { _state.update { it.copy(error = "Failed to create session") } },
            )
        }
    }

    fun setActiveSession(id: String) {
        _state.update { it.copy(activeSessionId = id, showSessionDrawer = false) }
        stopPolling()
    }

    fun sendMessage(content: String) {
        val sessionId = _state.value.activeSessionId
        if (sessionId.isBlank()) { _state.update { it.copy(error = "Create a session first") }; return }
        if (!_state.value.isConnected) { _state.update { it.copy(error = "Not connected") }; return }

        val userMsg = Message(role = Role.USER, content = content)
        _state.update { state ->
            state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = it.messages + userMsg) else it })
        }

        viewModelScope.launch {
            val assistantId = UUID.randomUUID().toString()
            val assistantMsg = Message(id = assistantId, role = Role.ASSISTANT, content = "")
            _state.update { state ->
                state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = it.messages + assistantMsg) else it })
            }

            when (val r = api.sendPrompt(sessionId, content)) {
                is Result.Success -> {
                    lastEventSeq = 0
                    startPolling(sessionId, assistantId)
                }
                is Result.Failure -> {
                    val err = r.exceptionOrNull()?.message ?: "Send failed"
                    _state.update { state ->
                        state.copy(sessions = state.sessions.map { s ->
                            if (s.id == state.activeSessionId) s.copy(messages = s.messages.map { m ->
                                if (m.id == assistantId) m.copy(content = err) else m
                            }) else s
                        })
                    }
                }
            }
        }
    }

    private fun startPolling(sessionId: String, assistantId: String) {
        stopPolling()
        eventPollJob = viewModelScope.launch {
            _state.update { it.copy(error = null) }
            while (true) {
                delay(500)
                when (val r = api.getSessionEvents(sessionId, lastEventSeq)) {
                    is Result.Success -> {
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
                    }
                    is Result.Failure -> {
                        _state.update { it.copy(error = "Event stream error") }
                        delay(2000)
                    }
                }
            }
        }
    }

    private fun stopPolling() { eventPollJob?.cancel(); eventPollJob = null }

    fun toggleDarkMode() { _state.update { it.copy(isDarkMode = !it.isDarkMode) } }
    fun toggleSessionDrawer() { _state.update { it.copy(showSessionDrawer = !it.showSessionDrawer) } }

    fun clearSession() {
        _state.update { state ->
            state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = emptyList()) else it })
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    override fun onCleared() { stopPolling(); super.onCleared() }
}
