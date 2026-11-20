package com.example.vinylcdvault.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.model.WithdrawalStatus
import com.example.vinylcdvault.util.toCop
import com.example.vinylcdvault.util.toDateText

/**
 * Lista de pedidos. Si se indica sellerId (vista del vendedor), cada pedido muestra solo
 * los discos y el total de ese vendedor, porque un pedido puede incluir discos de varios.
 */
class OrderAdapter(
    private val sellerId: String? = null,
    private val onClick: ((Order) -> Unit)? = null
) : ListAdapter<Order, OrderAdapter.VH>(OrderDiff) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val id: TextView = view.findViewById(R.id.tvOrderId)
        val status: TextView = view.findViewById(R.id.tvOrderStatus)
        val buyer: TextView = view.findViewById(R.id.tvBuyer)
        val products: TextView = view.findViewById(R.id.tvOrderItems)
        val date: TextView = view.findViewById(R.id.tvOrderDate)
        val total: TextView = view.findViewById(R.id.tvOrderTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = getItem(position)
        val context = holder.itemView.context

        // El vendedor solo ve lo suyo; el comprador y el administrador ven el pedido completo
        val shownItems = if (sellerId != null) order.items.filter { it.sellerId == sellerId } else order.items
        val shownTotal = if (sellerId != null) shownItems.sumOf { it.price * it.quantity } else order.total

        holder.id.text = context.getString(R.string.order_number, order.id.takeLast(5).uppercase())
        // Si hay una solicitud de retracto, esa etiqueta tiene prioridad sobre el estado de envío
        if (order.withdrawalStatus == WithdrawalStatus.REQUESTED) holder.status.setText(R.string.status_withdrawal)
        else holder.status.setText(orderStatusRes(order.status))
        holder.buyer.text = order.buyerName
        holder.products.text = shownItems.joinToString("\n") { "${it.title} (${it.format}) ×${it.quantity}" }
        holder.date.text = order.createdAt.toDateText()
        holder.total.text = shownTotal.toCop()
        holder.itemView.setOnClickListener { onClick?.invoke(order) }
    }
}

// Mismo pedido = mismo id; si cambia el contenido (por ejemplo el estado), se redibuja
private object OrderDiff : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(a: Order, b: Order) = a.id == b.id
    override fun areContentsTheSame(a: Order, b: Order) = a == b
}