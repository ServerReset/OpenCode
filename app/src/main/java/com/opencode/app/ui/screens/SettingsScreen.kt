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
    var urlInput by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }
    var passInput by remember(state.password) { mutableStateOf(state.password) }
    var showPass by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Server", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(value = urlInput, onValueChange = { urlInput = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Server URL") }, singleLine = true, shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri), enabled = !state.isConnecting)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = passInput, onValueChange = { passInput = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") }, singleLine = true, shape = RoundedCornerShape(12.dp), enabled = !state.isConnecting,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton(onClick = { showPass = !showPass }) { Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } })
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = { vm.setServerUrl(urlInput, passInput) }, enabled = !state.isConnecting && urlInput.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    if (state.isConnecting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) else Text("Connect")
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Appearance", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, null, tint = scheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text("Dark Mode"); Text("Toggle theme", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant) }
                Switch(checked = state.isDarkMode, onCheckedChange = { vm.toggleDarkMode() })
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(if (state.isConnected) "✓ Connected" else "Not connected", style = MaterialTheme.typography.labelSmall, color = if (state.isConnected) scheme.primary else scheme.error)
        Spacer(Modifier.height(80.dp))
    }
}
