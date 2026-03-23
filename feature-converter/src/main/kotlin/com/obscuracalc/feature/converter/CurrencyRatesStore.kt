package com.obscuracalc.feature.converter

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal

class CurrencyRatesStore(context: Context) {
    private val preferences =
        context.getSharedPreferences("obscuracalc_currency_rates", Context.MODE_PRIVATE)

    fun load(): LocalCurrencyState {
        val raw = preferences.getString(KEY_JSON, null) ?: return LocalCurrencyState()
        val root = Json.parseToJsonElement(raw).jsonObject
        val baseCurrency = root["baseCurrency"]?.jsonPrimitive?.content ?: "USD"
        val rates = root["rates"]?.jsonObject?.mapValues { (_, value) ->
            value.jsonPrimitive.content.toBigDecimal()
        }.orEmpty()
        return LocalCurrencyState(baseCurrency = baseCurrency, rates = rates)
    }

    fun save(state: LocalCurrencyState) {
        val json = buildJsonObject {
            put("baseCurrency", JsonPrimitive(state.baseCurrency))
            put(
                "rates",
                buildJsonObject {
                    state.rates.toSortedMap().forEach { (code, rate) ->
                        put(code, JsonPrimitive(rate.stripTrailingZeros().toPlainString()))
                    }
                },
            )
        }.toString()
        preferences.edit().putString(KEY_JSON, json).apply()
    }

    companion object {
        private const val KEY_JSON = "local_currency_json"
    }
}

data class LocalCurrencyState(
    val baseCurrency: String = "USD",
    val rates: Map<String, BigDecimal> = emptyMap(),
)
