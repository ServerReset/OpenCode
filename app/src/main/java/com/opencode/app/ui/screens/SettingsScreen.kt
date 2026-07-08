@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun SettingsScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    var serverUrlInput by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }
    var passwordInput by remember(state.password) { mutableStateOf(state.password) }
    var showPassword by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Spacer(Modifier.height(24.dp))

        Text("Server", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        Surface(shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(value = serverUrlInput, onValueChange = { serverUrlInput = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Server URL") }, placeholder = { Text("http://192.168.1.100:4096") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    textStyle = MaterialTheme.typography.bodyMedium, shape = RoundedCornerShape(12.dp), enabled = !state.isConnecting)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = passwordInput, onValueChange = { passwordInput = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Password (optional)") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), enabled = !state.isConnecting, textStyle = MaterialTheme.typography.bodyMedium,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null) } })
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = { vm.setServerUrl(serverUrlInput, passwordInput) }, enabled = !state.isConnecting && serverUrlInput.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    if (state.isConnecting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) else Text("Connect")
                }
                if (state.connectionError != null) { Spacer(Modifier.height(8.dp)); Text(state.connectionError!!, style = MaterialTheme.typography.labelSmall, color = scheme.error) }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Appearance", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        Surface(shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.DarkMode, null, tint = scheme.primary, modifier = Modifier.size(20.dp)) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text("Dark Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium); Text("Toggle light/dark theme", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant) }
                Switch(checked = state.isDarkMode, onCheckedChange = { vm.toggleDarkMode() })
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("About", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        Surface(shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = scheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Info, null, tint = scheme.primary, modifier = Modifier.size(20.dp)) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text("OpenCode Phone", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium); Text("M3 Expressive · Connect to your server", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant) }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}
