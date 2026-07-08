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

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // Logo + status - compact header, no bar
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = scheme.primaryContainer, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Shield, null, tint = scheme.primary, modifier = Modifier.size(22.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("OpenCode", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(if (state.isConnected) scheme.primary else scheme.error, RoundedCornerShape(50)))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when { state.isConnected -> "Connected"; state.isConnecting -> "Connecting..."; else -> "Offline" },
                        style = MaterialTheme.typography.labelSmall, color = if (state.isConnected) scheme.primary else scheme.error,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action buttons - full width, no fixed heights
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(onClick = { vm.createSession() }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large, color = scheme.primaryContainer) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Add, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("New Chat", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = scheme.primary)
                }
            }
            Surface(onClick = { vm.setScreen(Screen.SETTINGS) }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Settings, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Settings", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = scheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Sessions header
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Sessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
            Spacer(Modifier.weight(1f))
            if (state.isConnected && state.sessions.isNotEmpty()) {
                Surface(onClick = { vm.createSession() }, shape = RoundedCornerShape(50), color = scheme.primaryContainer) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, null, tint = scheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New", style = MaterialTheme.typography.labelSmall, color = scheme.primary)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        if (state.sessions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.TopCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp)) {
                    Icon(Icons.Filled.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(if (state.isConnected) "No sessions yet. Tap + to start." else "Connect in Settings", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.sessions, key = { it.id }) { session ->
                    val isActive = session.id == state.activeSessionId
                    val msgCount = session.messages.size
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { vm.switchToSession(session.id) }),
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
