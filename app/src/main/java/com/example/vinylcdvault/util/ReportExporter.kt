package com.example.vinylcdvault.util

import com.example.vinylcdvault.model.Order

/** Convierte los pedidos en un archivo CSV que Excel o Google Sheets pueden abrir. */
object ReportExporter {

    fun toCsv(orders: List<Order>): String {
        val header = "pedido,fecha,comprador,total_cop,pago,envio,discos"
        val rows = orders.sortedBy { it.createdAt }.map { o ->
            listOf(
                o.id, o.createdAt.toDateText(), o.buyerName, o.total.toString(),
                o.paymentStatus, o.status,
                o.items.joinToString(" | ") { "${it.title} x${it.quantity}" }
            ).joinToString(",") { quote(it) }
        }
        // El carácter inicial (BOM) hace que Excel respete las tildes
        return "\uFEFF" + (listOf(header) + rows).joinToString("\n")
    }

    // Encierra el valor entre comillas y duplica las comillas internas, como exige el formato CSV
    private fun quote(value: String) = "\"" + value.replace("\"", "\"\"") + "\""
}