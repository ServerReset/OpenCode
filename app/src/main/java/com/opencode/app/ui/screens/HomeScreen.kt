@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(40.dp))

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = MaterialTheme.shapes.large, color = scheme.primaryContainer, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Shield, null, tint = scheme.primary, modifier = Modifier.size(36.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            Text("OpenCode", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = scheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text("AI coding agent on mobile", style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            // Connection status
            Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                color = if (state.isConnected) scheme.primaryContainer else scheme.errorContainer) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(if (state.isConnected) scheme.primary else scheme.error, MaterialTheme.shapes.extraSmall))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when { state.isConnected -> "Connected"; state.isConnecting -> "Connecting..."; else -> "Not connected" },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.isConnected) scheme.onPrimaryContainer else scheme.onErrorContainer,
                    )
                    Spacer(Modifier.weight(1f))
                    if (!state.isConnected) {
                        TextButton(onClick = { vm.setScreen(Screen.SETTINGS) }) { Text("Settings", style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Quick actions
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                Triple(Icons.Filled.Chat, "New Chat", { vm.createSession(); vm.setScreen(Screen.CHAT) }),
                Triple(Icons.Filled.Settings, "Settings", { vm.setScreen(Screen.SETTINGS) }),
            ).forEach { (icon, label, onClick) ->
                Surface(onClick = onClick, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Sessions list
        Text("Sessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
        Spacer(Modifier.height(8.dp))
        state.sessions.take(5).forEach { session ->
            val isActive = session.id == state.activeSessionId
            val msgCount = session.messages.size
            Surface(onClick = { vm.setActiveSession(session.id); vm.setScreen(Screen.CHAT) }, modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium, color = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHigh) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.small, color = if (isActive) scheme.primary else scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Chat, null, tint = if (isActive) scheme.onPrimary else scheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(session.name, style = MaterialTheme.typography.titleSmall, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal, color = scheme.onSurface)
                        Text("$msgCount messages", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(80.dp))
    }
}
