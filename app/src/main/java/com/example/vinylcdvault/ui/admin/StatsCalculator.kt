package com.example.vinylcdvault.ui.admin

import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.model.OrderStatus
import com.example.vinylcdvault.model.PaymentStatus
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.model.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Cifras del panel principal del administrador. */
data class DashboardStats(
    val users: Int,
    val suspendedUsers: Int,
    val products: Int,
    val productsWithoutPhoto: Int,
    val ordersThisMonth: Int,
    val salesThisMonth: Long,
    val ordersToShip: Int,
    val ordersShipped: Int,
    val recentOrders: List<Order>
)

/** Ventas de un mes, para la gráfica de barras. */
data class MonthSales(val label: String, val total: Long)

/** Disco con más unidades vendidas. */
data class TopProduct(val title: String, val units: Int)

/** Cifras de la pantalla de reportes (últimos 6 meses). */
data class ReportStats(
    val totalSales: Long,
    val orderCount: Int,
    val months: List<MonthSales>,
    val topProducts: List<TopProduct>
)

/** Cifras del perfil del vendedor. */
data class SellerStats(val products: Int, val unitsSold: Int, val revenue: Long)

/** Calcula las estadísticas a partir de usuarios, discos y pedidos. Solo cuentan los pedidos pagados. */
object StatsCalculator {

    // Primer instante del mes, restando "monthsAgo" meses (0 = mes actual)
    private fun monthStart(monthsAgo: Int): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MONTH, -monthsAgo)
    }.timeInMillis

    /** Discos publicados, unidades vendidas e ingresos de un vendedor, contando solo pedidos pagados. */
    fun seller(sellerId: String, products: List<Product>, orders: List<Order>): SellerStats {
        val items = orders.paid().flatMap { it.items }.filter { it.sellerId == sellerId }
        return SellerStats(
            products = products.count { it.sellerId == sellerId },
            unitsSold = items.sumOf { it.quantity },
            revenue = items.sumOf { it.price * it.quantity }
        )
    }
    private fun List<Order>.paid() = filter { it.paymentStatus == PaymentStatus.PAID }

    fun dashboard(users: List<User>, products: List<Product>, orders: List<Order>): DashboardStats {
        val paid = orders.paid()
        val thisMonth = paid.filter { it.createdAt >= monthStart(0) }
        return DashboardStats(
            users = users.size,
            suspendedUsers = users.count { !it.active },
            products = products.size,
            productsWithoutPhoto = products.count { it.imageUrl.isBlank() },
            ordersThisMonth = thisMonth.size,
            salesThisMonth = thisMonth.sumOf { it.total },
            ordersToShip = paid.count { it.status == OrderStatus.PENDING },
            ordersShipped = paid.count { it.status == OrderStatus.SHIPPED },
            recentOrders = paid.sortedByDescending { it.createdAt }.take(3)
        )
    }

    fun report(orders: List<Order>): ReportStats {
        val windowStart = monthStart(5)
        val inWindow = orders.paid().filter { it.createdAt >= windowStart }
        val monthFormat = SimpleDateFormat("MMM", Locale("es", "CO"))

        // Seis meses, del más antiguo al actual
        val months = (5 downTo 0).map { ago ->
            val from = monthStart(ago)
            val to = if (ago == 0) Long.MAX_VALUE else monthStart(ago - 1)
            MonthSales(
                label = monthFormat.format(Date(from)).replaceFirstChar { it.uppercase() },
                total = inWindow.filter { it.createdAt in from until to }.sumOf { it.total }
            )
        }

        // Discos más vendidos: se suman las unidades de cada disco en todos los pedidos
        val top = inWindow.flatMap { it.items }
            .groupBy { it.productId }
            .map { (_, items) -> TopProduct(items.first().title, items.sumOf { it.quantity }) }
            .sortedByDescending { it.units }
            .take(3)

        return ReportStats(inWindow.sumOf { it.total }, inWindow.size, months, top)
    }
}