package com.obscuracalc.feature.converter

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import com.obscuracalc.core.converter.ConversionResult
import com.obscuracalc.core.converter.CurrencyImportFormat
import com.obscuracalc.core.converter.CurrencyRateImporter
import com.obscuracalc.core.converter.CurrencyRateTable
import com.obscuracalc.core.converter.UnitCategory
import com.obscuracalc.core.converter.UnitConverter
import java.math.BigDecimal

data class ConverterUiState(
    val category: UnitCategory = UnitCategory.LENGTH,
    val fromUnit: String = "m",
    val toUnit: String = "ft",
    val inputValue: String = "1",
    val outputValue: String = "",
    val selectedBaseCurrency: String = "USD",
    val rates: Map<String, BigDecimal> = emptyMap(),
    val rateCodeInput: String = "",
    val rateValueInput: String = "",
    val importMessage: String? = null,
)

class ConverterController(
    private val context: Context,
    private val store: CurrencyRatesStore = CurrencyRatesStore(context),
    private val unitConverter: UnitConverter = UnitConverter(),
    private val currencyRateImporter: CurrencyRateImporter = CurrencyRateImporter(),
) {
    var state by mutableStateOf(loadInitialState())
        private set

    fun categories(): List<UnitCategory> = UnitCategory.entries

    fun availableUnits(): List<String> {
        return if (state.category == UnitCategory.CURRENCY) {
            (listOf(state.selectedBaseCurrency) + state.rates.keys).distinct().sorted()
        } else {
            unitConverter.availableUnits(state.category).map { it.id }
        }
    }

    fun setCategory(category: UnitCategory) {
        val defaults = when (category) {
            UnitCategory.LENGTH -> "m" to "ft"
            UnitCategory.MASS -> "kg" to "lb"
            UnitCategory.AREA -> "m2" to "ft2"
            UnitCategory.VOLUME -> "l" to "gal"
            UnitCategory.TEMPERATURE -> "c" to "f"
            UnitCategory.SPEED -> "kph" to "mph"
            UnitCategory.TIME -> "min" to "h"
            UnitCategory.DIGITAL_SIZE -> "mb" to "gb"
            UnitCategory.CURRENCY -> {
                val units = availableCurrencyUnits()
                units.firstOrNull().orEmpty() to units.drop(1).firstOrNull().orEmpty()
            }
        }
        state = state.copy(category = category, fromUnit = defaults.first, toUnit = defaults.second, outputValue = "")
    }

    fun updateInput(value: String) {
        state = state.copy(inputValue = value)
    }

    fun updateFromUnit(unit: String) {
        state = state.copy(fromUnit = unit)
    }

    fun updateToUnit(unit: String) {
        state = state.copy(toUnit = unit)
    }

    fun updateBaseCurrency(code: String) {
        val updated = state.copy(selectedBaseCurrency = code.uppercase())
        state = updated
        persistRates()
    }

    fun updateManualRateCode(code: String) {
        state = state.copy(rateCodeInput = code.uppercase())
    }

    fun updateManualRateValue(value: String) {
        state = state.copy(rateValueInput = value)
    }

    fun saveManualRate() {
        val rate = state.rateValueInput.toBigDecimalOrNull() ?: return
        val updatedRates = state.rates + (state.rateCodeInput.uppercase() to rate)
        state = state.copy(rates = updatedRates, rateCodeInput = "", rateValueInput = "", importMessage = "Rate saved locally")
        persistRates()
    }

    fun removeManualRate(code: String) {
        state = state.copy(rates = state.rates - code)
        persistRates()
    }

    fun importRates(uri: Uri) {
        runCatching {
            val format = detectFormat(uri)
            val table = context.contentResolver.openInputStream(uri)?.use { stream ->
                currencyRateImporter.import(stream, format)
            } ?: throw IllegalArgumentException("Unable to open the selected file")
            state = state.copy(
                selectedBaseCurrency = table.baseCurrency,
                rates = table.rates,
                fromUnit = table.baseCurrency,
                toUnit = table.rates.keys.firstOrNull().orEmpty(),
                importMessage = "Imported ${table.rates.size} rates from ${format.name}",
            )
            persistRates()
        }.onFailure {
            state = state.copy(importMessage = it.message ?: "Unable to import rates")
        }
    }

    fun convert(): ConversionResult? {
        val input = state.inputValue.toBigDecimalOrNull() ?: return null
        val result = unitConverter.convert(
            category = state.category,
            fromUnit = state.fromUnit,
            toUnit = state.toUnit,
            value = input,
            currencyRates = if (state.category == UnitCategory.CURRENCY) {
                CurrencyRateTable(state.selectedBaseCurrency, state.rates)
            } else {
                null
            },
        )
        state = state.copy(outputValue = result.formattedOutput, importMessage = null)
        return result
    }

    private fun loadInitialState(): ConverterUiState {
        val local = store.load()
        return ConverterUiState(
            selectedBaseCurrency = local.baseCurrency,
            rates = local.rates,
        )
    }

    private fun detectFormat(uri: Uri): CurrencyImportFormat {
        val name = DocumentFile.fromSingleUri(context, uri)?.name?.lowercase().orEmpty()
        return if (name.endsWith(".csv")) CurrencyImportFormat.CSV else CurrencyImportFormat.JSON
    }

    private fun availableCurrencyUnits(): List<String> {
        return (listOf(state.selectedBaseCurrency) + state.rates.keys).distinct().sorted()
    }

    private fun persistRates() {
        store.save(LocalCurrencyState(state.selectedBaseCurrency, state.rates))
    }
}
