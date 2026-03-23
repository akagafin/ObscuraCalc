package com.obscuracalc.core.converter

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class UnitConverter(
    private val mathContext: MathContext = MathContext(16, RoundingMode.HALF_EVEN),
) {
    private val simpleCategories: Map<UnitCategory, Map<String, BigDecimal>> = mapOf(
        UnitCategory.LENGTH to mapOf(
            "mm" to BigDecimal("0.001"),
            "cm" to BigDecimal("0.01"),
            "m" to BigDecimal.ONE,
            "km" to BigDecimal("1000"),
            "in" to BigDecimal("0.0254"),
            "ft" to BigDecimal("0.3048"),
            "yd" to BigDecimal("0.9144"),
            "mi" to BigDecimal("1609.344"),
        ),
        UnitCategory.MASS to mapOf(
            "mg" to BigDecimal("0.000001"),
            "g" to BigDecimal("0.001"),
            "kg" to BigDecimal.ONE,
            "lb" to BigDecimal("0.45359237"),
            "oz" to BigDecimal("0.028349523125"),
        ),
        UnitCategory.AREA to mapOf(
            "mm2" to BigDecimal("0.000001"),
            "cm2" to BigDecimal("0.0001"),
            "m2" to BigDecimal.ONE,
            "ha" to BigDecimal("10000"),
            "km2" to BigDecimal("1000000"),
            "ft2" to BigDecimal("0.09290304"),
            "ac" to BigDecimal("4046.8564224"),
        ),
        UnitCategory.VOLUME to mapOf(
            "ml" to BigDecimal("0.001"),
            "l" to BigDecimal.ONE,
            "m3" to BigDecimal("1000"),
            "tsp" to BigDecimal("0.00492892159375"),
            "tbsp" to BigDecimal("0.01478676478125"),
            "cup" to BigDecimal("0.2365882365"),
            "gal" to BigDecimal("3.785411784"),
        ),
        UnitCategory.SPEED to mapOf(
            "mps" to BigDecimal.ONE,
            "kph" to BigDecimal("0.2777777777777778"),
            "mph" to BigDecimal("0.44704"),
            "knot" to BigDecimal("0.5144444444444445"),
        ),
        UnitCategory.TIME to mapOf(
            "ms" to BigDecimal("0.001"),
            "s" to BigDecimal.ONE,
            "min" to BigDecimal("60"),
            "h" to BigDecimal("3600"),
            "day" to BigDecimal("86400"),
        ),
        UnitCategory.DIGITAL_SIZE to mapOf(
            "b" to BigDecimal.ONE,
            "kb" to BigDecimal("1024"),
            "mb" to BigDecimal("1048576"),
            "gb" to BigDecimal("1073741824"),
            "tb" to BigDecimal("1099511627776"),
        ),
    )

    fun availableUnits(category: UnitCategory): List<UnitDefinition> {
        return when (category) {
            UnitCategory.TEMPERATURE -> listOf("c", "f", "k")
            UnitCategory.CURRENCY -> emptyList()
            else -> simpleCategories.getValue(category).keys.toList()
        }.map { UnitDefinition(it, it.uppercase()) }
    }

    fun convert(
        category: UnitCategory,
        fromUnit: String,
        toUnit: String,
        value: BigDecimal,
        currencyRates: CurrencyRateTable? = null,
    ): ConversionResult {
        val output = when (category) {
            UnitCategory.TEMPERATURE -> convertTemperature(fromUnit, toUnit, value)
            UnitCategory.CURRENCY -> convertCurrency(fromUnit, toUnit, value, currencyRates)
            else -> convertSimple(category, fromUnit, toUnit, value)
        }
        return ConversionResult(
            category = category,
            from = UnitDefinition(fromUnit, fromUnit.uppercase()),
            to = UnitDefinition(toUnit, toUnit.uppercase()),
            input = value,
            output = output,
            formattedOutput = output.stripTrailingZeros().toPlainString(),
        )
    }

    private fun convertSimple(
        category: UnitCategory,
        fromUnit: String,
        toUnit: String,
        value: BigDecimal,
    ): BigDecimal {
        val categoryUnits = simpleCategories[category]
            ?: throw IllegalArgumentException("Unsupported category: $category")
        val fromFactor = categoryUnits[fromUnit]
            ?: throw IllegalArgumentException("Unknown unit: $fromUnit")
        val toFactor = categoryUnits[toUnit]
            ?: throw IllegalArgumentException("Unknown unit: $toUnit")
        val baseValue = value.multiply(fromFactor, mathContext)
        return baseValue.divide(toFactor, mathContext)
    }

    private fun convertTemperature(
        fromUnit: String,
        toUnit: String,
        value: BigDecimal,
    ): BigDecimal {
        val celsius = when (fromUnit) {
            "c" -> value
            "f" -> value.subtract(BigDecimal("32")).multiply(BigDecimal("5")).divide(BigDecimal("9"), mathContext)
            "k" -> value.subtract(BigDecimal("273.15"))
            else -> throw IllegalArgumentException("Unknown temperature unit: $fromUnit")
        }
        return when (toUnit) {
            "c" -> celsius
            "f" -> celsius.multiply(BigDecimal("9")).divide(BigDecimal("5"), mathContext).add(BigDecimal("32"))
            "k" -> celsius.add(BigDecimal("273.15"))
            else -> throw IllegalArgumentException("Unknown temperature unit: $toUnit")
        }
    }

    private fun convertCurrency(
        fromUnit: String,
        toUnit: String,
        value: BigDecimal,
        currencyRates: CurrencyRateTable?,
    ): BigDecimal {
        val rates = currencyRates ?: throw IllegalArgumentException("Currency rates are required")
        val fromRate = if (fromUnit.equals(rates.baseCurrency, ignoreCase = true)) {
            BigDecimal.ONE
        } else {
            rates.rates[fromUnit.uppercase()]
        } ?: throw IllegalArgumentException("Missing rate for $fromUnit")
        val toRate = if (toUnit.equals(rates.baseCurrency, ignoreCase = true)) {
            BigDecimal.ONE
        } else {
            rates.rates[toUnit.uppercase()]
        } ?: throw IllegalArgumentException("Missing rate for $toUnit")
        val baseValue = value.divide(fromRate, mathContext)
        return baseValue.multiply(toRate, mathContext)
    }
}
