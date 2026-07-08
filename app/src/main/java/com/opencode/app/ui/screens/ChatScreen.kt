package com.opencode.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.data.Message
import com.opencode.app.data.ModelInfo
import com.opencode.app.data.Role
import com.opencode.app.data.availableModels
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChatScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val messages = state.activeSession?.messages ?: emptyList()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = scheme.surface,
            tonalElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { vm.toggleSessionDrawer() }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Sessions")
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Chat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        state.activeSession?.name ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { vm.toggleSessionDrawer() }) {
                    Icon(Icons.Filled.Add, contentDescription = "New")
                }
            }
        }

        // Model selector chip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = scheme.surface,
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val activeModel = availableModels.find { it.id == state.activeModel }
                SuggestionChip(
                    onClick = { vm.toggleModelPicker() },
                    label = {
                        Text(
                            activeModel?.name ?: "Select Model",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(activeModel?.color ?: 0xFF65558F),
                        )
                    },
                    shape = RoundedCornerShape(50),
                )
                if (messages.isNotEmpty()) {
                    TextButton(onClick = { vm.clearSession() }) {
                        Text("Clear", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Messages
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = scheme.primaryContainer,
                        modifier = Modifier.size(72.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Chat,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Start a conversation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ask anything about your code — debug issues,\nwrite features, or explain concepts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        isUser = msg.role == Role.USER,
                    )
                }

                if (state.isStreaming) {
                    item {
                        ThinkingIndicator()
                    }
                }

                if (state.error != null) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = scheme.errorContainer,
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = scheme.error,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    state.error ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chat input
        ChatInputBar(
            onSend = { text ->
                vm.setError(null)
                vm.sendMessage(text)
                scope.launch {
                    simulateResponse(text, vm)
                }
            },
            enabled = !state.isStreaming,
        )
    }

    // Model picker dialog
    if (state.showModelPicker) {
        ModelPickerDialog(
            activeModel = state.activeModel,
            onSelect = { vm.setActiveModel(it) },
            onDismiss = { vm.toggleModelPicker() },
        )
    }

    // Session drawer
    if (state.showSessionDrawer) {
        SessionDrawer(
            sessions = state.sessions,
            activeSessionId = state.activeSessionId,
            onSelect = { vm.setActiveSession(it) },
            onNew = { vm.createSession() },
            onDelete = { vm.deleteSession(it) },
            onPin = { vm.pinSession(it) },
            onDismiss = { vm.toggleSessionDrawer() },
        )
    }
}

@Composable
fun ChatBubble(message: Message, isUser: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        if (!isUser) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10),
                    color = scheme.primary,
                    modifier = Modifier.size(20.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "O",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimary,
                            fontSize = 11.sp,
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    "OpenCode",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        Surface(
            shape = if (isUser) {
                RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
            } else {
                RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
            },
            color = if (isUser) scheme.primaryContainer else scheme.surfaceContainerHigh,
            border = if (!isUser) ButtonDefaults.outlinedButtonBorder else null,
        ) {
            Text(
                text = formatMessageContent(message.content, scheme),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) scheme.onPrimaryContainer else scheme.onSurface,
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun formatMessageContent(content: String, scheme: androidx.compose.material3.ColorScheme): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val codeBlockRegex = Regex("```(\\w*)\\n?([\\s\\S]*?)```")
        var lastIndex = 0
        codeBlockRegex.findAll(content).forEach { match ->
            append(content.substring(lastIndex, match.range.first))
            withStyle(SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                background = scheme.surfaceVariant,
            )) {
                append("\n" + match.groupValues[2].trim() + "\n")
            }
            lastIndex = match.range.last + 1
        }
        append(content.substring(lastIndex))
    }
}

