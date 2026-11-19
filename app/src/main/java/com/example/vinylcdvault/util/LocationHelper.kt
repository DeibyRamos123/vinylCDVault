package com.example.vinylcdvault.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

/** Obtiene la ubicación actual del dispositivo y la convierte en una dirección legible. */
object LocationHelper {

    /** Ubicación actual, o null si el GPS está apagado. Requiere que el permiso ya esté concedido. */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(context: Context): Location? =
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .await()

    /** Convierte coordenadas en una dirección de texto (geocodificación inversa); null si no hay resultado. */
    suspend fun addressFor(context: Context, latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                Geocoder(context, Locale("es", "CO"))
                    .getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?.getAddressLine(0)
            } catch (e: Exception) {
                null   // sin internet o sin servicio de geocodificación: el usuario escribe la dirección
            }
        }
}