package com.example.vinylcdvault.repository

import com.example.vinylcdvault.model.Address
import kotlinx.coroutines.flow.Flow

/** Contrato de las direcciones guardadas del comprador. */
interface AddressRepository {
    /** Direcciones en tiempo real; la predeterminada va primero. */
    fun observeAddresses(): Flow<List<Address>>

    /** Crea la dirección (la primera queda como predeterminada) o actualiza la existente. */
    suspend fun save(address: Address)

    /** Deja una sola dirección como predeterminada. */
    suspend fun setPrimary(id: String)

    /** Elimina la dirección; si era la predeterminada, otra ocupa su lugar. */
    suspend fun delete(id: String)
}