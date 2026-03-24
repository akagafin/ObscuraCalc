package com.obscuracalc.feature.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    onNavigateToVault: () -> Unit,
) {
    val state by controller.stateFlow.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Top Bar ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { controller.toggleHistory() }) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = "History",
                        tint = colorScheme.outline,
                    )
                }
                Row {
                    // DEG/RAD indicator
                    Text(
                        text = state.angleMode.name,
                        color = colorScheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { controller.toggleAngleMode() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "Settings",
                            tint = colorScheme.outline,
                        )
                    }
                }
            }

            // ── History Panel (animated) ─────────────────────
            AnimatedVisibility(
                visible = state.isHistoryVisible,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.surface,
                    shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                    tonalElevation = 2.dp,
                ) {
                    if (state.history.isEmpty()) {
                        Text(
                            text = "No history yet",
                            color = colorScheme.outline,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(20.dp),
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(state.history) { entry ->
                                Text(
                                    text = entry,
                                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // ── Display Area ─────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
            ) {
                // Expression
                Text(
                    text = state.expression.ifEmpty { " " },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = if (state.expression.length > 12) 36.sp else 48.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp,
                    ),
                    color = if (state.lastError != null) colorScheme.error else colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 56.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Preview result
                if (state.display.isNotEmpty() && state.display != "0" && state.display != state.expression) {
                    Text(
                        text = "= ${state.display}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 24.sp,
                        ),
                        color = colorScheme.outline,
                        textAlign = TextAlign.End,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(
                color = colorScheme.outlineVariant.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            // ── Scientific Panel (expandable) ────────────────
            AnimatedVisibility(
                visible = state.isScientificExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val sciRow1 = listOf("sin", "cos", "tan", "log")
                        val sciRow2 = listOf("ln", "√", "π", "e")
                        val sciRow3 = listOf("^", "!", "(", ")")

                        listOf(sciRow1, sciRow2, sciRow3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                row.forEach { label ->
                                    ScientificButton(
                                        label = label,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            when (label) {
                                                "√" -> controller.append("sqrt")
                                                "π" -> controller.append("π")
                                                "(" -> controller.append("(")
                                                ")" -> controller.append(")")
                                                else -> controller.append(label)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Expand/Collapse Toggle ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { controller.toggleScientific() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (state.isScientificExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = "Toggle scientific",
                    tint = colorScheme.outline,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (state.isScientificExpanded) "Hide" else "Scientific",
                    color = colorScheme.outline,
                    fontSize = 11.sp,
                )
            }

            // ── Main Keypad ──────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Row 1: AC  ()  %  ÷
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CalcButton("AC", ButtonType.FUNCTION, Modifier.weight(1f)) { controller.clear() }
                        CalcButton("( )", ButtonType.FUNCTION, Modifier.weight(1f)) { controller.toggleParenthesis() }
                        CalcButton("%", ButtonType.FUNCTION, Modifier.weight(1f)) { controller.append("%") }
                        CalcButton("÷", ButtonType.OPERATOR, Modifier.weight(1f)) { controller.append("/") }
                    }
                    // Row 2: 7  8  9  ×
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CalcButton("7", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("7") }
                        CalcButton("8", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("8") }
                        CalcButton("9", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("9") }
                        CalcButton("×", ButtonType.OPERATOR, Modifier.weight(1f)) { controller.append("*") }
                    }
                    // Row 3: 4  5  6  -
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CalcButton("4", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("4") }
                        CalcButton("5", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("5") }
                        CalcButton("6", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("6") }
                        CalcButton("−", ButtonType.OPERATOR, Modifier.weight(1f)) { controller.append("-") }
                    }
                    // Row 4: 1  2  3  +
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CalcButton("1", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("1") }
                        CalcButton("2", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("2") }
                        CalcButton("3", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append("3") }
                        CalcButton("+", ButtonType.OPERATOR, Modifier.weight(1f)) { controller.append("+") }
                    }
                    // Row 5: 0(span2)  .  DEL  =
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CalcButton("0", ButtonType.NUMBER, Modifier.weight(2f), aspectOverride = 2.2f) { controller.append("0") }
                        CalcButton(".", ButtonType.NUMBER, Modifier.weight(1f)) { controller.append(".") }
                        CalcButtonIcon(Modifier.weight(1f)) { controller.deleteLast() }
                        CalcButton("=", ButtonType.EQUALS, Modifier.weight(1f)) {
                            onEvaluateExpression(controller.state.expression)
                            controller.evaluate()
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// ── Button Types ─────────────────────────────────────────────
private enum class ButtonType {
    NUMBER, OPERATOR, FUNCTION, EQUALS
}

// ── Main Calculator Button ───────────────────────────────────
@Composable
private fun CalcButton(
    label: String,
    type: ButtonType,
    modifier: Modifier = Modifier,
    aspectOverride: Float = 1.1f,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 800f),
        label = "btnScale",
    )
    val colorScheme = MaterialTheme.colorScheme

    val containerColor = when (type) {
        ButtonType.NUMBER -> colorScheme.surfaceVariant
        ButtonType.OPERATOR -> colorScheme.surface
        ButtonType.FUNCTION -> colorScheme.surface
        ButtonType.EQUALS -> colorScheme.primary
    }

    val contentColor = when (type) {
        ButtonType.NUMBER -> colorScheme.onSurfaceVariant
        ButtonType.OPERATOR -> colorScheme.tertiary
        ButtonType.FUNCTION -> colorScheme.secondary
        ButtonType.EQUALS -> colorScheme.onPrimary
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .aspectRatio(aspectOverride)
            .scale(scale)
            .clip(shape)
            .then(
                if (type == ButtonType.EQUALS) {
                    Modifier.background(
                        Brush.linearGradient(listOf(colorScheme.primary, colorScheme.primaryContainer))
                    )
                } else {
                    Modifier.background(containerColor)
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = if (type == ButtonType.EQUALS) FontWeight.Bold else FontWeight.Medium,
                fontSize = when {
                    type == ButtonType.FUNCTION && label.length > 1 -> 18.sp
                    else -> 24.sp
                },
            ),
            color = contentColor,
        )
    }
}

// ── Backspace Icon Button ────────────────────────────────────
@Composable
private fun CalcButtonIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 800f),
        label = "delScale",
    )
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .aspectRatio(1.1f)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.Backspace,
            contentDescription = "Delete",
            tint = colorScheme.secondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

// ── Scientific Panel Button ──────────────────────────────────
@Composable
private fun ScientificButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 800f),
        label = "sciScale",
    )
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .height(42.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = colorScheme.secondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
