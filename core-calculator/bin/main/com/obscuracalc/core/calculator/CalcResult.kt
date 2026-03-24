package com.obscuracalc.core.calculator

import java.math.BigDecimal

data class CalcResult(
    val expression: String,
    val value: BigDecimal?,
    val formattedValue: String,
    val error: String? = null,
) {
    val isSuccess: Boolean = error == null
}
