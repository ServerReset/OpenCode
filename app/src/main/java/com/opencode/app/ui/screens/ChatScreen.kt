@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.data.Message
import com.opencode.app.data.Role
import com.opencode.app.data.Session
import com.opencode.app.data.availableModels
import com.opencode.app.ui.components.M3EChip
import com.opencode.app.ui.components.M3EFilledButton
import com.opencode.app.ui.components.M3ETextButton
import com.opencode.app.ui.components.M3ETonalButton
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val messages = state.activeSession?.messages ?: emptyList()
    var inputText by remember { mutableStateOf("") }

    val modelName = availableModels.find { it.id == state.activeModel }?.name ?: "Select Model"

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // Auto-dismiss errors after 5s
    LaunchedEffect(state.error) {
        if (state.error != null) {
            delay(5000)
            vm.clearError()
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // Top bar
        Surface(Modifier.fillMaxWidth(), color = scheme.surface) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.toggleSessionDrawer() }) { Icon(Icons.Filled.Menu, "Sessions") }
                Column(Modifier.weight(1f).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(state.activeSession?.name ?: "", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
                IconButton(onClick = { vm.createSession() }) { Icon(Icons.Filled.Add, "New") }
            }
        }

        // Model selector row
        Surface(Modifier.fillMaxWidth(), color = scheme.surface, tonalElevation = 0.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    M3EChip(onClick = { vm.toggleModelPicker() }, label = modelName, icon = Icons.Filled.Bolt)
                    if (messages.isNotEmpty()) {
                        M3EChip(onClick = { vm.clearSession() }, label = "Clear")
                    }
                }
                IconButton(onClick = vm::toggleSessionDrawer) { Icon(Icons.Filled.MoreVert, "More") }
            }
        }

        // Messages area
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = scheme.primaryContainer, modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Chat, null, tint = scheme.primary, modifier = Modifier.size(36.dp)) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Start a conversation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Ask about your code — debug, write, or explain.\nOr try: \"write a React component\"", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(msg, isUser = msg.role == Role.USER)
                }
                if (state.isStreaming) {
                    item { ThinkingIndicator() }
                }
                if (state.error != null) {
                    item {
                        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = MaterialTheme.shapes.medium, color = scheme.errorContainer) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Error, null, tint = scheme.error, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = scheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }

        // Chat input
        Surface(Modifier.fillMaxWidth(), color = scheme.surface) {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                Surface(Modifier.weight(1f), shape = RoundedCornerShape(28), color = scheme.surfaceContainerHighest) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AttachFile, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = scheme.onSurface, fontSize = 14.sp),
                            cursorBrush = SolidColor(scheme.primary),
                            decorationBox = { inner ->
                                if (inputText.isEmpty()) Text("Ask anything about your code...", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                                inner()
                            },
                            enabled = !state.isStreaming,
                            maxLines = 4,
                        )
                        if (inputText.isNotBlank()) {
                            Surface(
                                onClick = {
                                    vm.setError(null)
                                    vm.sendMessage(inputText)
                                    inputText = ""
                                },
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(50.dp),
                                color = scheme.primary,
                                enabled = !state.isStreaming,
                            ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Send, "Send", tint = scheme.onPrimary, modifier = Modifier.size(18.dp)) } }
                        } else {
                            Icon(Icons.Filled.Mic, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }

    if (state.showModelPicker) ModelPickerSheet(state.activeModel, vm)
    if (state.showSessionDrawer) SessionDrawerSheet(state, vm)
}

@Composable
fun ChatBubble(msg: Message, isUser: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp), horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
        if (!isUser) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = scheme.primary, modifier = Modifier.size(20.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("O", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scheme.onPrimary) }
                }
                Spacer(Modifier.width(6.dp))
                Text("OpenCode", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Text(getTimeAgo(msg.timestamp), style = MaterialTheme.typography.labelSmall, color = scheme.outline)
            }
            Spacer(Modifier.height(4.dp))
        }
        Surface(
            shape = if (isUser) RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp) else RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
            color = if (isUser) scheme.primaryContainer else scheme.surfaceContainerHigh,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant) else null,
        ) {
            Box(Modifier.padding(12.dp)) {
                RichTextContent(msg.content, scheme)
            }
        }
    }
}

