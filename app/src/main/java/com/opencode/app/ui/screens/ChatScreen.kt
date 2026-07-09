package com.opencode.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
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

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // Top header with back button
        Surface(Modifier.fillMaxWidth(), color = scheme.surface) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.setScreen(com.opencode.app.data.Screen.HOME) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(20.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(session?.name ?: "Chat", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("${messages.size} msgs", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
            }
        }

        // Messages
        Box(Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No messages yet", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)) {
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
        }

        // Input area
        if (state.isConnected) {
            Surface(Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp), shape = RoundedCornerShape(28.dp), color = scheme.surfaceContainerHighest, shadowElevation = 4.dp) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = scheme.onSurface, fontSize = 14.sp), cursorBrush = SolidColor(scheme.primary),
                        decorationBox = { inner -> if (input.isEmpty()) Text("Message", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant); inner() },
                        enabled = session != null,
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
