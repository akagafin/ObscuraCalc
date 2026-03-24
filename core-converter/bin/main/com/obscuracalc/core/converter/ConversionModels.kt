package com.obscuracalc.core.converter

import java.math.BigDecimal

enum class UnitCategory {
    LENGTH,
    MASS,
    AREA,
    VOLUME,
    TEMPERATURE,
    SPEED,
    TIME,
    DIGITAL_SIZE,
    CURRENCY,
}

data class UnitDefinition(
    val id: String,
    val label: String,
)

data class ConversionResult(
    val category: UnitCategory,
    val from: UnitDefinition,
    val to: UnitDefinition,
    val input: BigDecimal,
    val output: BigDecimal,
    val formattedOutput: String,
)

enum class CurrencyImportFormat {
    JSON,
    CSV,
}

data class CurrencyRateTable(
    val baseCurrency: String,
    val rates: Map<String, BigDecimal>,
)
