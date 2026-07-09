package com.opencode.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.opencode.app.data.Screen
import com.opencode.app.ui.components.AppNavBar
import com.opencode.app.ui.screens.*
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun OpenCodeApp(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(scheme.surfaceContainerHigh, scheme.surface, scheme.surfaceContainerLow)),
            ))

            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (state.currentScreen) {
                        Screen.HOME -> HomeScreen(vm, state)
                        Screen.CHAT -> ChatScreen(vm, state)
                        Screen.SETTINGS -> SettingsScreen(vm, state)
                    }
                }

                AppNavBar(
                    current = state.currentScreen,
                    onSelect = { vm.setScreen(it) },
                )
            }
        }
    }
}
