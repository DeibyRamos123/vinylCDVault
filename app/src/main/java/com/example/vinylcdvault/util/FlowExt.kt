package com.example.vinylcdvault.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Convierte un Flow de datos en un StateFlow de Resource: cargando, éxito o error. */
fun <T> Flow<T>.asResource(scope: CoroutineScope): StateFlow<Resource<T>> =
    map<T, Resource<T>> { Resource.Success(it) }
        .catch { emit(Resource.Error(it.toFriendlyMessage())) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), Resource.Loading)