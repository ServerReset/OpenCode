@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun TerminalScreen(vm: AppViewModel, state: AppState) {
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var cmdHistory by remember { mutableStateOf(listOf<String>()) }
    var historyIdx by remember { mutableIntStateOf(-1) }

    val allLines = remember(state.terminalHistory) {
        state.terminalHistory.flatMap { e ->
            if (e.type == "clear") emptyList()
            else e.text.split("\n").map { line -> Triple(e.type, e.text, line) }
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().background(Color(0xFF0D1117))) {
        Surface(Modifier.fillMaxWidth(), color = Color(0xFF161B22)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Terminal, null, tint = Color(0xFF58A6FF), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Terminal", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFE6EDF3)), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!state.isConnected) {
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(50), color = Color(0xFFF85149).copy(alpha = 0.2f)) {
                            Text("DISCONNECTED", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFF85149)))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { vm.clearTerminal() }) {
                        Icon(Icons.Filled.Delete, null, tint = Color(0xFF8B949E), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8B949E)))
                    }
                }
            }
        }

        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
            itemsIndexed(allLines) { _, (type, _, line) ->
                Row(Modifier.padding(vertical = 1.dp), verticalAlignment = Alignment.Top) {
                    Text(if (type == "command") "❯" else "", modifier = Modifier.width(16.dp),
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFF58A6FF)))
                    Text(line, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp,
                        color = when (type) { "command" -> Color(0xFFE6EDF3); "error" -> Color(0xFFF85149); else -> Color(0xFF8B949E) }))
                }
            }
        }

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
                                if (cmd.trim().lowercase() == "clear") {
                                    vm.clearTerminal()
                                } else {
                                    vm.addTerminalCommand(cmd)
                                    vm.executeCommand(cmd)
                                }
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
                            if (historyIdx > 0) { historyIdx--; input = TextFieldValue(cmdHistory[cmdHistory.size - 1 - historyIdx]) }
                            else { historyIdx = -1; input = TextFieldValue("") }
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
