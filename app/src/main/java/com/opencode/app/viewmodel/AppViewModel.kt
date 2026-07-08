package com.opencode.app.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.opencode.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TerminalEntry(
    val type: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

data class AppState(
    val currentScreen: Screen = Screen.HOME,
    val isDarkMode: Boolean = false,
    val useDynamicColor: Boolean = true,
    val sessions: List<Session> = listOf(Session(name = "Default Session")),
    val activeSessionId: String = "",
    val activeModel: String = "claude-sonnet",
    val isStreaming: Boolean = false,
    val terminalHistory: List<TerminalEntry> = listOf(
        TerminalEntry("output", "OpenCode Terminal v1.0.0"),
        TerminalEntry("output", "Type a command and press Enter..."),
    ),
    val terminalInput: String = "",
    val terminalCommandHistory: List<String> = emptyList(),
    val terminalHistoryIndex: Int = -1,
    val showModelPicker: Boolean = false,
    val showSessionDrawer: Boolean = false,
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
    NavItem(Screen.FILES, "Files", Icons.Filled.Folder, Icons.Filled.FolderOpen),
    NavItem(Screen.TERMINAL, "Terminal", Icons.Filled.Terminal, Icons.Filled.Terminal),
    NavItem(Screen.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Filled.Settings),
)

class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState().copy(activeSessionId = AppState().sessions.first().id))
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun setScreen(screen: Screen) {
        _state.update { it.copy(currentScreen = screen) }
    }

    fun toggleDarkMode() {
        _state.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleDynamicColor() {
        _state.update { it.copy(useDynamicColor = !it.useDynamicColor) }
    }

    fun setActiveModel(modelId: String) {
        _state.update { it.copy(activeModel = modelId, showModelPicker = false) }
    }

    fun toggleModelPicker() {
        _state.update { it.copy(showModelPicker = !it.showModelPicker) }
    }

    fun toggleSessionDrawer() {
        _state.update { it.copy(showSessionDrawer = !it.showSessionDrawer) }
    }

    fun createSession(name: String? = null) {
        val session = Session(
            name = name ?: "Session ${_state.value.sessions.size + 1}",
            model = _state.value.activeModel,
        )
        _state.update {
            it.copy(
                sessions = it.sessions + session,
                activeSessionId = session.id,
                showSessionDrawer = false,
            )
        }
    }

    fun deleteSession(id: String) {
        _state.update { state ->
            val sessions = state.sessions.filter { it.id != id }
            val activeId = if (state.activeSessionId == id) sessions.firstOrNull()?.id ?: "" else state.activeSessionId
            state.copy(sessions = sessions.ifEmpty { listOf(Session(name = "Default Session")) }, activeSessionId = activeId)
        }
    }

    fun setActiveSession(id: String) {
        _state.update { it.copy(activeSessionId = id, showSessionDrawer = false) }
    }

    fun pinSession(id: String) {
        _state.update { state ->
            state.copy(sessions = state.sessions.map { if (it.id == id) it.copy(pinned = !it.pinned) else it })
        }
    }

    fun sendMessage(content: String) {
        val msg = Message(role = Role.USER, content = content)
        _state.update { state ->
            state.copy(
                sessions = state.sessions.map { s ->
                    if (s.id == state.activeSessionId) s.copy(messages = s.messages + msg) else s
                }
            )
        }
    }

    fun addAssistantMessage(msg: Message) {
        _state.update { state ->
            state.copy(
                sessions = state.sessions.map { s ->
                    if (s.id == state.activeSessionId) s.copy(messages = s.messages + msg) else s
                }
            )
        }
    }

    fun updateAssistantMessage(msgId: String, content: String) {
        _state.update { state ->
            state.copy(
                sessions = state.sessions.map { s ->
                    if (s.id == state.activeSessionId) {
                        s.copy(messages = s.messages.map { m ->
                            if (m.id == msgId) m.copy(content = content) else m
                        })
                    } else s
                }
            )
        }
    }

    fun setStreaming(v: Boolean) {
        _state.update { it.copy(isStreaming = v) }
    }

    fun setError(error: String?) {
        _state.update { it.copy(error = error) }
    }

    fun clearSession() {
        _state.update { state ->
            state.copy(
                sessions = state.sessions.map { s ->
                    if (s.id == state.activeSessionId) s.copy(messages = emptyList()) else s
                }
            )
        }
    }

    fun addTerminalEntry(entry: TerminalEntry) {
        _state.update { state ->
            state.copy(terminalHistory = listOf(entry) + state.terminalHistory.take(199))
        }
    }

    fun clearTerminal() {
        _state.update { it.copy(terminalHistory = emptyList()) }
    }

    fun setTerminalInput(input: String) {
        _state.update { it.copy(terminalInput = input) }
    }

    fun addTerminalCommand(cmd: String) {
        _state.update { state ->
            state.copy(
                terminalCommandHistory = state.terminalCommandHistory + cmd,
                terminalHistoryIndex = -1,
            )
        }
    }

    fun setTerminalHistoryIndex(idx: Int) {
        _state.update { it.copy(terminalHistoryIndex = idx) }
    }
}
