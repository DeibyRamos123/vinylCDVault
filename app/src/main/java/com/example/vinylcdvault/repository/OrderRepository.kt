package com.example.vinylcdvault.repository

import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.model.Order
import kotlinx.coroutines.flow.Flow

/** Contrato para crear y consultar pedidos. */
interface OrderRepository {
    /** Crea el pedido con los discos del carrito, vacía el carrito y devuelve el id del pedido. */
    suspend fun placeOrder(
        items: List<CartItem>, address: String, latitude: Double, longitude: Double,
        paymentReference: String, transactionId: String
    ): String

    suspend fun requestWithdrawal(orderId: String)

    /** Pedidos del comprador con sesión abierta. */
    fun observeMyOrders(): Flow<List<Order>>

    /** Pedidos que incluyen discos del vendedor con sesión abierta. */
    fun observeSellerOrders(): Flow<List<Order>>

    /** Cambia el estado de entrega de un pedido. */
    suspend fun updateStatus(orderId: String, status: String)

    /** Todos los pedidos de la tienda (vista del administrador). */
    fun observeAllOrders(): Flow<List<Order>>

    /** El vendedor acepta o rechaza una solicitud de retracto. Al aceptar, el pago queda con reembolso pendiente. */
    suspend fun resolveWithdrawal(orderId: String, accept: Boolean)

}