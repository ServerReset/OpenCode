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
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize()) {
        // Sessions list
        if (state.sessions.isEmpty()) {
            Column(Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text(if (state.isConnected) "No conversations yet" else "Connect in Settings", style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            ) {
                items(state.sessions, key = { it.id }) { session ->
                    val isActive = session.id == state.activeSessionId
                    val msgCount = session.messages.size
                    val snippet = session.messages.firstOrNull { it.role == com.opencode.app.data.Role.USER }?.content?.take(80) ?: ""
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { vm.switchToSession(session.id) }),
                        shape = MaterialTheme.shapes.medium,
                        color = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHigh,
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Surface(shape = MaterialTheme.shapes.small, color = if (isActive) scheme.primary else scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Chat, null, tint = if (isActive) scheme.onPrimary else scheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(session.name, style = MaterialTheme.typography.titleSmall, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium)
                                if (snippet.isNotEmpty()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(snippet, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 1)
                                }
                                Text("$msgCount messages", style = MaterialTheme.typography.labelSmall, color = scheme.outline)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        // Bottom actions (thumb zone)
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().statusBarsPadding(),
            color = scheme.surface,
            tonalElevation = 8.dp,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(onClick = { vm.createSession() }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large, color = scheme.primaryContainer) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Filled.Add, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("New Chat", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = scheme.primary)
                    }
                }
                Surface(onClick = { vm.setScreen(com.opencode.app.data.Screen.SETTINGS) }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Filled.Settings, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Settings", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
