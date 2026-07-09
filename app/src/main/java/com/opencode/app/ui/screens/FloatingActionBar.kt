package com.opencode.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun FloatingActionBar(
    state: AppState,
    vm: AppViewModel,
    modifier: Modifier = Modifier,
    onToggleServer: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        // Expanded panel
        AnimatedVisibility(visible = expanded, enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(), exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = scheme.surfaceContainerHigh,
                shadowElevation = 6.dp,
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Actions", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                    // New chat
                    ActionRow(Icons.Default.Add, "New Chat", "Start a new conversation") { vm.createSession(); expanded = false }
                    // Model
                    ActionRow(Icons.Default.Bolt, "Model", state.activeModel) { vm.toggleModelPicker() }
                    // Server
                    ActionRow(Icons.Default.Cloud, "Server", state.serverUrl) { onToggleServer(); expanded = false }
                    // Account
                    ActionRow(Icons.Default.AccountCircle, "Account", state.account?.plan ?: "Not logged in") { vm.fetchAccount(); expanded = false }
                    // Dark mode
                    ActionRow(if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, "Theme", if (state.isDarkMode) "Dark" else "Light") { vm.toggleDarkMode() }
                }
            }
        }
        // FAB
        Surface(
            onClick = { expanded = !expanded },
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(50), clip = false).size(56.dp),
            shape = RoundedCornerShape(50),
            color = scheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                Icon(if (expanded) Icons.Default.Close else Icons.Default.Menu, "Menu", tint = scheme.onPrimary, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), shape = RoundedCornerShape(12.dp), color = Color.Transparent) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text(value, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1)
            }
            Icon(Icons.Default.ChevronRight, null, tint = scheme.outline, modifier = Modifier.size(16.dp))
        }
    }
}
