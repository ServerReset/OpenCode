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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.opencode.app.data.Screen
import com.opencode.app.viewmodel.AppState
import com.opencode.app.viewmodel.AppViewModel
import com.opencode.app.viewmodel.Todo

@Composable
fun AppBottomBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit, vm: AppViewModel, state: AppState, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val items = com.opencode.app.viewmodel.navItems
    var barWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    var todoInput by remember { mutableStateOf("") }

    val activeIndex = items.indexOfFirst { it.screen == currentScreen }
    val itemWidthPx = if (barWidth > 0f && items.isNotEmpty()) barWidth / items.size else 0f

    val indicatorOffset by animateDpAsState(
        targetValue = if (activeIndex >= 0 && itemWidthPx > 0f) {
            with(density) { (activeIndex.toFloat() * itemWidthPx).toDp() }
        } else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "navIndicator",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        // Todo panel - expands upward from the floating bar
        AnimatedVisibility(
            visible = state.showTodos,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = scheme.surfaceContainerHigh,
                shadowElevation = 6.dp,
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Todos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text("${state.todos.count { it.done }}/${state.todos.size}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { vm.toggleTodos() }, modifier = Modifier.size(24.dp)) { Icon(Icons.Filled.Close, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
                    }
                    Spacer(Modifier.height(4.dp))
                    // Add todo input
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = todoInput, onValueChange = { todoInput = it },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodySmall,
                            decorationBox = { inner -> if (todoInput.isEmpty()) Text("Add todo...", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant); inner() },
                        )
                        IconButton(onClick = { if (todoInput.isNotBlank()) { vm.addTodo(todoInput); todoInput = "" } }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Add, null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    // Todo list
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(state.todos, key = { it.id }) { todo ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Checkbox(checked = todo.done, onCheckedChange = { vm.toggleTodoDone(todo.id) }, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(todo.text, style = MaterialTheme.typography.bodySmall.copy(textDecoration = if (todo.done) TextDecoration.LineThrough else TextDecoration.None),
                                    color = if (todo.done) scheme.onSurfaceVariant else scheme.onSurface, modifier = Modifier.weight(1f))
                                IconButton(onClick = { vm.removeTodo(todo.id) }, modifier = Modifier.size(20.dp)) { Icon(Icons.Filled.Close, null, tint = scheme.error, modifier = Modifier.size(12.dp)) }
                            }
                        }
                    }
                }
            }
        }

        // Floating pill navigation bar
        Box(modifier = Modifier.fillMaxWidth()) {
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
                Box(modifier = Modifier.fillMaxWidth().offset(x = indicatorOffset)) {
                    Box(
                        modifier = Modifier
                            .width(with(density) { (itemWidthPx - 8f).coerceAtLeast(0f).toDp() })
                            .height(44.dp)
                            .padding(4.dp)
                            .background(scheme.secondaryContainer, RoundedCornerShape(22.dp)),
                    )
                }

                // Tab items
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { c -> barWidth = c.size.width.toFloat() },
                ) {
                    items.forEach { item ->
                        val selected = currentScreen == item.screen
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight().clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null, onClick = { onScreenSelected(item.screen) },
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp),
                                    tint = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant)
                                Spacer(Modifier.height(2.dp))
                                Text(item.label, style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Todo toggle button (floating on the right side of the pill)
            Surface(
                onClick = { vm.toggleTodos() },
                modifier = Modifier.align(Alignment.CenterEnd).size(44.dp).padding(4.dp),
                shape = RoundedCornerShape(50),
                color = if (state.showTodos) scheme.primary else scheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Checklist, null, tint = if (state.showTodos) scheme.onPrimary else scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
