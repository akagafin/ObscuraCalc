package com.obscuracalc.core.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ExpressionEngineTest {
    private val engine = ExpressionEngine()

    @Test
    fun respectsOperatorPrecedence() {
        val result = engine.evaluate("2+3*4", AngleMode.DEG)
        assertTrue(result.isSuccess)
        assertEquals("14", result.formattedValue)
    }

    @Test
    fun supportsScientificFunctions() {
        val result = engine.evaluate("sin(30)+sqrt(16)", AngleMode.DEG)
        assertTrue(result.isSuccess)
        assertEquals("4.5", BigDecimal(result.formattedValue).stripTrailingZeros().toPlainString())
    }

    @Test
    fun supportsConstantsAndPowers() {
        val result = engine.evaluate("pi^2", AngleMode.RAD)
        assertTrue(result.isSuccess)
        assertEquals("9.869604401089359", result.formattedValue)
    }

    @Test
    fun supportsFactorial() {
        val result = engine.evaluate("5!", AngleMode.DEG)
        assertTrue(result.isSuccess)
        assertEquals("120", result.formattedValue)
    }

    @Test
    fun supportsModulo() {
        val result = engine.evaluate("10%3", AngleMode.DEG)
        assertTrue(result.isSuccess)
        assertEquals("1", result.formattedValue)
    }

    @Test
    fun reportsInvalidExpressions() {
        val result = engine.evaluate("2++", AngleMode.DEG)
        assertFalse(result.isSuccess)
        assertEquals("Error", result.formattedValue)
    }
}
