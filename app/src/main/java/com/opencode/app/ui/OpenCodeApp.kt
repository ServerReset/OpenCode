@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen
import com.opencode.app.ui.components.ExpressiveNavBar
import com.opencode.app.ui.components.ExpressiveNavRail
import com.opencode.app.ui.screens.*
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel

@Composable
fun OpenCodeApp(vm: AppViewModel, state: AppState) {
    val scheme = MaterialTheme.colorScheme
    val config = LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(scheme.surfaceContainerHigh, scheme.surface, scheme.surfaceContainerLow)),
            ))

            Row(modifier = Modifier.fillMaxSize()) {
                if (isTablet) {
                    ExpressiveNavRail(
                        currentScreen = state.currentScreen,
                        onScreenSelected = { vm.setScreen(it) },
                    )
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = state.currentScreen,
                            transitionSpec = {
                                val sign = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                (slideInHorizontally { it / 3 * sign } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it / 3 * sign } + fadeOut())
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
                    }

                    if (!isTablet) {
                        ExpressiveNavBar(
                            currentScreen = state.currentScreen,
                            onScreenSelected = { vm.setScreen(it) },
                        )
                    }
                }
            }
        }
    }
}
