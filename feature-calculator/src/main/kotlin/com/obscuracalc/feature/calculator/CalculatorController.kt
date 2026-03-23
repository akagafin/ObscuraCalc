package com.obscuracalc.feature.calculator

import com.obscuracalc.core.calculator.AngleMode
import com.obscuracalc.core.calculator.CalcResult
import com.obscuracalc.core.calculator.ExpressionEngine
import com.obscuracalc.core.calculator.MemoryRegister
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal

data class CalculatorUiState(
    val expression: String = "",
    val display: String = "0",
    val history: List<String> = emptyList(),
    val angleMode: AngleMode = AngleMode.DEG,
    val memoryPreview: String = "0",
    val lastError: String? = null,
)

class CalculatorController(
    private val engine: ExpressionEngine = ExpressionEngine(),
) {
    private val memory = MemoryRegister()
    private val _state = MutableStateFlow(CalculatorUiState())
    val stateFlow: StateFlow<CalculatorUiState> = _state.asStateFlow()

    val state: CalculatorUiState get() = _state.value

    fun append(token: String) {
        _state.update { currentState ->
            val next = when (token) {
                "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "exp", "abs" -> "$token("
                else -> token
            }
            val newExpression = if (currentState.expression == "0") next else currentState.expression + next
            currentState.copy(expression = newExpression, display = newExpression, lastError = null)
        }
    }

    fun clear() {
        _state.update { it.copy(expression = "", display = "0", lastError = null) }
    }

    fun deleteLast() {
        _state.update { currentState ->
            val newExpression = currentState.expression.dropLast(1)
            currentState.copy(
                expression = newExpression,
                display = newExpression.ifBlank { "0" },
                lastError = null,
            )
        }
    }

    fun toggleAngleMode() {
        _state.update { currentState ->
            currentState.copy(
                angleMode = if (currentState.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG,
            )
        }
    }

    fun evaluate(): CalcResult {
        val currentState = _state.value
        val expression = currentState.expression.ifBlank { currentState.display }
        val result = engine.evaluate(expression, currentState.angleMode)
        
        _state.update { state ->
            if (result.isSuccess) {
                state.copy(
                    expression = result.formattedValue,
                    display = result.formattedValue,
                    history = listOf("$expression = ${result.formattedValue}") + state.history.take(5),
                    lastError = null,
                )
            } else {
                state.copy(
                    display = result.formattedValue,
                    lastError = result.error,
                    history = listOf("$expression -> ${result.error}") + state.history.take(5),
                )
            }
        }
        return result
    }

    fun memoryStore() {
        currentValue()?.let(memory::store)
        refreshMemoryPreview()
    }

    fun memoryAdd() {
        currentValue()?.let(memory::add)
        refreshMemoryPreview()
    }

    fun memorySubtract() {
        currentValue()?.let(memory::subtract)
        refreshMemoryPreview()
    }

    fun memoryRecall() {
        val recalled = memory.recall().stripTrailingZeros().toPlainString()
        _state.update { it.copy(expression = recalled, display = recalled, lastError = null) }
    }

    fun memoryClear() {
        memory.clear()
        refreshMemoryPreview()
    }

    private fun currentValue(): BigDecimal? {
        val s = _state.value
        return runCatching { s.display.toBigDecimal() }.getOrNull()
            ?: runCatching { s.expression.toBigDecimal() }.getOrNull()
    }

    private fun refreshMemoryPreview() {
        _state.update { it.copy(memoryPreview = memory.recall().stripTrailingZeros().toPlainString()) }
    }
}
