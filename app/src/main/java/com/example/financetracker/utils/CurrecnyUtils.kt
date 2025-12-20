package com.example.financetracker.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        return formatter.format(amount)
    }

    fun formatCurrencyWithoutSymbol(amount: Double): String {
        return String.format("%.2f", amount)
    }

    fun parseCurrency(input: String): Double? {
        return try {
            input.replace("[^0-9.]".toRegex(), "").toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}