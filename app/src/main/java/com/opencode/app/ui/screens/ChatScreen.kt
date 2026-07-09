package com.opencode.app.ui.screens

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
                    Icon(Icons.Default.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Select a conversation", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
        } else {
            // messages
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp, bottom = 120.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = if (msg.role == Role.USER) Alignment.End else Alignment.Start) {
                        Surface(
                            shape = if (msg.role == Role.USER) RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp) else RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
                            color = if (msg.role == Role.USER) scheme.primaryContainer else scheme.surfaceContainerHigh,
                            border = if (msg.role != Role.USER) androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant) else null,
                        ) {
                            Text(msg.content.ifEmpty { "..." }, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium,
                                color = if (msg.role == Role.USER) scheme.onPrimaryContainer else scheme.onSurface)
                        }
                    }
                }
            }
        }

        // Input area (thumb zone)
        if (session != null && state.isConnected) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(12.dp),
            ) {
                // Error
                if (state.error != null) {
                    Surface(Modifier.fillMaxWidth().padding(bottom = 4.dp), shape = RoundedCornerShape(12.dp), color = scheme.errorContainer) {
                        Row(Modifier.padding(10.dp)) {
                            Icon(Icons.Default.Error, null, tint = scheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = scheme.onErrorContainer, modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = scheme.surfaceContainerHighest) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = input, onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = scheme.onSurface, fontSize = 14.sp),
                            cursorBrush = SolidColor(scheme.primary),
                            decorationBox = { inner -> if (input.isEmpty()) Text("Message", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant); inner() },
                        )
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
