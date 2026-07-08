package com.opencode.app.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TerminalEntry(
    val type: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

data class AppState(
    val currentScreen: Screen = Screen.HOME,
    val isDarkMode: Boolean = false,
    val useDynamicColor: Boolean = true,
    val serverUrl: String = "http://10.0.2.2:4096",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val sessions: List<Session> = listOf(Session(name = "Default Session")),
    val activeSessionId: String = "",
    val activeModel: String = "claude-sonnet",
    val isStreaming: Boolean = false,
    val terminalHistory: List<TerminalEntry> = listOf(
        TerminalEntry("output", "OpenCode Terminal v1.0.0"),
        TerminalEntry("output", "Connect to a server to execute commands."),
    ),
    val terminalInput: String = "",
    val terminalCommandHistory: List<String> = emptyList(),
    val terminalHistoryIndex: Int = -1,
    val showModelPicker: Boolean = false,
    val showSessionDrawer: Boolean = false,
    val error: String? = null,
    val fileTree: List<FileTreeEntry> = emptyList(),
    val fileContents: Map<String, String> = emptyMap(),
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
    NavItem(Screen.FILES, "Files", Icons.Filled.Folder, Icons.Filled.FolderOpen),
    NavItem(Screen.TERMINAL, "Terminal", Icons.Filled.Terminal, Icons.Filled.Terminal),
    NavItem(Screen.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Filled.Settings),
)

class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        AppState().copy(activeSessionId = AppState().sessions.first().id)
    )
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val api get() = OpenCodeClient.instance

    init {
        val url = _state.value.serverUrl
        if (url.isNotBlank()) {
            api.setUrl(url)
            connect()
        }
    }

    fun setScreen(screen: Screen) { _state.update { it.copy(currentScreen = screen) } }

    fun setServerUrl(url: String) {
        _state.update { it.copy(serverUrl = url) }
        api.setUrl(url)
        connect()
    }

    fun connect() {
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true, connectionError = null) }
            val result = api.testConnection()
            result.fold(
                onSuccess = { msg ->
                    _state.update { it.copy(isConnected = true, isConnecting = false, connectionError = null) }
                    loadFileTree()
                },
                onFailure = { error ->
                    _state.update { it.copy(isConnected = false, isConnecting = false, connectionError = error.message ?: "Connection failed") }
                },
            )
        }
    }

    private suspend fun loadFileTree() {
        api.getFileTree().fold(
            onSuccess = { tree -> _state.update { it.copy(fileTree = tree) } },
            onFailure = { err -> _state.update { it.copy(error = "Failed to load files: ${err.message}") } },
        )
    }

    fun toggleDarkMode() { _state.update { it.copy(isDarkMode = !it.isDarkMode) } }
    fun toggleDynamicColor() { _state.update { it.copy(useDynamicColor = !it.useDynamicColor) } }
    fun setActiveModel(modelId: String) { _state.update { it.copy(activeModel = modelId, showModelPicker = false) } }
    fun toggleModelPicker() { _state.update { it.copy(showModelPicker = !it.showModelPicker) } }
    fun toggleSessionDrawer() { _state.update { it.copy(showSessionDrawer = !it.showSessionDrawer) } }

    fun createSession(name: String? = null) {
        val session = Session(name = name ?: "Session ${_state.value.sessions.size + 1}", model = _state.value.activeModel)
        _state.update { it.copy(sessions = it.sessions + session, activeSessionId = session.id, showSessionDrawer = false) }
    }

    fun deleteSession(id: String) {
        _state.update { state ->
            val sessions = state.sessions.filter { it.id != id }
            val activeId = if (state.activeSessionId == id) sessions.firstOrNull()?.id ?: "" else state.activeSessionId
            state.copy(sessions = sessions.ifEmpty { listOf(Session(name = "Default Session")) }, activeSessionId = activeId)
        }
    }

    fun setActiveSession(id: String) { _state.update { it.copy(activeSessionId = id, showSessionDrawer = false) } }
    fun pinSession(id: String) { _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == id) it.copy(pinned = !it.pinned) else it }) } }

    fun sendMessage(content: String) {
        val msg = Message(role = Role.USER, content = content)
        _state.update { state ->
            state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = it.messages + msg) else it })
        }

        if (!_state.value.isConnected) {
            _state.update { it.copy(error = "Not connected. Configure server in Settings.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isStreaming = true, error = null) }
            val assistantId = java.util.UUID.randomUUID().toString()
            _state.update { state ->
                state.copy(sessions = state.sessions.map { s ->
                    if (s.id == state.activeSessionId) s.copy(messages = s.messages + Message(id = assistantId, role = Role.ASSISTANT, content = ""))
                    else s
                })
            }

            val result = api.chat(ChatRequest(message = content, sessionId = _state.value.activeSessionId, model = _state.value.activeModel))
            result.fold(
                onSuccess = { response ->
                    _state.update { state ->
                        state.copy(sessions = state.sessions.map { s ->
                            if (s.id == state.activeSessionId) {
                                s.copy(messages = s.messages.map { m ->
                                    if (m.id == assistantId) m.copy(content = response) else m
                                })
                            } else s
                        })
                    }
                },
                onFailure = { error ->
                    _state.update { state ->
                        val errorMsg = "Error: ${error.message ?: "Request failed"}"
                        state.copy(sessions = state.sessions.map { s ->
                            if (s.id == state.activeSessionId) {
                                s.copy(messages = s.messages.map { m ->
                                    if (m.id == assistantId) m.copy(content = errorMsg) else m
                                })
                            } else s
                        }, error = errorMsg)
                    }
                },
            )
            _state.update { it.copy(isStreaming = false) }
        }
    }

    fun addAssistantMessage(msg: Message) {
        _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = it.messages + msg) else it }) }
    }

    fun updateAssistantMessage(msgId: String, content: String) {
        _state.update { state ->
            state.copy(sessions = state.sessions.map { s ->
                if (s.id == state.activeSessionId) s.copy(messages = s.messages.map { if (it.id == msgId) it.copy(content = content) else it })
                else s
            })
        }
    }

    fun setStreaming(v: Boolean) { _state.update { it.copy(isStreaming = v) } }
    fun setError(error: String?) { _state.update { it.copy(error = error) } }
    fun clearError() { _state.update { it.copy(error = null) } }

    fun clearSession() {
        _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == state.activeSessionId) it.copy(messages = emptyList()) else it }) }
    }

    fun executeCommand(cmd: String) {
        if (!_state.value.isConnected) {
            _state.update { it.copy(terminalHistory = it.terminalHistory + TerminalEntry("error", "Not connected. Configure server in Settings.")) }
            return
        }

        _state.update { it.copy(terminalHistory = it.terminalHistory + TerminalEntry("command", cmd)) }
        viewModelScope.launch {
            val result = api.executeCommand(cmd)
            result.fold(
                onSuccess = { res ->
                    _state.update { state ->
                        state.copy(terminalHistory = state.terminalHistory + TerminalEntry("output", res.output))
                    }
                },
                onFailure = { error ->
                    _state.update { state ->
                        state.copy(terminalHistory = state.terminalHistory + TerminalEntry("error", "Error: ${error.message ?: "Command failed"}"))
                    }
                },
            )
        }
    }

    fun addTerminalEntry(entry: TerminalEntry) { _state.update { it.copy(terminalHistory = it.terminalHistory + entry) } }
    fun clearTerminal() { _state.update { it.copy(terminalHistory = emptyList()) } }
    fun addTerminalCommand(cmd: String) { _state.update { it.copy(terminalCommandHistory = it.terminalCommandHistory + cmd, terminalHistoryIndex = -1) } }
}
