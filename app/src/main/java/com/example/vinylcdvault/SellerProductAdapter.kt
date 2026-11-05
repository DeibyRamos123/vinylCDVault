package com.example.vinylcdvault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SellerProductAdapter(
    private val items: MutableList<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product, Int) -> Unit
) : RecyclerView.Adapter<SellerProductAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val artist: TextView = view.findViewById(R.id.tvArtist)
        val format: TextView = view.findViewById(R.id.tvFormat)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val edit: View = view.findViewById(R.id.btnEdit)
        val delete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_seller_product, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.title.text = p.title
        holder.artist.text = p.artist
        holder.format.text = p.format
        holder.price.text = p.price
        holder.edit.setOnClickListener { onEdit(p) }
        holder.delete.setOnClickListener { onDelete(p, holder.bindingAdapterPosition) }
    }

    override fun getItemCount() = items.size
}