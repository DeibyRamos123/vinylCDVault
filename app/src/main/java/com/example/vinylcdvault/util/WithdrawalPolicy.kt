package com.example.vinylcdvault.util

import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.model.OrderStatus
import com.example.vinylcdvault.model.WithdrawalStatus
import java.util.Calendar

/**
 * Regla del derecho de retracto (Ley 1480 de 2011, art. 47): el comprador puede retractarse
 * dentro de los 5 días hábiles siguientes a la entrega. Antes de la entrega se permite cancelar.
 */
object WithdrawalPolicy {
    private const val BUSINESS_DAYS = 5

    enum class Check { ALLOWED, ALREADY_REQUESTED, EXPIRED }

    /** Fecha base del plazo: la entrega; si no está registrada, la fecha del pedido. */
    fun baseDate(order: Order): Long = if (order.deliveredAt > 0) order.deliveredAt else order.createdAt

    /**
     * Fin del quinto día hábil (lunes a viernes) después de la fecha dada.
     * Limitación: no descuenta festivos.
     */
    fun deadline(from: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = from }
        var counted = 0
        while (counted < BUSINESS_DAYS) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val day = cal.get(Calendar.DAY_OF_WEEK)
            if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) counted++
        }
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    /** Indica si el pedido puede solicitar retracto o cancelación en este momento. */
    fun check(order: Order, now: Long = System.currentTimeMillis()): Check = when {
        order.withdrawalStatus == WithdrawalStatus.REQUESTED -> Check.ALREADY_REQUESTED
        order.status != OrderStatus.DELIVERED -> Check.ALLOWED          // aún no entregado: cancelación
        now <= deadline(baseDate(order)) -> Check.ALLOWED
        else -> Check.EXPIRED
    }
}