package com.example.vinylcdvault.data

import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.repository.AuthRepository
import com.example.vinylcdvault.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.example.vinylcdvault.model.Roles

/** Implementación de AuthRepository con Firebase Authentication y Firestore. */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    // Colección donde se guarda el perfil de cada usuario; el id del documento es su uid
    private val users get() = db.collection("users")

    override suspend fun register(
        name: String, email: String, phone: String, password: String, role: String
    ): User {
        // 1. Crea la cuenta (correo y contraseña) en Firebase Auth
        val uid = auth.createUserWithEmailAndPassword(email, password).await().user?.uid
            ?: throw IllegalStateException("No se pudo crear la cuenta")

        // 2. Guarda el perfil con el rol en Firestore
        val profile = User(name = name, email = email, role = role, active = true, uid = uid, phone = phone)
        try {
            users.document(uid).set(profile).await()
        } catch (e: Exception) {
            // Si falla el perfil, borra la cuenta recién creada para no dejarla a medias
            auth.currentUser?.delete()?.await()
            throw e
        }
        return profile
    }

    override suspend fun login(email: String, password: String): User {
        val uid = auth.signInWithEmailAndPassword(email, password).await().user?.uid
            ?: throw IllegalStateException("No se pudo iniciar sesión")

        // Lee el perfil para conocer el rol y si la cuenta está activa
        val profile = users.document(uid).get().await().toObject(User::class.java)
        if (profile == null) {
            auth.signOut()
            throw IllegalStateException("Tu perfil no existe. Contacta al administrador")
        }
        if (!profile.active || profile.deleted) {
            auth.signOut()
            throw IllegalStateException("Tu cuenta está suspendida")
        }
        return profile
    }

    override suspend fun loginWithGoogle(idToken: String): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val fbUser = auth.signInWithCredential(credential).await().user
                ?: return Resource.Error("No se pudo iniciar sesión")

            val ref = db.collection("users").document(fbUser.uid)
            val snap = ref.get().await()

            val user = if (snap.exists()) {
                snap.toObject(User::class.java)!!
            } else {
                User(
                    name = fbUser.displayName ?: "",
                    email = fbUser.email ?: "",
                    role = Roles.BUYER,
                    uid = fbUser.uid,
                    photoUrl = fbUser.photoUrl?.toString() ?: ""
                ).also { ref.set(it).await() }
            }

            if (!user.active || user.deleted) {
                auth.signOut()
                Resource.Error("Tu cuenta está desactivada")
            } else Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al iniciar con Google")
        }
    }

    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun currentProfile(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val profile = users.document(uid).get().await().toObject(User::class.java)?.copy(uid = uid)
        if (profile == null || !profile.active || profile.deleted) {
            auth.signOut()
            return null
        }
        return profile
    }

    override fun hasSession(): Boolean = auth.currentUser != null

    override fun logout() = auth.signOut()
}