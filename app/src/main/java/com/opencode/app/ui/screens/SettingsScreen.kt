package com.opencode.app.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
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
    var apiKeyInput by remember(state.apiKey) { mutableStateOf(state.apiKey) }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Spacer(Modifier.height(24.dp))

        // Server
        SettingsHeader("Server")
        Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
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
                Button(onClick = { vm.setServerUrl(urlInput, passInput) }, enabled = !state.isConnecting && urlInput.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    if (state.isConnecting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = scheme.onPrimary)
                    else Text("Connect")
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(if (state.isConnected) scheme.primary else scheme.error))
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isConnected) "Connected" else "Not connected", style = MaterialTheme.typography.labelSmall, color = if (state.isConnected) scheme.primary else scheme.error)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // OpenCode Account
        SettingsHeader("OpenCode Account")
        Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(value = apiKeyInput, onValueChange = { apiKeyInput = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("API Key") }, placeholder = { Text("oc_...") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = { vm.setApiKey(apiKeyInput) }, enabled = apiKeyInput.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Save Key")
                }
            }
        }

        // Account info
        AnimatedVisibility(visible = state.account != null, enter = expandVertically() + fadeIn()) {
            state.account?.let { acct ->
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = scheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, null, tint = scheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(acct.email.ifBlank { "OpenCode User" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${acct.plan} Plan", style = MaterialTheme.typography.labelSmall, color = scheme.onPrimaryContainer)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        // Monthly limit
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Monthly", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                            Text("${acct.monthlyUsed} / ${acct.monthlyLimit}", style = MaterialTheme.typography.labelMedium)
                        }
                        val mPct = if (acct.monthlyLimit > 0) (acct.monthlyUsed.toFloat() / acct.monthlyLimit).coerceIn(0f, 1f) else 0f
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(scheme.primaryContainer)) {
                            Box(Modifier.fillMaxWidth(mPct).fillMaxHeight().background(scheme.primary, RoundedCornerShape(3.dp)))
                        }
                        Spacer(Modifier.height(8.dp))
                        // Weekly limit
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Weekly", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                            Text("${acct.weeklyUsed} / ${acct.weeklyLimit}", style = MaterialTheme.typography.labelMedium)
                        }
                        val wPct = if (acct.weeklyLimit > 0) (acct.weeklyUsed.toFloat() / acct.weeklyLimit).coerceIn(0f, 1f) else 0f
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(scheme.primaryContainer)) {
                            Box(Modifier.fillMaxWidth(wPct).fillMaxHeight().background(scheme.primary, RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Appearance
        SettingsHeader("Appearance")
        Surface(shape = RoundedCornerShape(20.dp), color = scheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, null, tint = scheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text("Dark Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium); Text("Toggle theme", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant) }
                Switch(checked = state.isDarkMode, onCheckedChange = { vm.toggleDarkMode() })
            }
        }

        Spacer(Modifier.height(40.dp))
        Text("OpenCode Phone · M3 Expressive", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsHeader(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
}
