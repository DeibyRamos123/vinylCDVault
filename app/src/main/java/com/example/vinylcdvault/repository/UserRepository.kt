package com.example.vinylcdvault.repository

import com.example.vinylcdvault.model.User
import kotlinx.coroutines.flow.Flow

/** Contrato para gestionar usuarios desde el panel del administrador. */
interface UserRepository {
    /** Lista de usuarios en tiempo real (se actualiza sola cuando cambia Firestore). */
    fun observeUsers(): Flow<List<User>>

    /** Crea la cuenta en Auth y su perfil en Firestore sin cerrar la sesión del administrador. */
    suspend fun createUser(name: String, email: String, password: String, role: String, active: Boolean)

    /** Actualiza nombre, rol y estado de un usuario existente. */
    suspend fun updateUser(uid: String, name: String, role: String, active: Boolean)

    /** Eliminación lógica: marca al usuario como eliminado e inactivo. */
    suspend fun deleteUser(uid: String)

    /** Perfil de cualquier usuario por su id; null si no existe o fue eliminado. */
    suspend fun getUser(uid: String): User?

    /** Sube la foto de perfil (ya comprimida) y guarda su URL en el perfil del usuario. */
    suspend fun updatePhoto(uid: String, imageBytes: ByteArray)
}