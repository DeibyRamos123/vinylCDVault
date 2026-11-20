package com.example.vinylcdvault.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Convierte un número en texto con formato de pesos colombianos, por ejemplo 189900 → "$189.900". */
fun Long.toCop(): String =
    "$" + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(this)

/** Convierte milisegundos en una fecha legible, por ejemplo "17 nov 2026". */
fun Long.toDateText(): String =
    SimpleDateFormat("dd MMM yyyy", Locale("es", "CO")).format(Date(this))

/** Monto corto para espacios pequeños: 12400000 → "$12,4 M", 85000 → "$85 mil". */
fun Long.toCompactCop(): String = when {
    this >= 1_000_000 -> "$" + String.format(Locale("es", "CO"), "%.1f", this / 1_000_000.0) + " M"
    this >= 1_000 -> "$" + (this / 1_000) + " mil"
    else -> "$" + this
}