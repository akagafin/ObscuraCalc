package com.obscuracalc.feature.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun rememberCalculatorController(): CalculatorController = remember { CalculatorController() }

@Composable
fun CalculatorScreen(
    controller: CalculatorController,
    modifier: Modifier = Modifier,
    onEvaluateExpression: (String) -> Unit = {},
) {
    val state = controller.state
    val scientificRows = listOf(
        listOf("sin", "cos", "tan", "sqrt", "^"),
        listOf("asin", "acos", "atan", "log", "ln"),
        listOf("(", ")", "pi", "e", "exp"),
    )
    val keypadRows = listOf(
        listOf("7", "8", "9", "/", "C"),
        listOf("4", "5", "6", "*", "-"),
        listOf("1", "2", "3", "+", "."),
        listOf("0", "00", ",", "=", "DEL"),
    )

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(onClick = controller::toggleAngleMode, modifier = Modifier.weight(1f)) {
                    Text(state.angleMode.name)
                }
                FilledTonalButton(onClick = controller::memoryClear, modifier = Modifier.weight(1f)) {
                    Text("MC")
                }
                FilledTonalButton(onClick = controller::memoryRecall, modifier = Modifier.weight(1f)) {
                    Text("MR")
                }
                FilledTonalButton(onClick = controller::memoryAdd, modifier = Modifier.weight(1f)) {
                    Text("M+")
                }
                FilledTonalButton(onClick = controller::memoryStore, modifier = Modifier.weight(1f)) {
                    Text("MS")
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(28.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Memory ${state.memoryPreview}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.display,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.lastError != null) {
                        Text(
                            text = state.lastError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (state.history.isNotEmpty()) {
                Card(shape = RoundedCornerShape(24.dp)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(state.history) { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scientificRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { label ->
                            CalcButton(
                                label = label,
                                modifier = Modifier.weight(1f),
                                onClick = { controller.append(label) },
                            )
                        }
                    }
                }

                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { label ->
                            when (label) {
                                "C" -> FilledTonalButton(
                                    onClick = controller::clear,
                                    modifier = Modifier.weight(1f),
                                ) { Text("AC") }

                                "DEL" -> OutlinedButton(
                                    onClick = controller::deleteLast,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Outlined.Backspace, contentDescription = "Delete")
                                }

                                "=" -> Button(
                                    onClick = {
                                        onEvaluateExpression(controller.state.expression)
                                        controller.evaluate()
                                    },
                                    modifier = Modifier.weight(1f),
                                ) { Text("=") }

                                else -> CalcButton(
                                    label = label,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (label == ",") controller.append(",") else controller.append(label)
                                    },
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
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(text = label)
    }
}
