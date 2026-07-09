@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.opencode.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen

@Composable
fun ExpressiveNavBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val items = com.opencode.app.viewmodel.navItems
    var barWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val activeIndex = items.indexOfFirst { it.screen == currentScreen }
    val itemWidth = if (barWidth > 0 && items.isNotEmpty()) barWidth / items.size else 0

    val indicatorOffset by animateDpAsState(
        targetValue = if (activeIndex >= 0 && itemWidth > 0) {
            with(density) { (activeIndex * itemWidth).toDp() }
        } else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "navIndicator",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        // Floating pill background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp), clip = false)
                .clip(RoundedCornerShape(28.dp))
                .background(scheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            // Animated indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = indicatorOffset),
            ) {
                Box(
                    modifier = Modifier
                        .width(with(density) { (itemWidth - 8).toDp().coerceAtLeast(0.dp) })
                        .height(44.dp)
                        .padding(4.dp)
                        .background(scheme.secondaryContainer, RoundedCornerShape(22.dp)),
                )
            }

            // Tab items
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        barWidth = coordinates.size.width
                    },
            ) {
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
                                modifier = Modifier.size(22.dp),
                                tint = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            )
                        }
                    }
                }
            }
        }
    }
}
