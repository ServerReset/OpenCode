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

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // Header with connection + account
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("OpenCode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(if (state.isConnected) scheme.primary else scheme.error))
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isConnected) "Connected" else "Offline", style = MaterialTheme.typography.labelSmall, color = if (state.isConnected) scheme.primary else scheme.error)
                    if (state.isConnecting) {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                    }
                }
            }
            if (state.isConnected && state.sessions.isNotEmpty()) {
                FilledTonalButton(onClick = { vm.createSession() }, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }
        }

        // Account card (if logged in)
        AnimatedVisibility(visible = state.account != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            state.account?.let { acct ->
                Spacer(Modifier.height(8.dp))
                Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), color = scheme.primaryContainer) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, null, tint = scheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(acct.plan.ifBlank { "OpenCode" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onPrimaryContainer)
                            val usage = if (acct.monthlyLimit > 0) "${acct.monthlyUsed} / ${acct.monthlyLimit} monthly" else "API key saved"
                            Text(usage, style = MaterialTheme.typography.labelSmall, color = scheme.onPrimaryContainer)
                            if (acct.monthlyLimit > 0) {
                                Spacer(Modifier.height(6.dp))
                                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(scheme.outlineVariant)) {
                                    val pct = (acct.monthlyUsed.toFloat() / acct.monthlyLimit).coerceIn(0f, 1f)
                                    Box(Modifier.fillMaxWidth(pct).fillMaxHeight().background(scheme.primary))
                                }
                            }
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

        // Sessions header
        if (state.sessions.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${state.sessions.size} conversations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
            }
            Spacer(Modifier.height(8.dp))
        }

        // Sessions or empty state
        if (state.sessions.isEmpty() && !state.isConnecting) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state.isConnected) {
                        Icon(Icons.Default.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No conversations", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("Start one with + New", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    } else {
                        Icon(Icons.Default.CloudOff, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Connect in Settings", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    }
                }
            }
        } else if (state.sessions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(state.sessions, key = { it.id }) { session ->
                    val snippet = session.messages.firstOrNull { it.role == com.opencode.app.data.Role.USER }?.content?.take(120)
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { vm.switchToSession(session.id) },
                        ),
                        shape = RoundedCornerShape(14.dp),
                        color = scheme.surfaceContainerHigh,
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
                                Text("${session.messages.size} msgs", style = MaterialTheme.typography.labelSmall, color = scheme.outline)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