@Composable
private fun RichTextContent(content: String, scheme: ColorScheme) {
    val parts = remember(content) { parseRichText(content) }
    Column {
        parts.forEach { part ->
            when (part) {
                is RichPart.Text -> Text(part.text, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface)
                is RichPart.CodeBlock -> {
                    Spacer(Modifier.height(6.dp))
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = scheme.surfaceVariant) {
                        Column {
                            Row(Modifier.fillMaxWidth().background(scheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Code, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(part.language.uppercase(), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            }
                            Text(part.code, modifier = Modifier.padding(12.dp).horizontalScroll(rememberScrollState()), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

private sealed class RichPart {
    data class Text(val text: String) : RichPart()
    data class CodeBlock(val language: String, val code: String) : RichPart()
}

private fun parseRichText(text: String): List<RichPart> {
    val parts = mutableListOf<RichPart>()
    val regex = Regex("```(\\w*)\\n?([\\s\\S]*?)```")
    var last = 0
    regex.findAll(text).forEach { match ->
        if (match.range.first > last) parts.add(RichPart.Text(text.substring(last, match.range.first)))
        parts.add(RichPart.CodeBlock(match.groupValues[1].ifEmpty { "code" }, match.groupValues[2].trim()))
        last = match.range.last + 1
    }
    if (last < text.length) parts.add(RichPart.Text(text.substring(last)))
    return parts
}

@Composable
fun ThinkingIndicator() {
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val alpha by animateFloatAsState(targetValue = 0.4f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = i * 150), repeatMode = RepeatMode.Reverse), label = "dot")
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(50.dp)).background(scheme.primary.copy(alpha = alpha)))
            Spacer(Modifier.width(4.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text("Thinking...", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
    }
}

@Composable
fun ModelPickerSheet(activeModel: String, vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val grouped = availableModels.groupBy { it.provider }

    AlertDialog(
        onDismissRequest = { vm.toggleModelPicker() },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = scheme.surfaceContainerHigh,
        title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Bolt, null, tint = scheme.primary); Spacer(Modifier.width(8.dp)); Text("Select Model", fontWeight = FontWeight.Bold) } },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                grouped.forEach { (provider, models) ->
                    Text(provider, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
                    models.forEach { model ->
                        val isActive = model.id == activeModel
                        Surface(
                            onClick = { vm.setActiveModel(model.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHighest,
                            border = if (isActive) BorderStroke(1.dp, scheme.primary) else null,
                        ) {
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
            }
        },
        confirmButton = { M3ETextButton(onClick = { vm.toggleModelPicker() }) { Text("Close") } },
    )
}

@Composable
fun SessionDrawerSheet(state: AppState, vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val pinned = state.sessions.filter { it.pinned }
    val unpinned = state.sessions.filter { !it.pinned }

    AlertDialog(
        onDismissRequest = { vm.toggleSessionDrawer() },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = scheme.surfaceContainerHigh,
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Chat, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sessions", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text("(${state.sessions.size})", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                }
                M3ETonalButton(onClick = { vm.createSession() }, icon = Icons.Filled.Add) { Text("New") }
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (pinned.isNotEmpty()) {
                    Text("Pinned", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 4.dp))
                    pinned.forEach { session -> SessionRow(session, state.activeSessionId, vm) }
                    Spacer(Modifier.height(8.dp))
                }
                Text("Recent", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                unpinned.forEach { session -> SessionRow(session, state.activeSessionId, vm) }
            }
        },
        confirmButton = { M3ETextButton(onClick = { vm.toggleSessionDrawer() }) { Text("Done") } },
    )
}

@Composable
private fun SessionRow(session: Session, activeId: String, vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val isActive = session.id == activeId
    val msgCount = session.messages.size
    Surface(onClick = { vm.setActiveSession(session.id) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHighest) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = if (isActive) scheme.primary else scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Chat, null, tint = if (isActive) scheme.onPrimary else scheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(session.name, style = MaterialTheme.typography.titleSmall, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal); Text("$msgCount messages", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant) }
            IconButton(onClick = { vm.pinSession(session.id) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.PushPin, "Pin", tint = if (session.pinned) scheme.primary else scheme.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
            if (session.id != com.opencode.app.data.Session().id) {
                IconButton(onClick = { vm.deleteSession(session.id) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, "Delete", tint = scheme.error, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}

