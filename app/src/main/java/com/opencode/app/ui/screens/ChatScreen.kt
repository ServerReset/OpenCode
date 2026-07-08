@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import com.opencode.app.data.ModelInfo
import com.opencode.app.data.Role
import com.opencode.app.data.availableModels
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val messages: List<Message> = state.activeSession?.messages ?: emptyList()
    var inputText by remember { mutableStateOf("") }
    val modelName = availableModels.find { it.id == state.activeModel }?.name ?: "Model"

    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }
    LaunchedEffect(state.error) { if (state.error != null) { delay(5000); vm.clearError() } }

    Box(Modifier.fillMaxSize()) {
        // Messages fill the screen
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Chat, null, tint = scheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Start a conversation", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    if (!state.isConnected && state.sessions.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        FilledTonalButton(onClick = { vm.setScreen(com.opencode.app.data.Screen.SETTINGS) }) { Text("Connect") }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 120.dp),
            ) {
                items(messages, key = { it.id }) { msg -> ChatBubble(msg, msg.role == Role.USER) }
            }
        }

        // Bottom controls (thumb zone)
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().statusBarsPadding(),
        ) {
            // Error
            if (state.error != null) {
                Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp), shape = RoundedCornerShape(12.dp), color = scheme.errorContainer) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, null, tint = scheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = scheme.onErrorContainer, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Model chip + message count
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = { vm.toggleModelPicker() }, shape = RoundedCornerShape(50), color = scheme.secondaryContainer, modifier = Modifier.height(28.dp)) {
                    Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Bolt, null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(modelName, style = MaterialTheme.typography.labelSmall, color = scheme.onSecondaryContainer)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.activeSession != null) {
                        Text("${state.activeSession!!.messages.size} msgs", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                    }
                    Surface(onClick = { vm.createSession() }, shape = RoundedCornerShape(50), color = scheme.primaryContainer) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, null, tint = scheme.primary, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("New", style = MaterialTheme.typography.labelSmall, color = scheme.primary)
                        }
                    }
                }
            }

            // Input
            Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), shape = RoundedCornerShape(28.dp), color = scheme.surfaceContainerHighest) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = inputText, onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = scheme.onSurface, fontSize = 14.sp),
                        cursorBrush = SolidColor(scheme.primary),
                        decorationBox = { inner -> if (inputText.isEmpty()) Text("Message...", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant); inner() },
                        enabled = state.isConnected && state.activeSessionId.isNotBlank(),
                    )
                    if (inputText.isNotBlank()) {
                        Surface(onClick = { vm.sendMessage(inputText); inputText = "" }, modifier = Modifier.size(36.dp), shape = RoundedCornerShape(50), color = scheme.primary) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Send, "Send", tint = scheme.onPrimary, modifier = Modifier.size(16.dp)) }
                        }
                    }
                }
            }
        }

        // Model picker dialog
        if (state.showModelPicker) {
            AlertDialog(
                onDismissRequest = { vm.toggleModelPicker() }, shape = MaterialTheme.shapes.extraLarge, containerColor = scheme.surfaceContainerHigh,
                title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Bolt, null, tint = scheme.primary); Spacer(Modifier.width(8.dp)); Text("Select Model", fontWeight = FontWeight.Bold) } },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        availableModels.forEach { model ->
                            val isActive = model.id == state.activeModel
                            Surface(onClick = { vm.setActiveModel(model.id) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                                color = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHighest,
                                border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, scheme.primary) else null) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(10.dp), color = Color(model.color).copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Bolt, null, tint = Color(model.color), modifier = Modifier.size(18.dp)) }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) { Text(model.name, style = MaterialTheme.typography.titleSmall); Text(model.provider, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant) }
                                    if (isActive) Icon(Icons.Filled.CheckCircle, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { vm.toggleModelPicker() }) { Text("Done") } },
            )
        }
    }
}

@Composable
fun ChatBubble(msg: Message, isUser: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp), horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
        if (!isUser) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = scheme.primary, modifier = Modifier.size(20.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("O", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scheme.onSecondaryContainer ?: scheme.onPrimary) }
                }
                Spacer(Modifier.width(6.dp))
                Text("OpenCode", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(4.dp))
        }
        Surface(
            shape = if (isUser) RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp) else RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
            color = if (isUser) scheme.primaryContainer else scheme.surfaceContainerHigh,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant) else null,
        ) {
            Text(msg.content.ifEmpty { "..." }, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = if (isUser) scheme.onPrimaryContainer else scheme.onSurface)
        }
    }
}
