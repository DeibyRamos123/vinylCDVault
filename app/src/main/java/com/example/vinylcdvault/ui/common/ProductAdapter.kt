package com.example.vinylcdvault.ui.common

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.util.toCop

/**
 * Adaptador de tarjetas de disco. Sirve para el catálogo, el inicio, los favoritos y el carrito:
 * solo cambia el layout de cada fila (layoutRes).
 */
class ProductAdapter(
    private val items: List<Product>,
    private val layoutRes: Int,
    private val onClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        // La portada es opcional: no todos los layouts de fila la tienen
        val cover: ImageView? = view.findViewById(R.id.ivCover)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val artist: TextView = view.findViewById(R.id.tvArtist)
        val format: TextView = view.findViewById(R.id.tvFormat)
        val price: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.title.text = p.title
        holder.artist.text = p.artist
        holder.format.text = p.format
        holder.price.text = p.price.toCop()   // 189900 → "$189.900"

        // Portada: Coil la descarga y la guarda en caché; sin foto, queda el fondo gris
        holder.cover?.let { cover ->
            if (p.imageUrl.isNotBlank()) {
                cover.load(p.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.bg_cover)
                    error(R.drawable.bg_cover)
                    // Si falla la descarga, deja el motivo en el Logcat (filtra por "Coil")
                    listener(onError = { _, result -> Log.e("Coil", "No cargó ${p.imageUrl}: ${result.throwable.message}") })
                }
            } else {
                cover.setImageResource(R.drawable.bg_cover)
            }
        }
        holder.itemView.setOnClickListener { onClick?.invoke(p) }
    }

    override fun getItemCount() = items.size
}