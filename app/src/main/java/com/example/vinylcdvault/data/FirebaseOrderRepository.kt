package com.example.vinylcdvault.data

import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.model.PaymentStatus
import com.example.vinylcdvault.repository.OrderRepository
import com.example.vinylcdvault.util.Pricing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Pedidos guardados en la colección "orders" de Firestore. */
class FirebaseOrderRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : OrderRepository {

    private val orders get() = db.collection("orders")

    override suspend fun placeOrder(
        items: List<CartItem>, address: String, latitude: Double, longitude: Double,
        paymentReference: String, transactionId: String
    ): String {

        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Inicia sesión para comprar")
        if (items.isEmpty()) throw IllegalStateException("Tu carrito está vacío")

        // El nombre del comprador se toma de su perfil para mostrarlo al vendedor
        val buyerName = db.collection("users").document(uid).get().await().getString("name").orEmpty()
        val subtotal = Pricing.subtotal(items)
        val orderRef = orders.document()
        val order = Order(
            id = orderRef.id, buyerId = uid, buyerName = buyerName, items = items,
            subtotal = subtotal, shipping = Pricing.shipping(items), tax = Pricing.tax(subtotal),
            total = Pricing.total(items), address = address,
            latitude = latitude, longitude = longitude,
            paymentReference = paymentReference, transactionId = transactionId,
            paymentStatus = PaymentStatus.PAID,   // solo se llama cuando Wompi aprobó el pago
            sellerIds = items.map { it.sellerId }.filter { it.isNotBlank() }.distinct(),
            createdAt = System.currentTimeMillis()
        )

        // Un solo batch: crea el pedido y vacía el carrito, o no hace nada
        val batch = db.batch()
        batch.set(orderRef, order)
        items.forEach { item ->
            batch.delete(db.collection("users").document(uid).collection("cart").document(item.productId))
        }
        batch.commit().await()
        return orderRef.id
    }

    override fun observeMyOrders(): Flow<List<Order>> = observe { uid -> orders.whereEqualTo("buyerId", uid) }

    override fun observeSellerOrders(): Flow<List<Order>> = observe { uid -> orders.whereArrayContains("sellerIds", uid) }

    override suspend fun updateStatus(orderId: String, status: String) {
        orders.document(orderId).update("status", status).await()
    }

    // Escucha en tiempo real una consulta de pedidos y los ordena del más reciente al más antiguo
    private fun observe(query: (String) -> Query): Flow<List<Order>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = query(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(
                snapshot?.documents
                    ?.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
            )
        }
        awaitClose { registration.remove() }
    }
}