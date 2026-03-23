package com.obscuracalc.core.calculator

import java.math.BigDecimal

class MemoryRegister {
    private var current: BigDecimal = BigDecimal.ZERO

    fun store(value: BigDecimal) {
        current = value
    }

    fun add(value: BigDecimal) {
        current = current.add(value)
    }

    fun subtract(value: BigDecimal) {
        current = current.subtract(value)
    }

    fun recall(): BigDecimal = current

    fun clear() {
        current = BigDecimal.ZERO
    }
}
