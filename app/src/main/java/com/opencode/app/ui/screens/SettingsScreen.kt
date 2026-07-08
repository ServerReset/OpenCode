package com.opencode.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencode.app.data.availableModels
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    var darkMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Header
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
                    "Settings",
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Appearance
        SectionHeader("Appearance")

        SettingsGroup {
            SettingsRow(
                icon = Icons.Filled.DarkMode,
                label = "Dark Mode",
                description = "Toggle between light and dark theme",
                trailing = {
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it },
                    )
                },
            )
            SettingsRow(
                icon = Icons.Filled.Palette,
                label = "Dynamic Color",
                description = "Use Material You wallpaper colors",
                trailing = {
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                    )
                },
            )
        }

        Spacer(Modifier.height(16.dp))

        // AI & Models
        SectionHeader("AI & Models")

        SettingsGroup {
            SettingsRow(
                icon = Icons.Filled.Bolt,
                label = "Default Model",
                description = availableModels.find { it.id == state.activeModel }?.name ?: "Claude Sonnet 4",
                onClick = { vm.toggleModelPicker() },
            )
            SettingsRow(
                icon = Icons.Filled.AccountCircle,
                label = "GitHub Copilot",
                description = "Connect your Copilot account",
                onClick = {},
            )
            SettingsRow(
                icon = Icons.Filled.Link,
                label = "Custom API Endpoint",
                description = "Connect any provider via API",
                onClick = {},
            )
        }

        Spacer(Modifier.height(16.dp))

        // Sessions
        SectionHeader("Sessions (${state.sessions.size})")

        SettingsGroup {
            SettingsRow(
                icon = Icons.Filled.Delete,
                label = "Clear Current Session",
                description = "${state.activeSession?.messages?.size ?: 0} messages",
                trailing = {
                    FilledTonalButton(
                        onClick = { vm.clearSession() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = scheme.errorContainer,
                            contentColor = scheme.onErrorContainer,
                        ),
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelMedium)
                    }
                },
            )
        }

        Spacer(Modifier.height(16.dp))

        // Privacy & About
        SectionHeader("Privacy & About")

        SettingsGroup {
            SettingsRow(
                icon = Icons.Filled.Shield,
                label = "Privacy Notice",
                description = "We don't store your code or context data",
                onClick = {},
            )
            SettingsRow(
                icon = Icons.Filled.Info,
                label = "About OpenCode",
                description = "Version 0.1 (Material 3 Expressive)",
                onClick = {},
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "OpenCode Phone · Built with Jetpack Compose · Material 3 Expressive",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp),
        )

        if (state.showModelPicker) {
            ModelPickerDialog(
                activeModel = state.activeModel,
                onSelect = { vm.setActiveModel(it) },
                onDismiss = { vm.toggleModelPicker() },
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick ?: {},
        modifier = Modifier.fillMaxWidth(),
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = scheme.surfaceVariant,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                if (description != null) {
                    Text(
                        description,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
