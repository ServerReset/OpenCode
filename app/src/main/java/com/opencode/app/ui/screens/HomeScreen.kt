package com.opencode.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        // Logo and title
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = scheme.primaryContainer,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "OpenCode",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = scheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Your AI coding agent, now on mobile.",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Quick actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Chat,
                label = "New Chat",
                onClick = { vm.createSession(); vm.setScreen(Screen.CHAT) },
            )
            QuickActionChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Terminal,
                label = "Terminal",
                onClick = { vm.setScreen(Screen.TERMINAL) },
            )
            QuickActionChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Folder,
                label = "Files",
                onClick = { vm.setScreen(Screen.FILES) },
            )
            QuickActionChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Settings,
                label = "Settings",
                onClick = { vm.setScreen(Screen.SETTINGS) },
            )
        }

        Spacer(Modifier.height(24.dp))

        // Features
        SectionHeader("Features")
        Spacer(Modifier.height(8.dp))

        FeatureCard(
            icon = Icons.Filled.Chat,
            title = "AI Chat",
            description = "Intelligent coding agent powered by 75+ LLM providers",
            color = scheme.primary,
            onClick = { vm.setScreen(Screen.CHAT) },
        )
        Spacer(Modifier.height(8.dp))

        FeatureCard(
            icon = Icons.Filled.Folder,
            title = "File Explorer",
            description = "Browse, view, and edit project files with syntax highlighting",
            color = scheme.secondary,
            onClick = { vm.setScreen(Screen.FILES) },
        )
        Spacer(Modifier.height(8.dp))

        FeatureCard(
            icon = Icons.Filled.Terminal,
            title = "Terminal",
            description = "Execute commands and run scripts directly from your phone",
            color = scheme.tertiary,
            onClick = { vm.setScreen(Screen.TERMINAL) },
        )
        Spacer(Modifier.height(8.dp))

        FeatureCard(
            icon = Icons.Filled.Settings,
            title = "Multi-Session",
            description = "Run multiple parallel sessions with different models",
            color = scheme.primary,
            onClick = { vm.setScreen(Screen.SETTINGS) },
        )

        Spacer(Modifier.height(16.dp))

        // Privacy card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = scheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Privacy First",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                    )
                    Text(
                        "OpenCode does not store any of your code or context data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        // Extra padding for bottom nav
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun QuickActionChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = scheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = scheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurface,
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
    )
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
