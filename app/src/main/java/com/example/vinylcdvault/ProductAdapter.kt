package com.example.vinylcdvault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val items: List<Product>,
    private val layoutRes: Int,
    private val onClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageView = view.findViewById(R.id.ivCover)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val artist: TextView = view.findViewById(R.id.tvArtist)
        val format: TextView = view.findViewById(R.id.tvFormat)
        val price: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.cover.setImageResource(p.imageRes)
        holder.title.text = p.title
        holder.artist.text = p.artist
        holder.format.text = p.format
        holder.price.text = p.price
        holder.itemView.setOnClickListener { onClick?.invoke(p) }
    }

    override fun getItemCount() = items.size
}