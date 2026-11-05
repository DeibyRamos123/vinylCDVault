package com.example.vinylcdvault

import android.content.Context
import android.content.Intent
import java.io.Serializable

data class Product(
    val title: String,
    val artist: String,
    val format: String,
    val price: String,
    val condition: String,
    val description: String
) : Serializable

object SampleData {
    val products = listOf(
        Product("Random Access Memories", "Daft Punk", "Vinilo • LP", "$189.900", "Nuevo (sellado)", "Edición en doble vinilo de 180 g del álbum ganador del Grammy."),
        Product("Kill 'Em All", "Metallica", "Vinilo • LP", "$159.900", "Near Mint (NM)", "Prensado original en excelente estado, portada sin marcas."),
        Product("Kind of Blue", "Miles Davis", "Vinilo • LP", "$139.900", "Very Good Plus (VG+)", "El clásico del jazz modal. Ligeras marcas de uso en el sobre."),
        Product("Discovery", "Daft Punk", "CD • Remasterizado", "$59.900", "Nuevo (sellado)", "Edición remasterizada con libreto original."),
        Product("Black Album", "Metallica", "CD • Deluxe", "$79.900", "Near Mint (NM)", "Edición deluxe con folleto ampliado."),
        Product("Nevermind", "Nirvana", "CD • Estándar", "$49.900", "Very Good Plus (VG+)", "Edición estándar, disco sin rayones.")
    )
    val cart = listOf(products[0], products[3])
}

fun Context.openProductDetail(product: Product) {
    startActivity(Intent(this, ProductDetailActivity::class.java).putExtra("product", product))
}