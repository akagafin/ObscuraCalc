package com.obscuracalc.feature.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun rememberCalculatorController(): CalculatorController {
    return remember { CalculatorController() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    controller: CalculatorController,
    onEvaluateExpression: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToConverter: () -> Unit,
    onNavigateToVault: () -> Unit
) {
    val state by controller.stateFlow.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ObscuraCalc") },
                actions = {
                    IconButton(onClick = onNavigateToVault) {
                        Icon(Icons.Default.Lock, contentDescription = "Vault")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Expression Display - Google-like but distinct
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = state.expression,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.display,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = if (state.display.length > 8) 48.sp else 64.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }

            // Quick Actions
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AssistChip(
                        onClick = onNavigateToConverter,
                        label = { Text("Converter") },
                        leadingIcon = { Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            // Keypad - Material 3 Tonal Palette
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val keypad = listOf(
                        listOf("AC", "DEL", "%", "/"),
                        listOf("7", "8", "9", "*"),
                        listOf("4", "5", "6", "-"),
                        listOf("1", "2", "3", "+"),
                        listOf("0", ".", "MS", "=")
                    )

                    keypad.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { label ->
                                CalculatorButton(
                                    label = label,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        when (label) {
                                            "AC" -> controller.clear()
                                            "DEL" -> controller.deleteLast()
                                            "=" -> {
                                                onEvaluateExpression(controller.state.expression)
                                                controller.evaluate()
                                            }
                                            "MS" -> controller.memoryStore()
                                            else -> controller.append(label)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = label in listOf("/", "*", "-", "+", "=")
    val isSpecial = label in listOf("AC", "DEL", "%", "MS")
    val isEquals = label == "="

    val containerColor = when {
        isEquals -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isSpecial -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        isEquals -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        isSpecial -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (label == "DEL") {
            Icon(
                Icons.AutoMirrored.Outlined.Backspace,
                contentDescription = "Delete",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (isOperator || isEquals) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (label.length > 2) 16.sp else 22.sp
                ),
                color = contentColor
            )
        }
    }
}
