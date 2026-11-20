package com.example.vinylcdvault.repository

import kotlinx.coroutines.flow.Flow

/** Contrato de los discos favoritos del comprador con sesión abierta. */
interface FavoriteRepository {
    /** Ids de los discos favoritos, en tiempo real. */
    fun observeIds(): Flow<Set<String>>

    /** Marca o desmarca un disco como favorito. */
    suspend fun setFavorite(productId: String, favorite: Boolean)
}