package com.example.vinylcdvault.util

import com.example.vinylcdvault.model.CartItem

/** Reglas de costos de la compra: envío fijo e IVA del 19% sobre el subtotal. */
object Pricing {
    private const val SHIPPING_COP = 12000L
    private const val TAX_PERCENT = 19L

    fun subtotal(items: List<CartItem>): Long = items.sumOf { it.price * it.quantity }

    /** Sin discos no hay envío. */
    fun shipping(items: List<CartItem>): Long = if (items.isEmpty()) 0L else SHIPPING_COP

    fun tax(subtotal: Long): Long = subtotal * TAX_PERCENT / 100

    fun total(items: List<CartItem>): Long {
        val sub = subtotal(items)
        return sub + shipping(items) + tax(sub)
    }
}