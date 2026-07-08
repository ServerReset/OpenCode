package com.opencode.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen
import com.opencode.app.viewmodel.NavItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: Int = 0,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                com.opencode.app.viewmodel.navItems.forEach { item ->
                    val selected = currentScreen == item.screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onScreenSelected(item.screen) },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    )
                }
            }

            // Animated active indicator pill
            val navItems = com.opencode.app.viewmodel.navItems
            val activeIndex = navItems.indexOfFirst { it.screen == currentScreen }
            if (activeIndex >= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                ) {
                    val itemWidth = 1f / navItems.size
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(itemWidth)
                            .fillMaxHeight()
                            .offset(
                                x = (activeIndex * itemWidth * 1000).dp / 1000
                            ),
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight()
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small,
                        ) {}
                    }
                }
            }
        }
    }
}
