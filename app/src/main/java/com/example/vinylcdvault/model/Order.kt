package com.example.vinylcdvault.model

import java.io.Serializable

/** Estados de entrega de un pedido (los cambia el vendedor). */
object OrderStatus {
    const val PENDING = "pending"
    const val SHIPPED = "shipped"
    const val DELIVERED = "delivered"
}

/** Estados del pago (la pasarela los actualizará en el siguiente paso). */
object PaymentStatus {
    const val PENDING = "pending"
    const val PAID = "paid"
    const val FAILED = "failed"
}

object WithdrawalStatus {
    const val NONE = ""
    const val REQUESTED = "requested"
}

/** Pedido guardado en Firestore (colección "orders"). */
data class Order(
    val id: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Long = 0,
    val shipping: Long = 0,
    val tax: Long = 0,
    val total: Long = 0,
    val address: String = "",
    val latitude: Double = 0.0,     // se llenan con la geolocalización
    val longitude: Double = 0.0,
    val paymentStatus: String = PaymentStatus.PENDING,
    val paymentReference: String = "",   // referencia única enviada a Wompi
    val transactionId: String = "",      // id de la transacción que devuelve Wompi
    val status: String = OrderStatus.PENDING,
    val sellerIds: List<String> = emptyList(),   // permite al vendedor encontrar sus pedidos
    val createdAt: Long = 0,
    val deliveredAt: Long = 0,                          // cuándo el vendedor marcó la entrega
    val withdrawalStatus: String = WithdrawalStatus.NONE,
    val withdrawalAt: Long = 0                          // cuándo se solicitó el retracto
) : Serializable