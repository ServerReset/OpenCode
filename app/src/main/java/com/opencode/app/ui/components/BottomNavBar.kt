@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen
import com.opencode.app.viewmodel.NavItem

/** M3 Expressive NavigationBar: height 64, indicator pill 32x56, spring animation.
 *  Active indicator: secondary-container, icon on-secondary-container, label secondary.
 *  Inactive: on-surface-variant. */
@Composable
fun ExpressiveNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val navItems = com.opencode.app.viewmodel.navItems
    val activeIndex = navItems.indexOfFirst { it.screen == currentScreen }
    val itemWidth = if (navItems.isEmpty()) 1f else 1f / navItems.size

    // Animated indicator position with M3E spring
    val indicatorOffset by animateDpAsState(
        targetValue = if (activeIndex >= 0) (activeIndex * itemWidth * 1000).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "navIndicator",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Active indicator pill - 32x56
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) {
                if (activeIndex >= 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(
                                    ((1000 / navItems.size) - 16).coerceAtLeast(32).dp
                                )
                                .height(32.dp)
                                .offset(x = indicatorOffset)
                                .align(Alignment.CenterStart)
                                .drawBehind {
                                    drawRoundRect(
                                        color = scheme.secondaryContainer,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                                    )
                                },
                        )
                    }
                }

                // Tab items
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    navItems.forEach { item ->
                        val selected = currentScreen == item.screen
                        Surface(
                            onClick = { onScreenSelected(item.screen) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            color = Color.Transparent,
                            contentColor = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
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
                                    style = if (selected)
                                        MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
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
