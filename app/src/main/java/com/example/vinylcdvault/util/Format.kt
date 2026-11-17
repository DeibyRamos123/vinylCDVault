package com.example.vinylcdvault.util

import java.text.NumberFormat
import java.util.Locale

/** Convierte un número en texto con formato de pesos colombianos, por ejemplo 189900 → "$189.900". */
fun Long.toCop(): String =
    "$" + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(this)