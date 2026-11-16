package com.example.vinylcdvault.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/** Traduce una excepción técnica a un mensaje claro para el usuario. */
fun Throwable.toFriendlyMessage(): String = when (this) {
    is FirebaseAuthUserCollisionException -> "Ese correo ya está registrado"
    // WeakPassword hereda de InvalidCredentials, por eso va antes
    is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil"
    is FirebaseAuthInvalidCredentialsException,
    is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos"
    is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento"
    is FirebaseNetworkException -> "Sin conexión a internet"
    is IllegalStateException -> message ?: "Ocurrió un error"
    else -> "Ocurrió un error. Intenta de nuevo"
}