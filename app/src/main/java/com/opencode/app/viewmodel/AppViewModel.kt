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
    val screen: Screen = Screen.HOME,
    val isDarkMode: Boolean = false,
    val serverUrl: String = "http://10.0.2.2:4096",
    val password: String = "",
    val apiKey: String = "",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val sessions: List<Session> = emptyList(),
    val activeSessionId: String = "",
    val activeModel: String = "claude-sonnet",
    val account: AccountInfo? = null,
    val showAccountLogin: Boolean = false,
    val error: String? = null,
) {
    val activeSession: Session? get() = sessions.find { it.id == activeSessionId }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences(application)
    private val _state = MutableStateFlow(AppState(
        serverUrl = prefs.serverUrl, password = prefs.password,
        apiKey = prefs.apiKey, isDarkMode = prefs.isDarkMode,
    ))
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val api get() = OpenCodeClient.instance

    init {
        val u = _state.value.serverUrl
        if (u.isNotBlank()) { api.configure(u, _state.value.password); connect() }
        if (_state.value.apiKey.isNotBlank()) fetchAccount()
    }

    fun setScreen(s: Screen) { _state.update { it.copy(screen = s) } }

    fun setServerUrl(url: String, pass: String) {
        prefs.serverUrl = url; prefs.password = pass
        api.configure(url, pass)
        _state.update { it.copy(serverUrl = url, password = pass, isConnected = false, sessions = emptyList(), error = null) }
        connect()
    }

    fun connect() {
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true, error = null) }
            api.health().fold(
                onSuccess = {
                    // Health passed - now test that we can actually list sessions
                    api.listSessions().fold(
                        onSuccess = { _state.update { it.copy(isConnected = true, isConnecting = false) }; processSessions(it) },
                        onFailure = { e ->
                            // Health works but sessions fails - likely auth issue
                            _state.update { it.copy(isConnected = true, isConnecting = false, error = "Server reachable, but ${e.message ?: "session list failed"}. Check password.") }
                        },
                    )
                },
                onFailure = { _state.update { it.copy(isConnecting = false, error = "Cannot reach server") } },
            )
        }
    }

    private fun processSessions(list: List<ServerSession>) {
        viewModelScope.launch {
            val withMessages = list.map { s -> s to api.getMessages(s.id).getOrNull() }
            val sessions = withMessages.map { (s, msgs) ->
                val name = s.title ?: s.id.take(8)
                val messages = msgs?.mapNotNull { m ->
                    val role = when (m.info.role.lowercase()) { "user" -> Role.USER; "assistant" -> Role.ASSISTANT; else -> null } ?: return@mapNotNull null
                    Message(id = m.info.id, role = role, content = m.parts.firstOrNull { it.type == "text" }?.text ?: "")
                } ?: emptyList()
                Session(id = s.id, name = name, messages = messages)
            }.reversed()
            _state.update { it.copy(sessions = sessions, activeSessionId = sessions.firstOrNull()?.id ?: "") }
        }
    }

    fun createSession() {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            api.createSession().fold(
                onSuccess = { s ->
                    val session = Session(id = s.id, name = s.title ?: s.id.take(8))
                    _state.update { it.copy(sessions = listOf(session) + it.sessions, activeSessionId = session.id, screen = Screen.CHAT) }
                },
                onFailure = { err -> _state.update { it.copy(error = "Create failed: ${err.message}") } },
            )
        }
    }

    fun switchToSession(id: String) {
        _state.update { it.copy(activeSessionId = id, screen = Screen.CHAT) }
        viewModelScope.launch {
            api.getMessages(id).fold(
                onSuccess = { msgs ->
                    val messages = msgs.mapNotNull { m ->
                        val role = when (m.info.role) { "user" -> Role.USER; "assistant" -> Role.ASSISTANT; else -> null } ?: return@mapNotNull null
                        Message(id = m.info.id, role = role, content = m.parts.firstOrNull { it.type == "text" }?.text ?: "")
                    }
                    _state.update { state -> state.copy(sessions = state.sessions.map { if (it.id == id) it.copy(messages = messages) else it }) }
                },
                onFailure = { _state.update { it.copy(error = "Messages failed") } },
            )
        }
    }

    fun setApiKey(key: String) {
        prefs.apiKey = key
        _state.update { it.copy(apiKey = key) }
        if (key.isNotBlank()) fetchAccount()
    }

    private fun fetchAccount() {
        viewModelScope.launch {
            api.fetchAccount(_state.value.apiKey).fold(
                onSuccess = { acct -> _state.update { it.copy(account = acct) } },
                onFailure = { /* account API is optional */ },
            )
        }
    }

    fun setActiveModel(m: String) { prefs.activeModel = m; _state.update { it.copy(activeModel = m) } }
    fun toggleAccountLogin() { _state.update { it.copy(showAccountLogin = !it.showAccountLogin) } }

    fun sendMessage(text: String) {
        val sid = _state.value.activeSessionId
        if (sid.isBlank()) { _state.update { it.copy(error = "Create a session first") }; return }
        if (!_state.value.isConnected) { _state.update { it.copy(error = "Not connected") }; return }

        _state.update { s -> s.copy(sessions = s.sessions.map { if (it.id == s.activeSessionId) it.copy(messages = it.messages + Message(role = Role.USER, content = text)) else it }) }

        viewModelScope.launch {
            val aid = UUID.randomUUID().toString()
            _state.update { s -> s.copy(sessions = s.sessions.map { if (it.id == s.activeSessionId) it.copy(messages = it.messages + Message(id = aid, role = Role.ASSISTANT, content = "...")) else it }) }

            api.sendPrompt(sid, text).fold(
                onSuccess = {
                    var attempts = 0
                    while (attempts < 30) {
                        delay(2000)
                        api.getMessages(sid).fold(
                            onSuccess = { msgs ->
                                val t = msgs.firstOrNull { it.info.id == aid }?.parts?.firstOrNull { it.type == "text" }?.text
                                if (t != null && t.isNotBlank()) {
                                    _state.update { state -> state.copy(sessions = state.sessions.map { sess -> if (sess.id == state.activeSessionId) sess.copy(messages = sess.messages.map { m -> if (m.id == aid) m.copy(content = t) else m }) else sess }) }
                                    return@launch
                                }
                            },
                            onFailure = {},
                        )
                        attempts++
                    }
                },
                onFailure = { e -> _state.update { state -> state.copy(sessions = state.sessions.map { sess -> if (sess.id == state.activeSessionId) sess.copy(messages = sess.messages.map { m -> if (m.id == aid) m.copy(content = "Error: ${e.message}") else m }) else sess }) } },
            )
        }
    }

    fun toggleDarkMode() { val n = !_state.value.isDarkMode; prefs.isDarkMode = n; _state.update { it.copy(isDarkMode = n) } }
    fun clearError() { _state.update { it.copy(error = null) } }
}
