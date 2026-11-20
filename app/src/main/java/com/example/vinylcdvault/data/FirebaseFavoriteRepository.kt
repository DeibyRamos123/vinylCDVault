package com.example.vinylcdvault.data

import com.example.vinylcdvault.repository.FavoriteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Favoritos guardados en Firestore, en users/{uid}/favorites (el id del documento es el del disco). */
class FirebaseFavoriteRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FavoriteRepository {

    // Subcolección del usuario actual; falla si no hay sesión
    private fun favorites(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Inicia sesión para usar favoritos")
        return db.collection("users").document(uid).collection("favorites")
    }

    override fun observeIds(): Flow<Set<String>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }
        val registration = favorites().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.map { it.id }?.toSet() ?: emptySet())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun setFavorite(productId: String, favorite: Boolean) {
        val ref = favorites().document(productId)
        if (favorite) ref.set(mapOf("addedAt" to System.currentTimeMillis())).await()
        else ref.delete().await()
    }
}