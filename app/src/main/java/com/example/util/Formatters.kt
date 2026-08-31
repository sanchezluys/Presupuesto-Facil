package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val decimalFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.getDefault()))
    private val integerFormat = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.getDefault()))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateLongFormat = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "ES"))

    fun formatMoney(amount: Double, currency: String = "$"): String {
        return "$currency ${decimalFormat.format(amount)}"
    }

    fun formatNumber(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            integerFormat.format(amount)
        } else {
            decimalFormat.format(amount)
        }
    }

    fun formatDate(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    fun formatDateLong(millis: Long): String {
        return try {
            dateLongFormat.format(Date(millis))
        } catch (e: Exception) {
            formatDate(millis)
        }
    }
}
