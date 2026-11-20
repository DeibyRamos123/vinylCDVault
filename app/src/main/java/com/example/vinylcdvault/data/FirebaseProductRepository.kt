package com.example.vinylcdvault.data

import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Implementación de ProductRepository: datos en Firestore y fotos en Cloudinary. */
class FirebaseProductRepository(private val db: FirebaseFirestore) : ProductRepository {

    private val products get() = db.collection("products")

    override fun observeProducts(): Flow<List<Product>> = callbackFlow {
        // Listener en tiempo real: cada cambio en la colección emite una lista nueva
        val registration = products.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { doc -> doc.toObject(Product::class.java)?.copy(id = doc.id) }
                ?.sortedByDescending { it.createdAt }
                ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun saveProduct(product: Product, imageBytes: ByteArray?) {
        if (product.id.isEmpty()) {
            // Crear: se sube la foto (si hay) y luego se guarda el disco con su URL
            val ref = products.document()
            val url = imageBytes?.let { CloudinaryUploader.upload(it) } ?: ""
            ref.set(product.copy(id = ref.id, imageUrl = url, createdAt = System.currentTimeMillis())).await()
        } else {
            // Editar: solo se actualizan los campos del formulario; la foto solo si se eligió una nueva
            val data = mutableMapOf<String, Any>(
                "title" to product.title,
                "artist" to product.artist,
                "format" to product.format,
                "price" to product.price,
                "condition" to product.condition,
                "description" to product.description,
                "genre" to product.genre,
            )
            if (imageBytes != null) data["imageUrl"] = CloudinaryUploader.upload(imageBytes)
            products.document(product.id).update(data).await()
        }
    }

    override suspend fun deleteProduct(id: String) {
        // Se elimina el documento. La foto queda en Cloudinary (ver nota al final)
        products.document(id).delete().await()
    }
}