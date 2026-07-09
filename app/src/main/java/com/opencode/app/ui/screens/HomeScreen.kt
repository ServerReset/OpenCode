package com.opencode.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // Account info bar (if logged in)
        AnimatedVisibility(visible = state.account != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            state.account?.let { acct ->
                Surface(Modifier.fillMaxWidth(), color = scheme.primaryContainer) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("${acct.plan} · ${acct.monthlyUsed}/${acct.monthlyLimit} monthly", style = MaterialTheme.typography.labelMedium, color = scheme.onPrimaryContainer)
                        Spacer(Modifier.weight(1f))
                        // Usage bar
                        val pct = if (acct.monthlyLimit > 0) (acct.monthlyUsed.toFloat() / acct.monthlyLimit).coerceIn(0f, 1f) else 0f
                        Box(Modifier.width(60.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(scheme.outlineVariant)) {
                            Box(Modifier.fillMaxWidth(pct).fillMaxHeight().background(scheme.primary, RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
        }

        // Header
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("OpenCode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(if (state.isConnected) scheme.primary else scheme.error))
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isConnected) "Connected" else "Offline", style = MaterialTheme.typography.labelSmall, color = if (state.isConnected) scheme.primary else scheme.error)
                }
            }
            if (state.isConnected) {
                FilledTonalButton(onClick = { vm.createSession() }, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }
        }

        // Error
        if (state.error != null) {
            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = scheme.errorContainer) {
                Row(Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Error, null, tint = scheme.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = scheme.onErrorContainer, modifier = Modifier.weight(1f))
                }
            }
        }

        // Sessions
        if (state.sessions.isEmpty() && state.isConnected) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No conversations yet", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
        } else if (!state.isConnected && state.sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Connect in Settings", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.sessions, key = { it.id }) { session ->
                    val snippet = session.messages.firstOrNull { it.role == com.opencode.app.data.Role.USER }?.content?.take(100) ?: ""
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { vm.switchToSession(session.id) }),
                        shape = RoundedCornerShape(16.dp),
                        color = scheme.surfaceContainerHigh,
                        tonalElevation = 0.dp,
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(10.dp), color = scheme.primaryContainer, modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Chat, null, tint = scheme.primary, modifier = Modifier.size(18.dp)) }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(session.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("${session.messages.size} messages", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            if (snippet.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(snippet, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}
