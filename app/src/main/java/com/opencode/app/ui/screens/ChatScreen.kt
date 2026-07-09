package com.opencode.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.data.Message
import com.opencode.app.data.Role
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun ChatScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val session = state.activeSession
    val messages = session?.messages ?: emptyList()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Box(Modifier.fillMaxSize()) {
        if (session == null) {
            Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Select a conversation", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
        } else {
            // Session title
            Surface(Modifier.fillMaxWidth(), color = scheme.surface, tonalElevation = 2.dp) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.setScreen(com.opencode.app.data.Screen.HOME) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f)) {
                        Text(session.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("${messages.size} msgs", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                    FilledTonalButton(onClick = { vm.createSession() }, modifier = Modifier.height(32.dp), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp, bottom = 110.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalAlignment = if (msg.role == Role.USER) Alignment.End else Alignment.Start,
                        ) {
                            if (msg.role != Role.USER) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = scheme.primary, modifier = Modifier.size(18.dp)) {
                                        Box(contentAlignment = Alignment.Center) { Text("O", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = scheme.onPrimary) }
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Text("OpenCode", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                                }
                            }
                            Surface(
                                shape = if (msg.role == Role.USER) RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp) else RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
                                color = if (msg.role == Role.USER) scheme.primaryContainer else scheme.surfaceContainerHigh,
                                shadowElevation = if (msg.role == Role.USER) 0.dp else 1.dp,
                                border = if (msg.role != Role.USER) androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant) else null,
                            ) {
                                Text(msg.content.ifEmpty { "..." }, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium,
                                    color = if (msg.role == Role.USER) scheme.onPrimaryContainer else scheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Input area
        if (session != null && state.isConnected) {
            Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()) {
                if (state.error != null) {
                    Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = scheme.errorContainer) {
                        Row(Modifier.padding(10.dp)) {
                            Icon(Icons.Default.Error, null, tint = scheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = scheme.onErrorContainer, modifier = Modifier.weight(1f))
                        }
                    }
                }
                Surface(Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(28.dp), color = scheme.surfaceContainerHighest, shadowElevation = 4.dp) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = scheme.onSurface, fontSize = 14.sp), cursorBrush = SolidColor(scheme.primary),
                            decorationBox = { inner -> if (input.isEmpty()) Text("Message", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant); inner() })
                        if (input.isNotBlank()) {
                            Surface(onClick = { vm.sendMessage(input); input = "" }, modifier = Modifier.size(36.dp), shape = RoundedCornerShape(50), color = scheme.primary) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Send, "Send", tint = scheme.onPrimary, modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
