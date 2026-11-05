package com.example.vinylcdvault

data class Order(
    val id: String,
    val buyer: String,
    val items: String,
    val date: String,
    val total: String,
    val status: String
)

object SampleOrders {
    val orders = listOf(
        Order("Pedido #1042", "Ana Torres", "Random Access Memories (Vinilo)", "03 nov 2026", "\$189.900", "Pendiente"),
        Order("Pedido #1039", "Carlos Gómez", "Kill 'Em All (Vinilo) + Nevermind (CD)", "01 nov 2026", "\$209.800", "Enviado"),
        Order("Pedido #1031", "Laura Pérez", "Discovery (CD)", "28 oct 2026", "\$59.900", "Entregado"),
        Order("Pedido #1025", "Mateo Ruiz", "Kind of Blue (Vinilo)", "25 oct 2026", "\$139.900", "Entregado")
    )
}