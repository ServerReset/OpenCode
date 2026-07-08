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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.data.FileTreeEntry
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun FilesScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    var selected by remember { mutableStateOf<String?>(null) }
    var content by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Surface(Modifier.fillMaxWidth(), color = scheme.surface) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.toggleSessionDrawer() }) { Icon(Icons.Filled.Menu, "Menu") }
                Text("Files", modifier = Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (!state.isConnected) {
                    Surface(shape = RoundedCornerShape(50), color = scheme.errorContainer) {
                        Text("OFFLINE", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.error)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
        }

        if (state.fileTree.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CloudOff, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No files available", style = MaterialTheme.typography.titleMedium, color = scheme.onSurfaceVariant)
                    Text("Connect to a server in Settings", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                // Tree
                Surface(Modifier.width(260.dp).fillMaxHeight(), color = scheme.surface) {
                    LazyColumn {
                        item { Text("File Explorer", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(12.dp)) }
                        items(state.fileTree) { node ->
                            FileNodeRow(node, 0, selected, expanded, onSelect = { path, text ->
                                selected = path
                                content = text
                                // The actual content should be fetched from server
                                // For now use the path as placeholder
                            })
                        }
                    }
                }

                // Content
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    selected?.let { sel ->
                        Surface(Modifier.fillMaxWidth(), color = scheme.surfaceContainerHigh) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                val ext = sel.substringAfterLast('.', "kt")
                                Surface(shape = RoundedCornerShape(4.dp), color = scheme.primaryContainer) {
                                    Text(ext, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.onPrimaryContainer)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(sel.substringAfterLast('/'), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Box(Modifier.fillMaxSize().background(scheme.surfaceVariant.copy(alpha = 0.3f)).padding(12.dp)) {
                        Text(selected?.let { content.ifEmpty { "// Select a file from the tree\n// Server file content will appear here" } }
                            ?: "Select a file to view", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp), color = scheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun FileNodeRow(node: FileTreeEntry, level: Int, selected: String?, expanded: MutableMap<String, Boolean>, onSelect: (String, String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val pad = 12 + level * 18

    if (node.isDirectory) {
        val isExpanded = expanded[node.path] == true
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
        Surface(onClick = { onSelect(node.path, node.path) }, modifier = Modifier.fillMaxWidth(), color = if (isSel) scheme.primaryContainer else Color.Transparent) {
            Row(Modifier.padding(start = (pad + 20).dp, end = 12.dp, top = 5.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.InsertDriveFile, null, modifier = Modifier.size(14.dp), tint = if (isSel) scheme.primary else scheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(node.name, style = MaterialTheme.typography.bodySmall, fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal)
            }
        }
    }
}
