@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

data class FNode(val name: String, val path: String, val isDir: Boolean = false, val children: List<FNode> = emptyList())

val sampleTree = FNode("project", "/project", true, listOf(
    FNode("src", "/project/src", true, listOf(
        FNode("App.kt", "/project/src/App.kt"),
        FNode("MainActivity.kt", "/project/src/MainActivity.kt"),
        FNode("ui", "/project/src/ui", true, listOf(
            FNode("OpenCodeApp.kt", "/project/src/ui/OpenCodeApp.kt"),
            FNode("Theme.kt", "/project/src/ui/Theme.kt"),
            FNode("screens", "/project/src/ui/screens", true, listOf(
                FNode("ChatScreen.kt", "/project/src/ui/screens/ChatScreen.kt"),
                FNode("FilesScreen.kt", "/project/src/ui/screens/FilesScreen.kt"),
                FNode("TerminalScreen.kt", "/project/src/ui/screens/TerminalScreen.kt"),
                FNode("SettingsScreen.kt", "/project/src/ui/screens/SettingsScreen.kt"),
            )),
            FNode("components", "/project/src/ui/components", true, listOf(
                FNode("M3EButtons.kt", "/project/src/ui/components/M3EButtons.kt"),
                FNode("BottomNavBar.kt", "/project/src/ui/components/BottomNavBar.kt"),
            )),
        )),
        FNode("data", "/project/src/data", true, listOf(FNode("Models.kt", "/project/src/data/Models.kt"))),
        FNode("viewmodel", "/project/src/viewmodel", true, listOf(FNode("AppViewModel.kt", "/project/src/viewmodel/AppViewModel.kt"))),
    )),
    FNode("build.gradle.kts", "/project/build.gradle.kts"),
    FNode("settings.gradle.kts", "/project/settings.gradle.kts"),
    FNode("README.md", "/project/README.md"),
))

val fileContents = mapOf(
    "/project/src/ui/screens/ChatScreen.kt" to "package com.opencode.app.ui.screens\n\n@Composable\nfun ChatScreen(vm: AppViewModel, state: AppState) {\n    val messages = state.activeSession?.messages ?: emptyList()\n    LazyColumn(modifier = Modifier.weight(1f)) {\n        items(messages) { msg -> ChatBubble(msg, msg.role == Role.USER) }\n    }\n}",
    "/project/README.md" to "# OpenCode Android\n\nMaterial 3 Expressive phone app.\n\n## Features\n- AI Chat with streaming\n- Multi-session\n- File explorer\n- Terminal\n- 10 model providers\n\n## Build\n```bash\n./gradlew assembleDebug\n```",
    "/project/build.gradle.kts" to "plugins {\n    id(\"com.android.application\") version \"9.1.0\" apply false\n    kotlin(\"plugin.compose\") version \"2.2.20\"\n    kotlin(\"plugin.serialization\") version \"2.2.20\"\n}",
)

@Composable
fun FilesScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val expanded = remember { mutableStateMapOf("/project/src" to true, "/project/src/ui" to true, "/project/src/ui/screens" to true) }
    var selected by remember { mutableStateOf("/project/src/ui/screens/ChatScreen.kt") }
    var content by remember { mutableStateOf(fileContents["/project/src/ui/screens/ChatScreen.kt"] ?: "") }
    var showSplit by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Surface(Modifier.fillMaxWidth(), color = scheme.surface) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.toggleSessionDrawer() }) { Icon(Icons.Filled.Menu, "Menu") }
                Text("Files", modifier = Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { showSplit = !showSplit }) { Icon(if (showSplit) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, "Split") }
            }
        }

        Row(Modifier.fillMaxSize()) {
            // File tree
            Surface(Modifier.width(if (showSplit) 140.dp else 260.dp).fillMaxHeight(), color = scheme.surface) {
                LazyColumn {
                    item { Text("File Explorer", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(12.dp)) }
                    items(sampleTree.children) { node -> FileNodeRow(node, 0, selected, expanded) { path, text -> selected = path; content = text } }
                }
            }

            // Editor
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Surface(Modifier.fillMaxWidth(), color = scheme.surfaceContainerHigh) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        val ext = selected.substringAfterLast('.', "kt")
                        Surface(shape = RoundedCornerShape(4.dp), color = scheme.primaryContainer) {
                            Text(ext, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.onPrimaryContainer)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(selected.substringAfterLast('/'), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                }
                val lines = content.lines()
                val scrollState = rememberScrollState()
                Row(Modifier.fillMaxSize().background(scheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Column(Modifier.width(36.dp).fillMaxHeight().verticalScroll(scrollState).background(scheme.surfaceVariant).padding(vertical = 8.dp)) {
                        lines.forEachIndexed { i, _ -> Text("${i + 1}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp), color = scheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp)) }
                    }
                    Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(scrollState).padding(8.dp)) {
                        Text(content, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp), color = scheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun FileNodeRow(node: FNode, level: Int, selected: String, expanded: MutableMap<String, Boolean>, onSelect: (String, String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val pad = 12 + level * 18

    if (node.isDir) {
        val isExpanded = expanded[node.path] == true
        val isSelected = selected == node.path
        Column {
            Surface(onClick = { expanded[node.path] = !isExpanded }, modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
                Row(Modifier.padding(start = pad.dp, end = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowRight, null, modifier = Modifier.size(16.dp), tint = scheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Icon(if (isExpanded) Icons.Filled.FolderOpen else Icons.Filled.Folder, null, modifier = Modifier.size(16.dp), tint = scheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(node.name, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (isExpanded) node.children.forEach { FileNodeRow(it, level + 1, selected, expanded, onSelect) }
        }
    } else {
        val isSel = selected == node.path
        Surface(onClick = { onSelect(node.path, fileContents[node.path] ?: "// File: ${node.path}") }, modifier = Modifier.fillMaxWidth(), color = if (isSel) scheme.primaryContainer else Color.Transparent) {
            Row(Modifier.padding(start = (pad + 20).dp, end = 12.dp, top = 5.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.InsertDriveFile, null, modifier = Modifier.size(14.dp), tint = if (isSel) scheme.primary else scheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(node.name, style = MaterialTheme.typography.bodySmall, fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal)
            }
        }
    }
}
