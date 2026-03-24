package com.obscuracalc.core.converter

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream
import java.math.BigDecimal

class CurrencyRateImporter {
    fun import(stream: InputStream, format: CurrencyImportFormat): CurrencyRateTable {
        return when (format) {
            CurrencyImportFormat.JSON -> importJson(stream)
            CurrencyImportFormat.CSV -> importCsv(stream)
        }
    }

    private fun importJson(stream: InputStream): CurrencyRateTable {
        val root = Json.parseToJsonElement(stream.bufferedReader().use { it.readText() }).jsonObject
        val base = root["base"]?.jsonPrimitive?.content?.uppercase()
            ?: throw IllegalArgumentException("JSON must contain a 'base' field")
        val rates = root["rates"]?.jsonObject
            ?: throw IllegalArgumentException("JSON must contain a 'rates' object")
        val parsedRates = rates.mapValues { (_, value) ->
            value.jsonPrimitive.content.toBigDecimal()
        }
        validateRates(base, parsedRates)
        return CurrencyRateTable(base, parsedRates)
    }

    private fun importCsv(stream: InputStream): CurrencyRateTable {
        val lines = stream.bufferedReader().use { it.readLines() }.filter { it.isNotBlank() }
        if (lines.size < 2) {
            throw IllegalArgumentException("CSV must contain a header and at least one rate row")
        }
        val header = lines.first().trim().lowercase()
        if (header != "base,code,rate") {
            throw IllegalArgumentException("CSV header must be: base,code,rate")
        }
        val rows = lines.drop(1).map { line ->
            val parts = line.split(",").map(String::trim)
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid CSV row: $line")
            }
            Triple(parts[0].uppercase(), parts[1].uppercase(), parts[2].toBigDecimal())
        }
        val base = rows.first().first
        if (rows.any { it.first != base }) {
            throw IllegalArgumentException("All CSV rows must use the same base currency")
        }
        val rates = rows.associate { it.second to it.third }
        validateRates(base, rates)
        return CurrencyRateTable(base, rates)
    }

    private fun validateRates(base: String, rates: Map<String, BigDecimal>) {
        if (rates.isEmpty()) {
            throw IllegalArgumentException("At least one currency rate is required")
        }
        if (rates.containsKey(base)) {
            throw IllegalArgumentException("Rate table should not include the base currency as a separate rate")
        }
        if (rates.values.any { it <= BigDecimal.ZERO }) {
            throw IllegalArgumentException("All currency rates must be positive numbers")
        }
    }
}
