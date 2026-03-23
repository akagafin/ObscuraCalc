package com.obscuracalc.core.converter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.math.BigDecimal

class UnitConverterTest {
    private val converter = UnitConverter()
    private val importer = CurrencyRateImporter()

    @Test
    fun convertsTemperatureEdgeCase() {
        val result = converter.convert(
            category = UnitCategory.TEMPERATURE,
            fromUnit = "c",
            toUnit = "f",
            value = BigDecimal("-40"),
        )
        assertEquals("-40", result.formattedOutput)
    }

    @Test
    fun convertsDigitalUnits() {
        val result = converter.convert(
            category = UnitCategory.DIGITAL_SIZE,
            fromUnit = "mb",
            toUnit = "kb",
            value = BigDecimal("2"),
        )
        assertEquals("2048", result.formattedOutput)
    }

    @Test
    fun importsJsonCurrencyTable() {
        val table = importer.import(
            ByteArrayInputStream("""{"base":"USD","rates":{"EUR":0.9,"JPY":150}}""".toByteArray()),
            CurrencyImportFormat.JSON,
        )
        val result = converter.convert(
            category = UnitCategory.CURRENCY,
            fromUnit = "USD",
            toUnit = "EUR",
            value = BigDecimal("10"),
            currencyRates = table,
        )
        assertEquals("9", result.formattedOutput)
    }
}
