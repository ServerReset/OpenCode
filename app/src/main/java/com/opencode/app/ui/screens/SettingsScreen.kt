@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencode.app.data.availableModels
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun SettingsScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val model = availableModels.find { it.id == state.activeModel }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Surface(Modifier.fillMaxWidth(), color = scheme.surface, tonalElevation = 1.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.toggleSessionDrawer() }) { Icon(Icons.Filled.Menu, "Menu") }
                Text("Settings", modifier = Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(20.dp))

        SectionHeader("Appearance", scheme)
        SettingsGroup {
            SettingsRow(Icons.Filled.DarkMode, "Dark Mode", "Toggle light/dark theme") {
                Switch(checked = androidx.compose.foundation.isSystemInDarkTheme(), onCheckedChange = {})
            }
            SettingsRow(Icons.Filled.Palette, "Dynamic Color", "Use Material You wallpaper colors") {
                Switch(checked = true, onCheckedChange = {})
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("AI & Models", scheme)
        SettingsGroup {
            SettingsRow(Icons.Filled.Bolt, "Default Model", model?.name ?: "Claude Sonnet 4", onClick = { vm.toggleModelPicker() })
            SettingsRow(Icons.Filled.AccountCircle, "GitHub Copilot", "Connect your Copilot account", onClick = {})
            SettingsRow(Icons.Filled.Link, "Custom API Endpoint", "Connect any provider via API", onClick = {})
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Sessions (${state.sessions.size})", scheme)
        SettingsGroup {
            SettingsRow(Icons.Filled.Delete, "Clear Current Session", "${state.activeSession?.messages?.size ?: 0} messages") {
                FilledTonalButton(onClick = { vm.clearSession() }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = scheme.errorContainer, contentColor = scheme.onErrorContainer)) {
                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Privacy & About", scheme)
        SettingsGroup {
            SettingsRow(Icons.Filled.Shield, "Privacy Notice", "We don't store your code or context", onClick = {})
            SettingsRow(Icons.Filled.Info, "OpenCode", "v0.1 · Material 3 Expressive · Compose", onClick = {})
            SettingsRow(Icons.Filled.Share, "Share Links", "Generate shareable session links", onClick = {})
        }

        Spacer(Modifier.height(24.dp))
        Text("OpenCode Phone · M3 Expressive · Jetpack Compose", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp))
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
            trailing?.let { it() }
                ?: if (onClick != null) Icon(Icons.Filled.ChevronRight, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}
