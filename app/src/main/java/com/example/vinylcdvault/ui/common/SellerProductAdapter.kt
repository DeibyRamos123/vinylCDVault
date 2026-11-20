package com.example.vinylcdvault.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.util.toCop

/** Lista de discos con portada y botones de editar y eliminar (vendedor y administrador). */
class SellerProductAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit,
    private val onOpen: ((Product) -> Unit)? = null
) : ListAdapter<Product, SellerProductAdapter.VH>(ProductDiff) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageView = view.findViewById(R.id.ivCover)
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
        val p = getItem(position)
        holder.title.text = p.title
        holder.artist.text = p.artist
        // Los discos ocultos por moderación se marcan para que el administrador los distinga
        holder.format.text = if (p.hidden)
            "${p.format} · ${holder.itemView.context.getString(R.string.product_hidden_tag)}"
        else p.format

        holder.price.text = p.price.toCop()

        // Portada con Coil; si el disco no tiene foto, se queda el fondo gris
        if (p.imageUrl.isNotBlank()) {
            holder.cover.load(p.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_cover)
                error(R.drawable.bg_cover)
            }
        } else {
            holder.cover.setImageResource(R.drawable.bg_cover)
        }

        holder.edit.setOnClickListener { onEdit(p) }
        holder.delete.setOnClickListener { onDelete(p) }
        
        // Solo el administrador abre el detalle tocando la fila; en la lista del vendedor no hace nada
        onOpen?.let { open -> holder.itemView.setOnClickListener { open(p) } }
    }
}

// Compara por id para saber si es el mismo disco, y por contenido para saber si cambió
object ProductDiff : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
    override fun areContentsTheSame(a: Product, b: Product) = a == b
}