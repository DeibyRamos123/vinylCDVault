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
import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.util.toCop

/** Lista del carrito, con botones para cambiar la cantidad y quitar el disco. */
class CartAdapter(
    private val onPlus: (CartItem) -> Unit,
    private val onMinus: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.VH>(CartDiff) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageView = view.findViewById(R.id.ivCover)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val artist: TextView = view.findViewById(R.id.tvArtist)
        val format: TextView = view.findViewById(R.id.tvFormat)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val qty: TextView = view.findViewById(R.id.tvQty)
        val plus: View = view.findViewById(R.id.btnPlus)
        val minus: View = view.findViewById(R.id.btnMinus)
        val remove: View = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.title.text = item.title
        holder.artist.text = item.artist
        holder.format.text = item.format
        holder.price.text = item.price.toCop()
        holder.qty.text = item.quantity.toString()

        // Portada con Coil; sin foto queda el fondo gris
        if (item.imageUrl.isNotBlank()) {
            holder.cover.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_cover)
                error(R.drawable.bg_cover)
            }
        } else {
            holder.cover.setImageResource(R.drawable.bg_cover)
        }

        holder.plus.setOnClickListener { onPlus(item) }
        holder.minus.setOnClickListener { onMinus(item) }
        holder.remove.setOnClickListener { onRemove(item) }
    }
}

// Mismo disco = mismo productId; si cambia la cantidad, se redibuja solo esa fila
private object CartDiff : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(a: CartItem, b: CartItem) = a.productId == b.productId
    override fun areContentsTheSame(a: CartItem, b: CartItem) = a == b
}