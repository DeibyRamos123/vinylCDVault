package com.example.vinylcdvault.data

import android.net.Uri
import com.example.vinylcdvault.util.WompiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest

/** Arma la dirección del checkout de Wompi y confirma el estado real de cada transacción. */
object WompiClient {

    const val APPROVED = "APPROVED"
    const val PENDING = "PENDING"

    private val client = OkHttpClient()

    /** Firma de integridad: SHA-256 de referencia + monto en centavos + moneda + secreto. */
    private fun signature(reference: String, amountInCents: Long): String {
        val raw = "$reference$amountInCents${WompiConfig.CURRENCY}${WompiConfig.INTEGRITY_SECRET}"
        return MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    /** Dirección del checkout alojado de Wompi para pagar este monto con esta referencia. */
    fun checkoutUrl(reference: String, amountInCents: Long): String =
        Uri.parse(WompiConfig.CHECKOUT_URL).buildUpon()
            .appendQueryParameter("public-key", WompiConfig.PUBLIC_KEY)
            .appendQueryParameter("currency", WompiConfig.CURRENCY)
            .appendQueryParameter("amount-in-cents", amountInCents.toString())
            .appendQueryParameter("reference", reference)
            .appendQueryParameter("signature:integrity", signature(reference, amountInCents))
            .appendQueryParameter("redirect-url", WompiConfig.REDIRECT_URL)
            .build()
            .toString()

    /**
     * Consulta la transacción en Wompi y devuelve su estado (APPROVED, DECLINED, ERROR...).
     * Si sigue PENDING, reintenta unas veces. Nunca se confía solo en la redirección.
     */
    suspend fun verify(transactionId: String, reference: String, amountInCents: Long): String =
        withContext(Dispatchers.IO) {
            repeat(5) {
                val status = fetchStatus(transactionId, reference, amountInCents)
                if (status != PENDING) return@withContext status
                delay(2000)
            }
            PENDING
        }

    // Pide la transacción a la API y comprueba que la referencia y el monto sean los esperados
    private fun fetchStatus(transactionId: String, reference: String, amountInCents: Long): String {
        val request = Request.Builder().url("${WompiConfig.API_URL}/transactions/$transactionId").get().build()
        try {
            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException("No se pudo verificar el pago (código ${response.code})")
                }
                val data = JSONObject(text).getJSONObject("data")
                if (data.getString("reference") != reference || data.getLong("amount_in_cents") != amountInCents) {
                    throw IllegalStateException("La transacción no coincide con el pedido")
                }
                return data.getString("status")
            }
        } catch (e: IOException) {
            throw IllegalStateException("Sin conexión al verificar el pago")
        }
    }
}