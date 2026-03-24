package com.obscuracalc.core.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class ExpressionEngine(
    private val mathContext: MathContext = MathContext(16, RoundingMode.HALF_EVEN),
) {
    fun evaluate(expression: String, angleMode: AngleMode): CalcResult {
        return runCatching {
            val value = Parser(expression, angleMode, mathContext).parse().round(mathContext)
            CalcResult(
                expression = expression,
                value = value,
                formattedValue = format(value),
            )
        }.getOrElse { throwable ->
            CalcResult(
                expression = expression,
                value = null,
                formattedValue = "Error",
                error = throwable.message ?: "Invalid expression",
            )
        }
    }

    fun format(value: BigDecimal): String {
        return value.stripTrailingZeros().toPlainString()
    }

    private class Parser(
        rawInput: String,
        private val angleMode: AngleMode,
        private val mathContext: MathContext,
    ) {
        private val pi = BigDecimal("3.14159265358979323846264338327950288419716939937510")
        private val e = BigDecimal("2.71828182845904523536028747135266249775724709369995")
        private val input = rawInput.replace(" ", "")
        private var position = 0

        fun parse(): BigDecimal {
            if (input.isBlank()) {
                throw IllegalArgumentException("Expression cannot be empty")
            }
            val result = parseExpression()
            if (!isAtEnd()) {
                throw IllegalArgumentException("Unexpected token at position $position")
            }
            return result
        }

        private fun parseExpression(): BigDecimal {
            var value = parseTerm()
            while (true) {
                value = when {
                    match('+') -> value.add(parseTerm(), mathContext)
                    match('-') -> value.subtract(parseTerm(), mathContext)
                    else -> return value
                }
            }
        }

        private fun parseTerm(): BigDecimal {
            var value = parsePower()
            while (true) {
                value = when {
                    match('*') -> value.multiply(parsePower(), mathContext)
                    match('/') -> {
                        val divisor = parsePower()
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw ArithmeticException("Division by zero")
                        }
                        value.divide(divisor, mathContext)
                    }
                    match('%') -> {
                        val divisor = parsePower()
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw ArithmeticException("Division by zero")
                        }
                        value.remainder(divisor, mathContext)
                    }
                    else -> return value
                }
            }
        }

        private fun parsePower(): BigDecimal {
            val base = parseUnary()
            return if (match('^')) {
                pow(base, parsePower())
            } else {
                base
            }
        }

        private fun parseUnary(): BigDecimal {
            return when {
                match('+') -> parseUnary()
                match('-') -> parseUnary().negate()
                else -> parsePrimary()
            }
        }

        private fun parsePrimary(): BigDecimal {
            var value = when {
                match('(') -> {
                    val v = parseExpression()
                    expect(')')
                    v
                }
                peek()?.isDigit() == true || peek() == '.' -> parseNumber()
                peek()?.isLetter() == true -> parseIdentifier()
                else -> throw IllegalArgumentException("Unexpected token at position $position")
            }
            // Handle postfix factorial operator
            while (match('!')) {
                value = factorial(value)
            }
            return value
        }

        private fun parseNumber(): BigDecimal {
            val start = position
            while (peek()?.isDigit() == true || peek() == '.') {
                position++
            }
            return input.substring(start, position).toBigDecimal(mathContext)
        }

        private fun parseIdentifier(): BigDecimal {
            val start = position
            while (peek()?.isLetter() == true) {
                position++
            }
            val name = input.substring(start, position).lowercase()
            return when (name) {
                "pi" -> pi
                "e" -> e
                else -> {
                    expect('(')
                    val argument = parseExpression()
                    val secondArgument = if (match(',')) parseExpression() else null
                    expect(')')
                    evaluateFunction(name, argument, secondArgument)
                }
            }
        }

        private fun evaluateFunction(
            name: String,
            argument: BigDecimal,
            secondArgument: BigDecimal?,
        ): BigDecimal {
            val arg = argument.toDouble()
            val result = when (name) {
                "sin" -> sin(toRadiansIfNeeded(arg))
                "cos" -> cos(toRadiansIfNeeded(arg))
                "tan" -> tan(toRadiansIfNeeded(arg))
                "asin" -> fromRadiansIfNeeded(asin(arg))
                "acos" -> fromRadiansIfNeeded(acos(arg))
                "atan" -> fromRadiansIfNeeded(atan(arg))
                "log" -> log10(arg)
                "ln" -> ln(arg)
                "exp" -> exp(arg)
                "sqrt" -> sqrt(arg)
                "abs" -> kotlin.math.abs(arg)
                "root" -> {
                    val exponent = secondArgument?.toDouble()
                        ?: throw IllegalArgumentException("root(value, degree) requires two arguments")
                    arg.pow(1.0 / exponent)
                }

                else -> throw IllegalArgumentException("Unknown function: $name")
            }
            return BigDecimal.valueOf(result).round(mathContext)
        }

        private fun toRadiansIfNeeded(value: Double): Double {
            return if (angleMode == AngleMode.DEG) Math.toRadians(value) else value
        }

        private fun fromRadiansIfNeeded(value: Double): Double {
            return if (angleMode == AngleMode.DEG) Math.toDegrees(value) else value
        }

        private fun pow(base: BigDecimal, exponent: BigDecimal): BigDecimal {
            return if (exponent.scale() <= 0) {
                base.pow(exponent.toInt(), mathContext)
            } else {
                BigDecimal(base.toDouble().pow(exponent.toDouble()), mathContext)
            }
        }

        private fun factorial(n: BigDecimal): BigDecimal {
            val intVal = n.stripTrailingZeros()
            if (intVal.scale() > 0 || intVal < BigDecimal.ZERO) {
                throw IllegalArgumentException("Factorial requires a non-negative integer")
            }
            val num = intVal.toInt()
            if (num > 170) throw IllegalArgumentException("Factorial overflow for $num")
            var result = BigDecimal.ONE
            for (i in 2..num) {
                result = result.multiply(BigDecimal(i), mathContext)
            }
            return result
        }

        private fun match(expected: Char): Boolean {
            if (peek() == expected) {
                position++
                return true
            }
            return false
        }

        private fun expect(expected: Char) {
            if (!match(expected)) {
                throw IllegalArgumentException("Expected '$expected' at position $position")
            }
        }

        private fun peek(): Char? = input.getOrNull(position)

        private fun isAtEnd(): Boolean = position >= input.length
    }
}
