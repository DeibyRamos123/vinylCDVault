package com.example.vinylcdvault.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import kotlin.coroutines.resume

/** Obtiene la ubicación actual del dispositivo y la convierte en una dirección legible. */
object LocationHelper {

    private const val TAG = "Location"   // etiqueta para filtrar el Logcat
    private const val LOCATION_TIMEOUT_MS = 15_000L
    private val http = OkHttpClient()

    /**
     * Ubicación actual: intenta una lectura nueva (hasta 15 s) y, si no llega, usa la última
     * ubicación conocida del teléfono. Devuelve null si el GPS está apagado y no hay ninguna.
     * Requiere que el permiso ya esté concedido.
     */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(context: Context): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)

        // 1) Rápida: Wi-Fi + red móvil + GPS (funciona adentro)
        readOnce(client, Priority.PRIORITY_BALANCED_POWER_ACCURACY, 8_000L)?.let { return it }

        // 2) GPS puro, por si la anterior no pudo
        readOnce(client, Priority.PRIORITY_HIGH_ACCURACY, LOCATION_TIMEOUT_MS)?.let { return it }

        // 3) Última conocida como último recurso
        Log.w(TAG, "Sin lectura nueva; se usa la última ubicación conocida")
        return try {
            client.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun readOnce(
        client: FusedLocationProviderClient,
        priority: Int,
        timeoutMs: Long
    ): Location? {
        val cancel = CancellationTokenSource()
        return try {
            withTimeoutOrNull(timeoutMs) {
                client.getCurrentLocation(priority, cancel.token).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Lectura con prioridad $priority falló", e)
            null
        } finally {
            cancel.cancel()
        }
    }
    /**
     * Convierte coordenadas en una dirección de texto. Primero usa el Geocoder del sistema y,
     * si no está disponible o falla en este teléfono, consulta OpenStreetMap. Null si ninguno responde.
     */
    suspend fun addressFor(context: Context, latitude: Double, longitude: Double): String? =
        systemAddress(context, latitude, longitude)?.takeIf { it.isNotBlank() }
            ?: openStreetMapAddress(latitude, longitude)

    // Geocoder del sistema: en Android 13+ se usa la versión asíncrona; antes, la clásica en segundo plano
    private suspend fun systemAddress(context: Context, lat: Double, lon: Double): String? {
        if (!Geocoder.isPresent()) {
            Log.w(TAG, "Este teléfono no tiene servicio de Geocoder")
            return null
        }
        val geocoder = Geocoder(context, Locale("es", "CO"))
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses.firstOrNull()?.getAddressLine(0))
                        }

                        override fun onError(errorMessage: String?) {
                            Log.w(TAG, "Geocoder con error: $errorMessage")
                            continuation.resume(null)
                        }
                    })
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.getAddressLine(0)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder falló", e)
            null
        }
    }

    // Respaldo con Nominatim (OpenStreetMap). Su política exige un User-Agent que identifique la app
    private suspend fun openStreetMapAddress(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$lat&lon=$lon&zoom=18&accept-language=es")
            .header("User-Agent", "VinylCDVault/1.0 (proyecto academico Android)")
            .build()
        try {
            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Nominatim respondió con código ${response.code}")
                    return@withContext null
                }
                // display_name trae la dirección completa en un solo texto
                JSONObject(response.body?.string().orEmpty()).optString("display_name").takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Nominatim falló", e)
            null
        }
    }
}