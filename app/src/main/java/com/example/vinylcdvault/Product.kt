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
    val description: String,
    val imageRes: Int
) : Serializable

object SampleData {
    val products = listOf(
        Product("Random Access Memories", "Daft Punk", "Vinilo • LP", "$189.900", "Nuevo (sellado)", "Edición en doble vinilo de 180 g del álbum ganador del Grammy.", R.drawable.img_ram),
        Product("Kill 'Em All", "Metallica", "Vinilo • LP", "$159.900", "Near Mint (NM)", "Prensado original en excelente estado, portada sin marcas.", R.drawable.img_metallica),
        Product("Amores lejanos", "Enanitos Verdes", "CD • Estándar", "$139.900", "Very Good Plus (VG+)", "El clásico del rock. Ligeras marcas de uso en el sobre.", R.drawable.img_enanitos),
        Product("Discovery", "Daft Punk", "CD • Remasterizado", "$59.900", "Nuevo (sellado)", "Edición remasterizada con libreto original.", R.drawable.img_discovery),
        Product("Reinkaos", "Dissection", "CD • Deluxe", "$79.900", "Near Mint (NM)", "Edición deluxe con folleto ampliado.", R.drawable.img_reinkaos),
        Product("Nevermind", "Nirvana", "CD • Estándar", "$49.900", "Very Good Plus (VG+)", "Edición estándar, disco sin rayones.", R.drawable.img_nevermind)
    )
    val cart = listOf(products[0], products[3])
}

fun Context.openProductDetail(product: Product) {
    startActivity(Intent(this, ProductDetailActivity::class.java).putExtra("product", product))
}