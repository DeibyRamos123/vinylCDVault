package com.example.vinylcdvault.model

import java.io.Serializable

/** Dirección de entrega guardada por el comprador (users/{uid}/addresses). */
data class Address(
    val id: String = "",
    val label: String = "",          // nombre corto: Casa, Trabajo...
    val address: String = "",
    val latitude: Double = 0.0,      // 0 si no se capturó con la ubicación
    val longitude: Double = 0.0,
    val primary: Boolean = false     // la dirección que se propone al pagar
) : Serializable