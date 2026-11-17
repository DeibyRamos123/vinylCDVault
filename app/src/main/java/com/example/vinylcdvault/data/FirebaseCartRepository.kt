package com.example.vinylcdvault.data

import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Carrito guardado en Firestore, en users/{uid}/cart. */
class FirebaseCartRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CartRepository {

    // Subcolección del carrito del usuario actual; falla si no hay sesión
    private fun cart(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Inicia sesión para usar el carrito")
        return db.collection("users").document(uid).collection("cart")
    }

    override fun observeCart(): Flow<List<CartItem>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        // Listener en tiempo real: la lista se actualiza sola al agregar o quitar
        val registration = cart().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(
                snapshot?.documents
                    ?.mapNotNull { it.toObject(CartItem::class.java) }
                    ?.sortedBy { it.title.lowercase() }
                    ?: emptyList()
            )
        }
        awaitClose { registration.remove() }
    }

    override suspend fun add(product: Product) {
        val ref = cart().document(product.id)
        // Transacción: lee la cantidad actual y suma una unidad sin pisar cambios simultáneos
        db.runTransaction { tx ->
            val current = tx.get(ref).getLong("quantity") ?: 0L
            tx.set(
                ref,
                CartItem(
                    productId = product.id, title = product.title, artist = product.artist,
                    format = product.format, price = product.price, imageUrl = product.imageUrl,
                    sellerId = product.sellerId, quantity = (current + 1).toInt()
                )
            )
            Unit
        }.await()
    }

    override suspend fun changeQuantity(productId: String, quantity: Int) {
        cart().document(productId).update("quantity", quantity).await()
    }

    override suspend fun remove(productId: String) {
        cart().document(productId).delete().await()
    }
}