@Composable
fun ChatInputBar(onSend: (String) -> Unit, enabled: Boolean) {
    val scheme = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surface,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28),
                color = scheme.surfaceContainerHighest,
                border = ButtonDefaults.outlinedButtonBorder,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = "Attach",
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Ask anything about your code...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        enabled = enabled,
                    )
                    if (text.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSend(text)
                                text = ""
                            },
                            modifier = Modifier.size(40.dp),
                            enabled = enabled,
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Send",
                                tint = scheme.primary,
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = "Voice",
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$i",
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.primary.copy(alpha = alpha)),
            )
            Spacer(Modifier.width(3.dp))
        }
        Spacer(Modifier.width(6.dp))
        Text(
            "Thinking...",
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModelPickerDialog(
    activeModel: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val groupedModels = availableModels.groupBy { it.provider }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = scheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Select Model", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                groupedModels.forEach { (provider, models) ->
                    Text(
                        provider,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                    models.forEach { model ->
                        val isActive = model.id == activeModel
                        Surface(
                            onClick = { onSelect(model.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHigh,
                            border = if (isActive) ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(scheme.primary),
                            ) else null,
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10),
                                    color = Color(model.color).copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.Bolt,
                                            contentDescription = null,
                                            tint = Color(model.color),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(model.name, style = MaterialTheme.typography.titleSmall)
                                    Text(model.provider, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                                }
                                if (isActive) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = scheme.surfaceContainerHigh,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SessionDrawer(
    sessions: List<com.opencode.app.data.Session>,
    activeSessionId: String,
    onSelect: (String) -> Unit,
    onNew: () -> Unit,
    onDelete: (String) -> Unit,
    onPin: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val pinned = sessions.filter { it.pinned }
    val unpinned = sessions.filter { !it.pinned }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Sessions", fontWeight = FontWeight.Bold)
                FilledTonalButton(onClick = onNew) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (pinned.isNotEmpty()) {
                    Text(
                        "Pinned",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    pinned.forEach { session ->
                        SessionRow(session, activeSessionId == session.id, onSelect, onPin, onDelete)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    "Recent",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                unpinned.forEach { session ->
                    SessionRow(session, activeSessionId == session.id, onSelect, onPin, onDelete)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = scheme.surfaceContainerHigh,
    )
}

@Composable
private fun SessionRow(
    session: com.opencode.app.data.Session,
    isActive: Boolean,
    onSelect: (String) -> Unit,
    onPin: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val msgCount = session.messages.size
    Surface(
        onClick = { onSelect(session.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12),
                color = if (isActive) scheme.primary else scheme.surfaceVariant,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Chat, null, tint = if (isActive) scheme.onPrimary else scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                )
                Text(
                    "$msgCount messages",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onPin(session.id) }) {
                Icon(
                    Icons.Filled.PushPin,
                    contentDescription = "Pin",
                    tint = if (session.pinned) scheme.primary else scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            if (session.id != "default") {
                IconButton(onClick = { onDelete(session.id) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = scheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private suspend fun simulateResponse(prompt: String, vm: AppViewModel) {
    vm.setStreaming(true)
    val assistantId = java.util.UUID.randomUUID().toString()
    val lower = prompt.lowercase()

    val response = when {
        lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
            "Hey there! I'm OpenCode, your AI coding assistant. I can help you write, review, and understand code. What would you like to work on?"

        lower.contains("bug") || lower.contains("error") || lower.contains("fix") ->
            "I can help you debug that! Here's a common pattern to check:\n\n```kotlin\n// Check if state is being properly observed\nval state by viewModel.state.collectAsState()\n\n// Ensure recomposition triggers on change\nLaunchedEffect(state.error) {\n    state.error?.let { showSnackbar(it) }\n}\n```\n\nThe issue is often that state changes aren't triggering recomposition properly."

        lower.contains("code") || lower.contains("write") || lower.contains("create") ->
            "Here's a clean implementation:\n\n```kotlin\n@Composable\nfun AnimatedButton(\n    onClick: () -> Unit,\n    modifier: Modifier = Modifier,\n    enabled: Boolean = true,\n    content: @Composable RowScope.() -> Unit,\n) {\n    val interactionSource = remember { MutableInteractionSource() }\n    val isPressed by interactionSource.collectIsPressedAsState()\n    \n    val shape by animateShapeAsState(\n        if (isPressed) RoundedCornerShape(12.dp) \n        else RoundedCornerShape(50)\n    )\n    \n    Surface(\n        onClick = onClick,\n        modifier = modifier,\n        enabled = enabled,\n        shape = shape,\n        color = MaterialTheme.colorScheme.primary,\n    ) {\n        Row(\n            Modifier.padding(horizontal = 24.dp, vertical = 12.dp),\n            content = content,\n        )\n    }\n}\n```\n\nThis follows Material 3 Expressive conventions with animated shape morphing."

        lower.contains("explain") || lower.contains("what") ->
            "Great question! Here's how it works:\n\n**Architecture Overview:**\n- **ViewModel**: Holds all UI state using `StateFlow`\n- **Compose**: Observes state with `collectAsState()`\n- **M3 Expressive**: Uses `MaterialExpressiveTheme` with animated shapes\n\n```kotlin\n// State flows down, events flow up\nclass AppViewModel : ViewModel() {\n    private val _state = MutableStateFlow(AppState())\n    val state: StateFlow<AppState> = _state.asStateFlow()\n    \n    fun onEvent(event: AppEvent) {\n        _state.update { it.handleEvent(event) }\n    }\n}\n```"

        else ->
            "I understand you're asking about: \"$prompt\"\n\nI can help with:\n- **Code generation** — write new features and components\n- **Debugging** — find and fix issues in your code\n- **Code review** — analyze and improve your implementation\n- **Architecture** — design patterns and best practices\n\nWhat specifically would you like me to help with?"
    }

    val words = response.split(" ")
    var current = ""
    for (word in words) {
        current += if (current.isEmpty()) word else " $word"
        vm.updateAssistantMessage(assistantId, current)
        delay(20 + (Math.random() * 15).toLong())
    }
    vm.setStreaming(false)
}
