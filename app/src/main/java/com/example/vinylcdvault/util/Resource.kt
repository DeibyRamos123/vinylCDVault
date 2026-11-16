package com.example.vinylcdvault.util

/** Representa el estado de una operación: cargando, éxito con datos, o error con mensaje. */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}