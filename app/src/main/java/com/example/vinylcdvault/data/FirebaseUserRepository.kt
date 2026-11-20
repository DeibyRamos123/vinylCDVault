package com.example.vinylcdvault.data

import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.repository.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Implementación de UserRepository sobre Firestore y Firebase Auth. */
class FirebaseUserRepository(private val db: FirebaseFirestore) : UserRepository {

    private val users get() = db.collection("users")

    override fun observeUsers(): Flow<List<User>> = callbackFlow {
        // Listener en tiempo real: cada cambio en la colección emite una lista nueva
        val registration = users.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { doc -> doc.toObject(User::class.java)?.copy(uid = doc.id) }
                ?.filter { !it.deleted }                 // oculta los eliminados lógicamente
                ?.sortedBy { it.name.lowercase() }
                ?: emptyList()
            trySend(list)
        }
        // Al dejar de observar, se libera el listener para no gastar recursos
        awaitClose { registration.remove() }
    }

    override suspend fun createUser(
        name: String, email: String, password: String, role: String, active: Boolean
    ) {
        // Segunda instancia de Firebase: crear la cuenta aquí no afecta la sesión del administrador
        val primary = FirebaseApp.getInstance()
        val secondary = try {
            FirebaseApp.getInstance(SECONDARY_APP)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(primary.applicationContext, primary.options, SECONDARY_APP)
        }
        val secondaryAuth = FirebaseAuth.getInstance(secondary)

        val uid = secondaryAuth.createUserWithEmailAndPassword(email, password).await().user?.uid
            ?: throw IllegalStateException("No se pudo crear la cuenta")
        try {
            users.document(uid)
                .set(User(name = name, email = email, role = role, active = active, uid = uid))
                .await()
        } catch (e: Exception) {
            // Si falla el perfil, se borra la cuenta recién creada para no dejarla a medias
            secondaryAuth.currentUser?.delete()?.await()
            throw e
        } finally {
            secondaryAuth.signOut()
        }
    }

    override suspend fun updateUser(uid: String, name: String, role: String, active: Boolean) {
        val ref = users.document(uid)
        val wasActive = ref.get().await().getBoolean("active") ?: true
        ref.update(mapOf("name" to name, "role" to role, "active" to active)).await()
        // Si cambió el estado de la cuenta, sus discos se ocultan o se vuelven a mostrar en el catálogo
        if (wasActive != active) setProductsHidden(uid, hidden = !active)
    }

    override suspend fun deleteUser(uid: String) {
        users.document(uid).update(mapOf("deleted" to true, "active" to false)).await()
        // Un usuario eliminado no debe dejar discos visibles
        setProductsHidden(uid, hidden = true)
    }


    override suspend fun getUser(uid: String): User? =
        users.document(uid).get().await().toObject(User::class.java)
            ?.copy(uid = uid)
            ?.takeIf { !it.deleted }

    override suspend fun updatePhoto(uid: String, imageBytes: ByteArray) {
        // La foto se sube a Cloudinary y solo se guarda su dirección en Firestore
        val url = CloudinaryUploader.upload(imageBytes)
        users.document(uid).update("photoUrl", url).await()
    }

    // Oculta o muestra todos los discos de un vendedor (Firestore permite 500 escrituras por batch)
    private suspend fun setProductsHidden(sellerId: String, hidden: Boolean) {
        val docs = db.collection("products").whereEqualTo("sellerId", sellerId).get().await().documents
        docs.chunked(400).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.update(it.reference, "hidden", hidden) }
            batch.commit().await()
        }
    }

    private companion object {
        const val SECONDARY_APP = "secondary"
    }
}