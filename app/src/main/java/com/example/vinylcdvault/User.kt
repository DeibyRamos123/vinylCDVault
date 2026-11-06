package com.example.vinylcdvault

import java.io.Serializable

data class User(
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean
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