package com.opencode.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.opencode.app.data.Screen
import com.opencode.app.ui.components.BottomNavBar
import com.opencode.app.ui.screens.*
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OpenCodeApp(vm: AppViewModel, state: AppState) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state.currentScreen,
                transitionSpec = {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut())
                },
                label = "screen",
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(vm, state)
                    Screen.CHAT -> ChatScreen(vm, state)
                    Screen.FILES -> FilesScreen(vm, state)
                    Screen.TERMINAL -> TerminalScreen(vm, state)
                    Screen.SETTINGS -> SettingsScreen(vm, state)
                }
            }
            BottomNavBar(
                currentScreen = state.currentScreen,
                onScreenSelected = { vm.setScreen(it) },
                modifier = Modifier
                    .matchParentSize()
                    .padding(bottom = androidx.compose.ui.unit.dp.times(0)),
                bottomInset = 0,
            )
        }
    }
}
