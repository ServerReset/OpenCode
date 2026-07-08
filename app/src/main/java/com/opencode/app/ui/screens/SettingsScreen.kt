@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
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
import com.opencode.app.data.availableModels
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun SettingsScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val model = availableModels.find { it.id == state.activeModel }
    var serverUrlInput by remember(state.isConnected, state.serverUrl) { mutableStateOf(state.serverUrl) }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Surface(Modifier.fillMaxWidth(), color = scheme.surface, tonalElevation = 1.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.toggleSessionDrawer() }) { Icon(Icons.Filled.Menu, "Menu") }
                Text("Settings", modifier = Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(20.dp))

        // Server Connection
        SectionHeader("Server", scheme)
        SettingsGroup {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
                )
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(onClick = { vm.setServerUrl(serverUrlInput) }, enabled = !state.isConnecting && serverUrlInput.isNotBlank()) {
                    if (state.isConnecting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Connect")
                }
            }
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(50), color = if (state.isConnected) scheme.primary else scheme.errorContainer, modifier = Modifier.size(10.dp)) {}
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.isConnected) "Connected to ${state.serverUrl}" else if (state.isConnecting) "Connecting..." else "Not connected",
                    style = MaterialTheme.typography.labelSmall,
                )
                if (state.connectionError != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(state.connectionError, style = MaterialTheme.typography.labelSmall, color = scheme.error)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Appearance", scheme)
        SettingsGroup {
            SettingsRow(Icons.Filled.DarkMode, "Dark Mode", "Toggle light/dark theme", trailing = {
                Switch(checked = state.isDarkMode, onCheckedChange = { vm.toggleDarkMode() })
            })
            SettingsRow(Icons.Filled.Palette, "Dynamic Color", "Use Material You wallpaper colors", trailing = {
                Switch(checked = state.useDynamicColor, onCheckedChange = { vm.toggleDynamicColor() })
            })
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("AI & Models", scheme)
        SettingsGroup {
            SettingsRow(Icons.Filled.Bolt, "Default Model", model?.name ?: "Claude Sonnet 4", onClick = { vm.toggleModelPicker() })
            SettingsRow(Icons.Filled.Link, "Custom API Endpoint", "Connect any provider via API", onClick = {})
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Sessions (${state.sessions.size})", scheme)
        SettingsGroup {
            SettingsRow(Icons.Filled.Delete, "Clear Current Session", "${state.activeSession?.messages?.size ?: 0} messages", trailing = {
                FilledTonalButton(onClick = { vm.clearSession() }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = scheme.errorContainer, contentColor = scheme.onErrorContainer)) {
                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                }
            })
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("About", scheme)
        SettingsGroup {
            SettingsRow(Icons.Filled.Shield, "Privacy", "We don't store your code or context", onClick = {})
            SettingsRow(Icons.Filled.Info, "Version", "0.1 · Material 3 Expressive · Compose", onClick = {})
        }

        Spacer(Modifier.height(24.dp))
        Text("OpenCode Phone · M3 Expressive", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp))
    }
}

@Composable
private fun SectionHeader(title: String, scheme: ColorScheme) {
    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val selected = trailing != null || onClick != null
    Surface(onClick = onClick ?: {}, modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                if (description != null) Text(description, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
            if (trailing != null) trailing()
            else if (onClick != null) Icon(Icons.Filled.ChevronRight, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}
