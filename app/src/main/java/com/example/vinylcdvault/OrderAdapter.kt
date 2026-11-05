package com.example.vinylcdvault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(private val items: List<Order>) : RecyclerView.Adapter<OrderAdapter.VH>() {

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
        val o = items[position]
        holder.id.text = o.id
        holder.status.text = o.status
        holder.buyer.text = o.buyer
        holder.products.text = o.items
        holder.date.text = o.date
        holder.total.text = o.total
    }

    override fun getItemCount() = items.size
}