package com.opencode.app.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel
import com.opencode.app.viewmodel.TerminalEntry

private fun simulateCommand(cmd: String): List<TerminalEntry> {
    val trimmed = cmd.trim()
    val entries = mutableListOf(TerminalEntry("command", trimmed))

    when {
        trimmed == "clear" -> return listOf(TerminalEntry("output", "Terminal cleared"))
        trimmed == "ls" || trimmed == "ls -la" -> entries.add(
            TerminalEntry("output", """total 48
drwxr-xr-x  12 user  staff   384 Mar 15 10:30 .
drwxr-xr-x   5 user  staff   160 Mar 15 10:29 ..
-rw-r--r--   1 user  staff   423 Mar 15 10:29 MainActivity.kt
drwxr-xr-x   3 user  staff    96 Mar 15 10:29 ui
-rw-r--r--   1 user  staff   512 Mar 15 10:29 build.gradle.kts
-rw-r--r--   1 user  staff   847 Mar 15 10:29 README.md
drwxr-xr-x   4 user  staff   128 Mar 15 10:29 src""")
        )
        trimmed == "pwd" -> entries.add(TerminalEntry("output", "/home/admins/projects/OpenCode"))
        trimmed.startsWith("echo ") -> entries.add(TerminalEntry("output", trimmed.removePrefix("echo ").trim()))
        trimmed == "whoami" -> entries.add(TerminalEntry("output", "developer"))
        trimmed == "date" -> entries.add(TerminalEntry("output", java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").format(java.util.Date())))
        trimmed == "git status" -> entries.add(TerminalEntry("output", """On branch main
Your branch is up to date with 'origin/main'.

Changes not staged for commit:
  modified:   app/src/main/java/com/opencode/app/ui/screens/ChatScreen.kt
  modified:   app/src/main/java/com/opencode/app/ui/screens/FilesScreen.kt

no changes added to commit"""))
        trimmed.startsWith("npm run ") -> entries.add(TerminalEntry("output", """
> opencode@1.0.0 ${trimmed.removePrefix("npm run ")}
> vite

  VITE v6.3.4  ready in 234 ms
  ➜  Local:   http://localhost:5173/
  ➜  Network: http://192.168.1.100:5173/"""))
        trimmed == "help" -> entries.add(TerminalEntry("output", """Available commands:
  ls          - List directory contents
  pwd         - Print working directory
  echo        - Display message
  cat         - Display file contents
  whoami      - Display current user
  date        - Display current date
  clear       - Clear terminal
  git status  - Show git status
  npm run     - Run npm scripts
  help        - Show this help"""))
        else -> entries.add(TerminalEntry("error", "command not found: $trimmed"))
    }
    return entries
}

@Composable
fun TerminalScreen(vm: AppViewModel, state: AppState) {
    val listState = rememberLazyListState()
    val inputFocus = remember { FocusRequester() }

    LaunchedEffect(state.terminalHistory.size) {
        if (state.terminalHistory.isNotEmpty()) {
            listState.animateScrollToItem(state.terminalHistory.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        inputFocus.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFF0D1117)),
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF161B22),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Terminal,
                        contentDescription = null,
                        tint = Color(0xFF58A6FF),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Terminal",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFFE6EDF3),
                        ),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(onClick = { vm.clearTerminal() }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Clear", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8B949E)))
                }
            }
        }

        // Terminal output
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Flatten entries into individual lines
            val allLines = state.terminalHistory.flatMap { entry ->
                if (entry.type == "clear") return@flatMap emptyList()
                entry.text.split("\n").map { line ->
                    Triple(entry.type, entry.text, line)
                }
            }

            items(allLines) { (type, fullText, line) ->
                val isCommand = type == "command" && fullText == line
                Row(
                    modifier = Modifier.padding(vertical = 1.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        if (isCommand) "❯" else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF58A6FF),
                        ),
                        modifier = Modifier.width(16.dp),
                    )
                    Text(
                        line,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = when (type) {
                                "command" -> Color(0xFFE6EDF3)
                                "error" -> Color(0xFFF85149)
                                else -> Color(0xFF8B949E)
                            },
                        ),
                    )
                }
            }
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "❯",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color(0xFF58A6FF),
                ),
                modifier = Modifier.padding(end = 8.dp),
            )
            var inputText by remember { mutableStateOf(TextFieldValue("")) }
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(inputFocus)
                    .onKeyEvent { event ->
                        if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                            event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                            val cmd = inputText.text
                            if (cmd.isNotBlank()) {
                                simulateCommand(cmd).forEach { vm.addTerminalEntry(it) }
                                vm.addTerminalCommand(cmd)
                                inputText = TextFieldValue("")
                            }
                            true
                        } else false
                    },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color(0xFFE6EDF3),
                    lineHeight = 20.sp,
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF58A6FF)),
                decorationBox = { innerTextField ->
                    if (inputText.text.isEmpty()) {
                        Text(
                            "Enter command...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = Color(0xFF484F58),
                            ),
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}
