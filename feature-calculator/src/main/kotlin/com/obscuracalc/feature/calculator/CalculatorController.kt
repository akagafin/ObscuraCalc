package com.obscuracalc.feature.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.obscuracalc.core.calculator.AngleMode
import com.obscuracalc.core.calculator.CalcResult
import com.obscuracalc.core.calculator.ExpressionEngine
import com.obscuracalc.core.calculator.MemoryRegister
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

    var state by mutableStateOf(CalculatorUiState())
        private set

    fun append(token: String) {
        val next = when (token) {
            "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "exp", "abs" -> "$token("
            else -> token
        }
        val expression = if (state.expression == "0") next else state.expression + next
        state = state.copy(expression = expression, display = expression, lastError = null)
    }

    fun clear() {
        state = state.copy(expression = "", display = "0", lastError = null)
    }

    fun deleteLast() {
        val expression = state.expression.dropLast(1)
        state = state.copy(
            expression = expression,
            display = expression.ifBlank { "0" },
            lastError = null,
        )
    }

    fun toggleAngleMode() {
        state = state.copy(
            angleMode = if (state.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG,
        )
    }

    fun evaluate(): CalcResult {
        val expression = state.expression.ifBlank { state.display }
        val result = engine.evaluate(expression, state.angleMode)
        state = if (result.isSuccess) {
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
        state = state.copy(expression = recalled, display = recalled, lastError = null)
    }

    fun memoryClear() {
        memory.clear()
        refreshMemoryPreview()
    }

    private fun currentValue(): BigDecimal? {
        return runCatching { state.display.toBigDecimal() }.getOrNull()
            ?: runCatching { state.expression.toBigDecimal() }.getOrNull()
    }

    private fun refreshMemoryPreview() {
        state = state.copy(memoryPreview = memory.recall().stripTrailingZeros().toPlainString())
    }
}
