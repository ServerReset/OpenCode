@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun SettingsScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    var serverUrlInput by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Spacer(Modifier.height(20.dp))

        // Server Connection
        SectionHeader("Server", scheme)
        Surface(shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Server URL") },
                        placeholder = { Text("http://192.168.1.100:4096") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isConnecting,
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { vm.setServerUrl(serverUrlInput) },
                        enabled = !state.isConnecting && serverUrlInput.isNotBlank(),
                    ) {
                        if (state.isConnecting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Connect")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(if (state.isConnected) scheme.primary else scheme.error, RoundedCornerShape(50)))
                    Spacer(Modifier.width(8.dp))
                    val statusText = when {
                        state.isConnected -> "Connected to ${state.serverUrl}"
                        state.isConnecting -> "Connecting..."
                        state.connectionError != null -> "Error: ${state.connectionError}"
                        else -> "Not connected"
                    }
                    Text(statusText, style = MaterialTheme.typography.labelSmall, color = if (state.isConnected) scheme.primary else scheme.error)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader("Appearance", scheme)
        SettingsGroup {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.DarkMode, null, tint = scheme.primary, modifier = Modifier.size(20.dp)) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Dark Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text("Toggle light/dark theme", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
                Switch(checked = state.isDarkMode, onCheckedChange = { vm.toggleDarkMode() })
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader("About", scheme)
        SettingsGroup {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Info, null, tint = scheme.primary, modifier = Modifier.size(20.dp)) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("OpenCode Phone", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text("M3 Expressive · Connect to your server", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
        Text("Connect to an OpenCode server to start chatting.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SectionHeader(title: String, scheme: ColorScheme) {
    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Column(content = content)
    }
}
