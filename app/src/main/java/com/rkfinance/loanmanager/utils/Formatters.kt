package com.rkfinance.loanmanager.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun Double.formatCurrency(locale: Locale = Locale.getDefault()): String {
    val formatter = NumberFormat.getCurrencyInstance(locale)
    return formatter.format(this)
}

fun Long.toFormattedDate(pattern: String = "dd MMM yyyy"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}