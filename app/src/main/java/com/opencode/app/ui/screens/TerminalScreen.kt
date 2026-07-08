@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel
import com.opencode.app.viewmodel.TerminalEntry

private fun execCommand(cmd: String): List<TerminalEntry> {
    val trimmed = cmd.trim().lowercase()
    val out = mutableListOf(TerminalEntry("command", cmd))
    when {
        trimmed == "clear" -> return listOf(TerminalEntry("system", "Terminal cleared"))
        trimmed == "ls" || trimmed == "ls -la" -> out.add(TerminalEntry("output", 
            "total 48\ndrwxr-xr-x  12 user  staff   384 Mar 15 10:30 .\ndrwxr-xr-x   5 user  staff   160 Mar 15 10:29 ..\n-rw-r--r--   1 user  staff   423 Mar 15 10:29 MainActivity.kt\ndrwxr-xr-x   3 user  staff    96 Mar 15 10:29 ui\n-rw-r--r--   1 user  staff   512 Mar 15 10:29 build.gradle.kts\ndrwxr-xr-x   6 user  staff   192 Mar 15 10:29 src"))
        trimmed == "pwd" -> out.add(TerminalEntry("output", "/home/admins/projects/OpenCode"))
        trimmed.startsWith("echo ") -> out.add(TerminalEntry("output", trimmed.removePrefix("echo ").trim()))
        trimmed == "whoami" -> out.add(TerminalEntry("output", "developer"))
        trimmed == "date" -> out.add(TerminalEntry("output", java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").format(java.util.Date())))
        trimmed == "git status" -> out.add(TerminalEntry("output", "On branch main\nYour branch is up to date with 'origin/main'.\n\nChanges not staged for commit:\n  modified:   app/src/main/java/com/opencode/app/ui/screens/ChatScreen.kt\n  modified:   app/src/main/java/com/opencode/app/ui/screens/FilesScreen.kt\n\nno changes added to commit"))
        trimmed.startsWith("npm run ") -> out.add(TerminalEntry("output", "\n> opencode@1.0.0 ${trimmed.removePrefix("npm run ")}\n> vite\n\n  VITE v6.3.4  ready\n  ➜  Local:   http://localhost:5173/"))
        trimmed.startsWith("cat ") -> out.add(TerminalEntry("output", "// File: ${trimmed.removePrefix("cat ").trim()}\n// (file contents would be displayed)"))
        trimmed == "help" -> out.add(TerminalEntry("output", "ls, pwd, echo, cat, whoami, date, clear, git status, npm run, help"))
        else -> out.add(TerminalEntry("error", "command not found: $cmd"))
    }
    return out
}

@Composable
fun TerminalScreen(vm: AppViewModel, state: AppState) {
    val listState = rememberLazyListState()
    val scroll = rememberScrollState()
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var cmdHistory by remember { mutableStateOf(listOf<String>()) }
    var historyIdx by remember { mutableIntStateOf(-1) }

    // Flatten for display
    val allLines = remember(state.terminalHistory) {
        state.terminalHistory.flatMap { e ->
            if (e.type == "clear") emptyList()
            else e.text.split("\n").map { line -> Triple(e.type, e.text, line) }
        }
    }

    LaunchedEffect(allLines.size) {
        if (allLines.isNotEmpty()) {
// Using a simple delay-based scroll
            kotlinx.coroutines.delay(50)
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().background(Color(0xFF0D1117))) {
        // Header
        Surface(Modifier.fillMaxWidth(), color = Color(0xFF161B22)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Terminal, null, tint = Color(0xFF58A6FF), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Terminal", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFE6EDF3)), fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = { vm.clearTerminal() }) {
                    Icon(Icons.Filled.Delete, null, tint = Color(0xFF8B949E), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8B949E)))
                }
            }
        }

        // Output
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            itemsIndexed(allLines) { _, (type, fullText, line) ->
                val isCmd = type == "command" && fullText == line
                Row(Modifier.padding(vertical = 1.dp), verticalAlignment = Alignment.Top) {
                    Text(if (isCmd) "❯" else "", modifier = Modifier.width(16.dp),
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFF58A6FF)))
                    Text(line, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp,
                        color = when (type) { "command" -> Color(0xFFE6EDF3); "error" -> Color(0xFFF85149); else -> Color(0xFF8B949E) }))
                }
            }
        }

        // Input
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            Text("❯", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFF58A6FF)), modifier = Modifier.padding(end = 8.dp))
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth()
                    .onPreviewKeyEvent { event ->
                        if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                            event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                            val cmd = input.text
                            if (cmd.isNotBlank()) {
                                execCommand(cmd).forEach { vm.addTerminalEntry(it) }
                                cmdHistory = cmdHistory + cmd
                                historyIdx = -1
                                input = TextFieldValue("")
                            }
                            true
                        } else if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP &&
                            event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                            if (cmdHistory.isNotEmpty() && historyIdx < cmdHistory.size - 1) {
                                val newIdx = if (historyIdx < 0) 0 else historyIdx + 1
                                historyIdx = newIdx
                                input = TextFieldValue(cmdHistory[cmdHistory.size - 1 - newIdx])
                            }
                            true
                        } else if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN &&
                            event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                            if (historyIdx > 0) {
                                historyIdx--
                                input = TextFieldValue(cmdHistory[cmdHistory.size - 1 - historyIdx])
                            } else {
                                historyIdx = -1
                                input = TextFieldValue("")
                            }
                            true
                        } else false
                    },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFFE6EDF3)),
                cursorBrush = SolidColor(Color(0xFF58A6FF)),
                decorationBox = { inner ->
                    if (input.text.isEmpty()) Text("Enter command...", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFF484F58)))
                    inner()
                },
            )
        }
    }
}
