package com.example.vinylcdvault.data

import com.example.vinylcdvault.util.CloudinaryConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/** Sube imágenes a Cloudinary con una petición HTTP multipart y un preset sin firma. */
object CloudinaryUploader {

    private val client = OkHttpClient()

    /** Sube la foto y devuelve su URL pública (https). Lanza IllegalStateException con un mensaje legible si falla. */
    suspend fun upload(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        // Cuerpo del formulario: el preset y el archivo
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
            .addFormDataPart("file", "disco.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/image/upload")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    // Cloudinary devuelve el motivo en {"error": {"message": "..."}}
                    val reason = runCatching { JSONObject(text).getJSONObject("error").getString("message") }
                        .getOrDefault("código ${response.code}")
                    throw IllegalStateException("No se pudo subir la imagen: $reason")
                }
                // secure_url es la dirección https de la imagen ya subida
                JSONObject(text).getString("secure_url")
            }
        } catch (e: IOException) {
            throw IllegalStateException("No se pudo subir la imagen. Revisa tu conexión")
        }
    }
}