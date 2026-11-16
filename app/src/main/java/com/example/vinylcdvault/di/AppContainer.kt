package com.example.vinylcdvault.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Contenedor de dependencias manual de la app.
 * Centraliza las instancias de Firebase para que las pantallas
 * no las creen directamente (principio de inversión de dependencias).
 */
object AppContainer {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}