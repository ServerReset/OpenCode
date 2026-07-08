@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen

@Composable
fun ExpressiveNavRail(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val navItems = com.opencode.app.viewmodel.navItems
    val activeIndex = navItems.indexOfFirst { it.screen == currentScreen }
    val itemHeight = 72.dp
    val pillHeight = 56.dp
    val pillYOffset = (itemHeight - pillHeight) / 2

    val indicatorOffset by animateDpAsState(
        targetValue = if (activeIndex >= 0) activeIndex * itemHeight + pillYOffset else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "railIndicator",
    )

    Surface(
        modifier = modifier.width(80.dp).fillMaxHeight(),
        color = scheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo
            Spacer(Modifier.height(8.dp))
            Surface(shape = MaterialTheme.shapes.medium, color = scheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("O", style = MaterialTheme.typography.titleLarge, color = scheme.primary)
                }
            }
            Spacer(Modifier.height(24.dp))

            // Items with indicator pill
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Animated pill
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(pillHeight)
                        .offset(y = indicatorOffset)
                        .drawBehind {
                            drawRoundRect(color = scheme.secondaryContainer, cornerRadius = CornerRadius(16f, 16f))
                        },
                )

                // Clickable items
                Column(modifier = Modifier.fillMaxSize()) {
                    navItems.forEach { item ->
                        val selected = currentScreen == item.screen
                        Surface(
                            onClick = { onScreenSelected(item.screen) },
                            modifier = Modifier.fillMaxWidth().height(itemHeight),
                            color = Color.Transparent,
                            contentColor = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                        ) {
                            Column(
                                Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    item.label,
                                    style = if (selected) MaterialTheme.typography.labelMedium
                                        else MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
