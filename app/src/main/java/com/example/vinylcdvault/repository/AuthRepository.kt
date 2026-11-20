package com.example.vinylcdvault.repository

import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.util.Resource

/**
 * Contrato de autenticación. Los ViewModels dependen de esta interfaz,
 * no de Firebase, así la fuente de datos se puede cambiar sin tocar las pantallas.
 */
interface AuthRepository {
    /** Crea la cuenta en Firebase Auth y guarda el perfil con su rol en Firestore. */
    suspend fun register(name: String, email: String, phone: String, password: String, role: String): User

    /** Inicia sesión y devuelve el perfil (con rol). Falla si la cuenta está suspendida. */
    suspend fun login(email: String, password: String): User

    /** Envía el correo para restablecer la contraseña. */
    suspend fun sendPasswordReset(email: String)

    /** Perfil del usuario con sesión abierta, o null si no hay sesión. */
    suspend fun currentProfile(): User?

    /** Indica si hay una sesión guardada en el dispositivo. */
    fun hasSession(): Boolean

    suspend fun loginWithGoogle(idToken: String): Resource<User>

    /** Cierra la sesión. */
    fun logout()
}