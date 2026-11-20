package com.example.vinylcdvault.util

/** Datos de Wompi en ambiente de PRUEBAS. El secreto de integridad solo debe estar aquí en el sandbox. */
object WompiConfig {
    const val PUBLIC_KEY = "pub_test_b80j31A4hoL7iSavbsXsQnoHTqnTwM5M"
    const val INTEGRITY_SECRET = "test_integrity_DlmXBfjjLWnMDC8N6fsyS4pu91jJhHbN"
    const val CURRENCY = "COP"
    const val CHECKOUT_URL = "https://checkout.wompi.co/p/"
    const val API_URL = "https://sandbox.wompi.co/v1"

    // Dirección a la que Wompi redirige al terminar; la app la intercepta, nunca llega a cargarse
    const val REDIRECT_URL = "https://vinylcdvault.app/pago-resultado"
}