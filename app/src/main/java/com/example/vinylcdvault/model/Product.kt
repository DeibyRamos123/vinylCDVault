package com.example.vinylcdvault.model

import android.content.Context
import android.content.Intent
import com.example.vinylcdvault.ui.buyer.ProductDetailActivity
import java.io.Serializable

/**
 * Disco publicado en la tienda (colección "products" de Firestore).
 * Todos los campos tienen valor por defecto porque Firestore necesita un
 * constructor vacío para convertir el documento en objeto.
 */
data class Product(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val format: String = "",
    val price: Long = 0,            // precio en pesos colombianos, sin decimales
    val condition: String = "",
    val description: String = "",
    val imageUrl: String = "",      // se llena cuando se suba la foto (parte B)
    val sellerId: String = "",      // uid del vendedor que lo publicó
    val createdAt: Long = 0         // fecha de creación en milisegundos
) : Serializable

/** Datos de ejemplo que usan el carrito y los favoritos hasta que se conecten a Firestore. */
object SampleData {
    val products = listOf(
        Product("1", "Random Access Memories", "Daft Punk", "Vinilo • LP", 189900, "Nuevo (sellado)", "Edición en doble vinilo de 180 g del álbum ganador del Grammy."),
        Product("2", "Kill 'Em All", "Metallica", "Vinilo • LP", 159900, "Near Mint (NM)", "Prensado original en excelente estado, portada sin marcas."),
        Product("3", "Kind of Blue", "Miles Davis", "Vinilo • LP", 139900, "Very Good Plus (VG+)", "El clásico del jazz modal. Ligeras marcas de uso en el sobre."),
        Product("4", "Discovery", "Daft Punk", "CD • Remasterizado", 59900, "Nuevo (sellado)", "Edición remasterizada con libreto original."),
        Product("5", "Black Album", "Metallica", "CD • Deluxe", 79900, "Near Mint (NM)", "Edición deluxe con folleto ampliado."),
        Product("6", "Nevermind", "Nirvana", "CD • Estándar", 49900, "Very Good Plus (VG+)", "Edición estándar, disco sin rayones.")
    )
    val cart = listOf(products[0], products[3])
}

/** Atajo para abrir la pantalla de detalle de un disco. */
fun Context.openProductDetail(product: Product) {
    startActivity(Intent(this, ProductDetailActivity::class.java).putExtra("product", product))
}