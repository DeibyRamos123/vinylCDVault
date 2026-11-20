package com.example.vinylcdvault.data

import com.example.vinylcdvault.model.Address
import com.example.vinylcdvault.repository.AddressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Direcciones guardadas en Firestore, en users/{uid}/addresses. */
class FirebaseAddressRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AddressRepository {

    // Subcolección del usuario actual; falla si no hay sesión
    private fun addresses(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Inicia sesión para usar tus direcciones")
        return db.collection("users").document(uid).collection("addresses")
    }

    override fun observeAddresses(): Flow<List<Address>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = addresses().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(
                snapshot?.documents
                    ?.mapNotNull { it.toObject(Address::class.java)?.copy(id = it.id) }
                    ?.sortedWith(compareByDescending<Address> { it.primary }.thenBy { it.label.lowercase() })
                    ?: emptyList()
            )
        }
        awaitClose { registration.remove() }
    }

    override suspend fun save(address: Address) {
        val col = addresses()
        if (address.id.isEmpty()) {
            // La primera dirección que se guarda queda como predeterminada
            val isFirst = col.limit(1).get().await().isEmpty
            val ref = col.document()
            ref.set(address.copy(id = ref.id, primary = isFirst)).await()
        } else {
            col.document(address.id).update(
                mapOf(
                    "label" to address.label,
                    "address" to address.address,
                    "latitude" to address.latitude,
                    "longitude" to address.longitude
                )
            ).await()
        }
    }

    override suspend fun setPrimary(id: String) {
        val col = addresses()
        // Un solo batch: la elegida pasa a true y todas las demás a false
        val batch = db.batch()
        col.get().await().documents.forEach { batch.update(it.reference, "primary", it.id == id) }
        batch.commit().await()
    }

    override suspend fun delete(id: String) {
        val col = addresses()
        col.document(id).delete().await()
        // Si se borró la predeterminada, la primera que quede pasa a serlo
        val rest = col.get().await().documents
        if (rest.isNotEmpty() && rest.none { it.getBoolean("primary") == true }) {
            col.document(rest.first().id).update("primary", true).await()
        }
    }
}