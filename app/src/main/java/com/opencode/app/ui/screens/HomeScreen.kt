package com.opencode.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    var showServerConfig by remember { mutableStateOf(!state.isConnected) }
    var serverUrl by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }
    var serverPass by remember(state.password) { mutableStateOf(state.password) }
    var apiKey by remember(state.apiKey) { mutableStateOf(state.apiKey) }
    var showFab by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Top: server config (collapsible)
            if (!state.isConnected || showServerConfig) {
                Surface(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Server", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(value = serverUrl, onValueChange = { serverUrl = it }, modifier = Modifier.fillMaxWidth(),
                            label = { Text("URL") }, placeholder = { Text("http://192.168.1.100:4096") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = serverPass, onValueChange = { serverPass = it }, modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.setServerUrl(serverUrl, serverPass) }, enabled = serverUrl.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Text("Connect")
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("OpenCode Account", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, modifier = Modifier.fillMaxWidth(),
                            label = { Text("API Key") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(8.dp))
                        FilledTonalButton(onClick = { vm.setApiKey(apiKey) }, enabled = apiKey.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Text("Save Key")
                        }
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Chats", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(if (state.isConnected) scheme.primary else scheme.error))
                            Spacer(Modifier.width(6.dp))
                            Text(if (state.isConnected) "Connected · ${state.sessions.size} chats" else "Offline", style = MaterialTheme.typography.labelSmall, color = if (state.isConnected) scheme.primary else scheme.error)
                        }
                    }
                    IconButton(onClick = { showServerConfig = true }) { Icon(Icons.Default.Settings, "Settings") }
                }
            }

            // Account card
            AnimatedVisibility(visible = state.account != null && state.isConnected, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                state.account?.let { acct ->
                    Spacer(Modifier.height(8.dp))
                    Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), color = scheme.primaryContainer) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, null, tint = scheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(acct.plan.ifBlank { "OpenCode" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onPrimaryContainer)
                                val usage = if (acct.monthlyLimit > 0) "${acct.monthlyUsed} / ${acct.monthlyLimit}" else "API key saved"
                                Text(usage, style = MaterialTheme.typography.labelSmall, color = scheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            // Error
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(12.dp), color = scheme.errorContainer) {
                    Row(Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Error, null, tint = scheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = scheme.onErrorContainer, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Sessions
            if (state.sessions.isEmpty() && state.isConnected && !state.isConnecting) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No chats yet", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        FilledTonalButton(onClick = { vm.createSession() }, shape = RoundedCornerShape(20.dp)) { Text("Start one") }
                    }
                }
            } else if (state.sessions.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.sessions, key = { it.id }) { session ->
                        val snippet = session.messages.firstOrNull { it.role == com.opencode.app.data.Role.USER }?.content?.take(100)
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable(
                                interactionSource = remember { MutableInteractionSource() }, indication = null,
                                onClick = { vm.switchToSession(session.id) },
                            ),
                            shape = RoundedCornerShape(14.dp), color = scheme.surfaceContainerHigh,
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(10.dp), color = scheme.primaryContainer, modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Chat, null, tint = scheme.primary, modifier = Modifier.size(18.dp)) }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(session.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (snippet != null) {
                                        Text(snippet, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Text("${session.messages.size}", style = MaterialTheme.typography.labelSmall, color = scheme.outline)
                            }
                        }
                    }
                }
            }
        }

        // Bottom floating action bar
        FloatingActionBar(
            state = state,
            vm = vm,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
            onToggleServer = { showServerConfig = !showServerConfig },
        )
    }
}
