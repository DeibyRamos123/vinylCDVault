package com.example.vinylcdvault.model

import java.io.Serializable

/** Códigos de rol guardados en Firestore, en el campo "role" de users/{uid}. */
object Roles {
    const val BUYER = "buyer"
    const val SELLER = "seller"
    const val ADMIN = "admin"
}

/**
 * Perfil de usuario guardado en Firestore (colección "users").
 * Todos los campos tienen valor por defecto porque Firestore necesita
 * un constructor vacío para convertir el documento en objeto.
 */
data class User(
    val name: String = "",
    val email: String = "",
    val role: String = Roles.BUYER,
    val active: Boolean = true,
    val uid: String = "",
    val phone: String = ""
) : Serializable


object SampleUsers {
    val users = listOf(
        User("Ava Johnson", "ava@correo.com", "Comprador", true),
        User("Noah Kim", "vendedor@vinylworks.co", "Vendedor", true),
        User("Mia Rivera", "mia@correo.com", "Comprador", false),
        User("Ethan Blake", "admin@vinylvault.co", "Administrador", true),
        User("Sophia Chen", "sophia@correo.com", "Comprador", true)
    )
}