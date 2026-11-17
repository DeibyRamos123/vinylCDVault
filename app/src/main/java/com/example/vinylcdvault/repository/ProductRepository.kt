package com.example.vinylcdvault.repository

import com.example.vinylcdvault.model.Product
import kotlinx.coroutines.flow.Flow

/** Contrato para gestionar los discos de la tienda. */
interface ProductRepository {
    /** Lista de discos en tiempo real, del más reciente al más antiguo. */
    fun observeProducts(): Flow<List<Product>>

    /** Crea el disco si no tiene id, o actualiza el existente. Si llegan bytes, sube la foto. */
    suspend fun saveProduct(product: Product, imageBytes: ByteArray?)

    /** Elimina el disco. */
    suspend fun deleteProduct(id: String)
}