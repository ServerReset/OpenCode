package com.opencode.app.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean = false,
    val children: List<FileNode> = emptyList(),
)

val sampleFileTree = FileNode(
    name = "project", path = "/project", isDirectory = true,
    children = listOf(
        FileNode(name = "src", path = "/project/src", isDirectory = true, children = listOf(
            FileNode(name = "App.kt", path = "/project/src/App.kt"),
            FileNode(name = "MainActivity.kt", path = "/project/src/MainActivity.kt"),
            FileNode(name = "ui", path = "/project/src/ui", isDirectory = true, children = listOf(
                FileNode(name = "OpenCodeApp.kt", path = "/project/src/ui/OpenCodeApp.kt"),
                FileNode(name = "theme", path = "/project/src/ui/theme", isDirectory = true, children = listOf(
                    FileNode(name = "Theme.kt", path = "/project/src/ui/theme/Theme.kt"),
                )),
                FileNode(name = "screens", path = "/project/src/ui/screens", isDirectory = true, children = listOf(
                    FileNode(name = "ChatScreen.kt", path = "/project/src/ui/screens/ChatScreen.kt"),
                    FileNode(name = "FilesScreen.kt", path = "/project/src/ui/screens/FilesScreen.kt"),
                    FileNode(name = "TerminalScreen.kt", path = "/project/src/ui/screens/TerminalScreen.kt"),
                )),
            )),
            FileNode(name = "data", path = "/project/src/data", isDirectory = true, children = listOf(
                FileNode(name = "Models.kt", path = "/project/src/data/Models.kt"),
            )),
            FileNode(name = "viewmodel", path = "/project/src/viewmodel", isDirectory = true, children = listOf(
                FileNode(name = "AppViewModel.kt", path = "/project/src/viewmodel/AppViewModel.kt"),
            )),
        )),
        FileNode(name = "build.gradle.kts", path = "/project/build.gradle.kts"),
        FileNode(name = "settings.gradle.kts", path = "/project/settings.gradle.kts"),
        FileNode(name = "AndroidManifest.xml", path = "/project/AndroidManifest.xml"),
        FileNode(name = "README.md", path = "/project/README.md"),
    ),
)

val sampleFileContents = mapOf(
    "/project/src/ui/screens/ChatScreen.kt" to """package com.opencode.app.ui.screens

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChatScreen(vm: AppViewModel, state: AppState) {
    val messages = state.activeSession?.messages ?: emptyList()
    Column(modifier = Modifier.fillMaxSize()) {
        // Chat messages
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg ->
                ChatBubble(message = msg, isUser = msg.role == Role.USER)
            }
        }
        // Input bar
        ChatInputBar(
            onSend = { text -> vm.sendMessage(text) },
            enabled = !state.isStreaming,
        )
    }
}""",
    "/project/README.md" to """# OpenCode Android

Material 3 Expressive phone app for the OpenCode AI coding agent.

Built with Jetpack Compose and Material 3 Expressive.

## Features
- AI-powered chat interface
- Multi-session support
- File browser & code editor
- Built-in terminal
- Model selection with 75+ providers
- Share session links

## Tech Stack
- Kotlin 2.2.20
- Jetpack Compose 1.12
- Material 3 Expressive
- AGP 9.1.0
""",
    "/project/build.gradle.kts" to """plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.opencode.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.opencode.app"
        minSdk = 26
        targetSdk = 36
    }
}""",
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilesScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    var showSplitView by remember { mutableStateOf(false) }
    var selectedPath by remember { mutableStateOf("/project/src/ui/screens/ChatScreen.kt") }
    var fileContent by remember { mutableStateOf(sampleFileContents[selectedPath] ?: "") }

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
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
                Text(
                    "Files",
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = { showSplitView = !showSplitView }) {
                    Icon(
                        if (showSplitView) Icons.Filled.Fullscreen else Icons.Filled.Fullscreen,
                        contentDescription = "Toggle split view",
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // File tree
            if (!showSplitView || showSplitView) {
                Surface(
                    modifier = Modifier
                        .width(if (showSplitView) 160.dp else 280.dp)
                        .fillMaxHeight(),
                    color = scheme.surface,
                ) {
                    LazyColumn {
                        item {
                            Text(
                                "File Explorer",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.primary,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                        items(sampleFileTree.children) { node ->
                            FileTreeNode(
                                node = node,
                                level = 0,
                                selectedPath = selectedPath,
                                expandedPaths = remember {
                                    mutableStateMapOf(
                                        "/project/src" to true,
                                        "/project/src/ui" to true,
                                    )
                                },
                                onSelect = { path ->
                                    selectedPath = path
                                    fileContent = sampleFileContents[path] ?: "// File: $path"
                                },
                            )
                        }
                    }
                }
            }

            // Code editor
            if (!showSplitView || showSplitView) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    // File header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = scheme.surfaceContainerHigh,
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val ext = selectedPath.substringAfterLast('.', "kt")
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = scheme.primaryContainer,
                            ) {
                                Text(
                                    ext,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onPrimaryContainer,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                selectedPath.substringAfterLast('/'),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = {}) {
                                Text("Save", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    // Code content with line numbers
                    val lines = fileContent.lines()
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .width(40.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .background(scheme.surfaceVariant)
                                .padding(vertical = 8.dp),
                        ) {
                            lines.forEachIndexed { i, _ ->
                                Text(
                                    "${i + 1}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                    ),
                                    color = scheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(8.dp),
                        ) {
                            Text(
                                fileContent,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 20.sp,
                                ),
                                color = scheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileTreeNode(
    node: FileNode,
    level: Int,
    selectedPath: String,
    expandedPaths: MutableMap<String, Boolean>,
    onSelect: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val isExpanded = expandedPaths[node.path] == true
    val isSelected = selectedPath == node.path
    val paddingStart = 12 + level * 20

    if (node.isDirectory) {
        Column {
            Surface(
                onClick = { expandedPaths[node.path] = !isExpanded },
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = paddingStart.dp,
                        end = 12.dp,
                        top = 6.dp,
                        bottom = 6.dp,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = scheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Filled.FolderOpen else Icons.Filled.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = scheme.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        node.name,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (isExpanded) {
                node.children.forEach { child ->
                    FileTreeNode(child, level + 1, selectedPath, expandedPaths, onSelect)
                }
            }
        }
    } else {
        Surface(
            onClick = { onSelect(node.path) },
            modifier = Modifier.fillMaxWidth(),
            color = if (isSelected) scheme.primaryContainer else Color.Transparent,
        ) {
            Row(
                modifier = Modifier.padding(
                    start = (paddingStart + 20).dp,
                    end = 12.dp,
                    top = 5.dp,
                    bottom = 5.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isSelected) scheme.primary else scheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    node.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }
    }
}
