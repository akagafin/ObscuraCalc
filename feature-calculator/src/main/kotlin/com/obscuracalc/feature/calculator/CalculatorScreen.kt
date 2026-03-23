package com.obscuracalc.feature.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun rememberCalculatorController(): CalculatorController {
    return remember { CalculatorController() }
}

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
        containerColor = Color(0xFF0D0D2B) // Deep dark blue like the image
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Minimal Top Bar (Icons only, no title)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateToVault) {
                    Icon(
                        Icons.Outlined.History, 
                        contentDescription = "History", 
                        tint = Color.Gray.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Outlined.MoreVert, 
                        contentDescription = "Settings", 
                        tint = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }

            // Display Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.expression,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.White,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    lineHeight = 56.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (state.display.isNotEmpty() && state.display != "0") {
                    Text(
                        text = state.display,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp
                        ),
                        color = Color(0xFFE91E63), // Pink color for result like image
                        textAlign = TextAlign.End
                    )
                }
            }

            // Expand/Collapse Indicator (Visual only, like image)
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.filled.UnfoldMore,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Keypad
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val keypad = listOf(
                        listOf("AC", "()", "%", "/"),
                        listOf("7", "8", "9", "*"),
                        listOf("4", "5", "6", "-"),
                        listOf("1", "2", "3", "+"),
                        listOf("0", ".", "DEL", "=")
                    )

                    keypad.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                            "()" -> { /* Need logic for parens if supported */ }
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
    val isNumber = label.any { it.isDigit() } || label == "."
    val isEquals = label == "="
    val isSpecial = label in listOf("AC", "()", "%", "DEL")

    val containerColor = when {
        isEquals -> Color(0xFFFF8A80) // Light red/pinkish for equals
        isOperator -> Color(0xFF2D2D5B) // Muted purple/blue for operators
        isSpecial -> Color(0xFF3F3F7A) // Slightly lighter for top row
        else -> Color(0xFF1E1E3F) // Deep blue for numbers
    }

    val contentColor = when {
        isEquals -> Color(0xFF1A1A3A)
        isOperator -> Color(0xFF8C9EFF)
        isSpecial -> Color(0xFF8C9EFF)
        else -> Color.White
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
                text = label.replace("*", "×").replace("/", "÷"),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = if (label.length > 1 && !isNumber) 18.sp else 28.sp
                ),
                color = contentColor
            )
        }
    }
}
