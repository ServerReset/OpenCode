@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencode.app.data.availableModels
import com.opencode.app.data.Screen
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val modelName = availableModels.find { it.id == state.activeModel }?.name ?: "Model"

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // Header
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)) {
            Surface(shape = MaterialTheme.shapes.large, color = scheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Shield, null, tint = scheme.primary, modifier = Modifier.size(28.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            Text("OpenCode", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)

            // Status row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(if (state.isConnected) scheme.primary else scheme.error, RoundedCornerShape(50)))
                Spacer(Modifier.width(8.dp))
                Text(
                    when { state.isConnected -> "Connected"; state.isConnecting -> "Connecting..."; else -> "Offline" },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.isConnected) scheme.primary else scheme.error,
                )
                if (state.isConnected) {
                    Spacer(Modifier.width(12.dp))
                    Text("· $modelName", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                }
            }
        }

        // Quick actions
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(Icons.Filled.Add, "New Session", scheme.primary) { vm.createSession() }
            ActionCard(Icons.Filled.Settings, "Settings", scheme.secondary) { vm.setScreen(Screen.SETTINGS) }
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Sessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
            Spacer(Modifier.weight(1f))
            if (!state.isConnected) {
                TextButton(onClick = { vm.setScreen(Screen.SETTINGS) }) { Text("Connect", style = MaterialTheme.typography.labelMedium) }
            }
        }
        Spacer(Modifier.height(8.dp))

        if (state.sessions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.TopCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(40.dp))
                    Icon(Icons.Filled.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(if (state.isConnected) "No sessions yet" else "Connect to load sessions", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.sessions, key = { it.id }) { session ->
                    val isActive = session.id == state.activeSessionId
                    val msgCount = session.messages.size
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null,
                            onClick = { vm.switchToSession(session.id) },
                        ),
                        shape = MaterialTheme.shapes.medium,
                        color = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHigh,
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = MaterialTheme.shapes.small, color = if (isActive) scheme.primary else scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Chat, null, tint = if (isActive) scheme.onPrimary else scheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(session.name, style = MaterialTheme.typography.titleSmall, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
                                Text("$msgCount messages", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(onClick = onClick, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        }
    }
}
