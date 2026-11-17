package com.example.vinylcdvault.repository

import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.model.Product
import kotlinx.coroutines.flow.Flow

/** Contrato del carrito del comprador con sesión abierta. */
interface CartRepository {
    /** Discos del carrito en tiempo real. */
    fun observeCart(): Flow<List<CartItem>>

    /** Agrega un disco; si ya estaba, suma una unidad. */
    suspend fun add(product: Product)

    suspend fun changeQuantity(productId: String, quantity: Int)

    suspend fun remove(productId: String)
}