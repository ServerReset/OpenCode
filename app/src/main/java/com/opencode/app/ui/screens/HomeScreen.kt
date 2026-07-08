@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen
import com.opencode.app.ui.components.M3EMorphButton
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(32.dp))

        // Logo
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = scheme.primaryContainer,
                modifier = Modifier.size(88.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Shield, null, tint = scheme.primary, modifier = Modifier.size(44.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("OpenCode", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = scheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text("Your AI coding agent, on mobile.", style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.extraSmall, color = scheme.primaryContainer) {
                    Text("M3 Expressive", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Quick actions grid
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple(Icons.Filled.Chat, "New Chat", { vm.createSession(); vm.setScreen(Screen.CHAT) }),
                Triple(Icons.Filled.SwapHoriz, "Multi", { vm.toggleSessionDrawer() }),
                Triple(Icons.Filled.Terminal, "Terminal", { vm.setScreen(Screen.TERMINAL) }),
                Triple(Icons.Filled.Settings, "Settings", { vm.setScreen(Screen.SETTINGS) }),
            ).forEach { (icon, label, onClick) ->
                Surface(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = scheme.surfaceContainerHigh,
                ) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Feature cards
        SectionHeader("Features", scheme)
        Spacer(Modifier.height(10.dp))

        FeatureCard(Icons.Filled.Chat, "AI Chat", "Streaming responses, code blocks, 75+ LLM providers across 7 platforms", scheme.primary, onClick = { vm.setScreen(Screen.CHAT) })
        Spacer(Modifier.height(8.dp))
        FeatureCard(Icons.Filled.Layers, "Multi-Session", "Run parallel agents, pin favorites, share session links", scheme.primary, onClick = { vm.toggleSessionDrawer() })
        Spacer(Modifier.height(8.dp))
        FeatureCard(Icons.Filled.Bolt, "Any Model", "Claude, GPT, Gemini, DeepSeek, Llama, Codestral, GitHub Copilot", scheme.secondary, onClick = { vm.toggleModelPicker() })
        Spacer(Modifier.height(8.dp))

        Spacer(Modifier.height(8.dp))

        // Privacy card
        Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = scheme.primaryContainer) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shield, null, tint = scheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Privacy First", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onPrimaryContainer)
                    Text("We don't store your code or context data.", style = MaterialTheme.typography.bodySmall, color = scheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SectionHeader(title: String, scheme: ColorScheme) {
    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.primary)
}

@Composable
private fun FeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String, color: Color, onClick: () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerHigh, onClick = onClick) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = color.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}
