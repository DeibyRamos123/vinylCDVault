package com.example.vinylcdvault.ui.common

import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.model.OrderStatus
import com.example.vinylcdvault.model.WithdrawalStatus

/** Texto del estado de un pedido; si tiene una solicitud de retracto, esa tiene prioridad. */

fun orderStatusRes(status: String): Int = when (status) {
    OrderStatus.SHIPPED -> R.string.status_shipped
    OrderStatus.DELIVERED -> R.string.status_delivered
    else -> R.string.status_pending
}

fun orderLabelRes(order: Order): Int = when (order.withdrawalStatus) {
    WithdrawalStatus.REQUESTED -> R.string.status_withdrawal
    WithdrawalStatus.ACCEPTED -> R.string.status_withdrawal_accepted
    WithdrawalStatus.REJECTED -> R.string.status_withdrawal_rejected
    else -> orderStatusRes(order.status)
}