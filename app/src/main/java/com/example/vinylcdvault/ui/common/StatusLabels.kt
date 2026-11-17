package com.example.vinylcdvault.ui.common

import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.OrderStatus

/** Texto que se muestra para cada estado de pedido. */
fun orderStatusRes(status: String): Int = when (status) {
    OrderStatus.SHIPPED -> R.string.status_shipped
    OrderStatus.DELIVERED -> R.string.status_delivered
    else -> R.string.status_pending
}