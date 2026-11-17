package com.example.vinylcdvault.di

import com.example.vinylcdvault.data.FirebaseAuthRepository
import com.example.vinylcdvault.data.FirebaseProductRepository
import com.example.vinylcdvault.data.FirebaseUserRepository
import com.example.vinylcdvault.repository.AuthRepository
import com.example.vinylcdvault.repository.ProductRepository
import com.example.vinylcdvault.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Contenedor de dependencias manual. Crea una sola vez los servicios de Firebase
 * y los repositorios, y los entrega a los ViewModels.
 */
object AppContainer {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository(auth, firestore) }

    val userRepository: UserRepository by lazy { FirebaseUserRepository(firestore) }

    val productRepository: ProductRepository by lazy { FirebaseProductRepository(firestore) }

}