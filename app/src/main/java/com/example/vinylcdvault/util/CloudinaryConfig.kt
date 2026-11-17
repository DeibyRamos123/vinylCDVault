package com.example.vinylcdvault.util

/**
 * Datos públicos de Cloudinary. Son seguros dentro de la app:
 * el "cloud name" y el preset sin firma no incluyen la clave secreta.
 */
object CloudinaryConfig {
    const val CLOUD_NAME = "hzuxozll"              // cámbialo por el de tu panel
    const val UPLOAD_PRESET = "vinylvault_products"     // el preset unsigned que creaste
}