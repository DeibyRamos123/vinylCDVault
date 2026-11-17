package com.example.vinylcdvault.model

import java.io.Serializable

/** Disco dentro del carrito. También se guarda tal cual dentro de cada pedido. */
data class CartItem(
    val productId: String = "",
    val title: String = "",
    val artist: String = "",
    val format: String = "",
    val price: Long = 0,        // precio unitario en pesos
    val imageUrl: String = "",
    val sellerId: String = "",
    val quantity: Int = 1
) : Serializable