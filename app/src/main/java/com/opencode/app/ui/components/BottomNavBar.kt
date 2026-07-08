@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen

@Composable
fun ExpressiveNavBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val items = com.opencode.app.viewmodel.navItems
    val activeIndex = items.indexOfFirst { it.screen == currentScreen }

    val indicatorOffset by animateDpAsState(
        targetValue = if (activeIndex >= 0) (activeIndex * 160).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "navIndicator",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(64.dp)
            .drawBehind { drawRect(color = scheme.surface) },
    ) {
        // Animated pill indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(152.dp)
                .height(32.dp)
                .drawBehind { drawRoundRect(color = scheme.secondaryContainer, cornerRadius = CornerRadius(16f, 16f)) },
        )

        // Tab items
        Row(modifier = Modifier.fillMaxSize()) {
            items.forEach { item ->
                val selected = currentScreen == item.screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onScreenSelected(item.screen) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
