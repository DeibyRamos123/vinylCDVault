package com.example.vinylcdvault.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Address

/** Lista de direcciones: tocar una fila la edita; la predeterminada muestra una etiqueta. */
class AddressAdapter(
    private val onEdit: (Address) -> Unit,
    private val onMakePrimary: (Address) -> Unit,
    private val onDelete: (Address) -> Unit
) : ListAdapter<Address, AddressAdapter.VH>(AddressDiff) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.tvAddressLabel)
        val text: TextView = view.findViewById(R.id.tvAddressText)
        val primaryTag: TextView = view.findViewById(R.id.tvAddressPrimary)
        val makePrimary: TextView = view.findViewById(R.id.btnMakePrimary)
        val delete: View = view.findViewById(R.id.btnDeleteAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_adress, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = getItem(position)
        holder.label.text = a.label
        holder.text.text = a.address
        // La etiqueta y el botón son excluyentes: o ya es la predeterminada, o se puede elegir
        holder.primaryTag.visibility = if (a.primary) View.VISIBLE else View.GONE
        holder.makePrimary.visibility = if (a.primary) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener { onEdit(a) }
        holder.makePrimary.setOnClickListener { onMakePrimary(a) }
        holder.delete.setOnClickListener { onDelete(a) }
    }
}

// Mismo id = misma dirección; si cambia el contenido, se redibuja la fila
private object AddressDiff : DiffUtil.ItemCallback<Address>() {
    override fun areItemsTheSame(a: Address, b: Address) = a.id == b.id
    override fun areContentsTheSame(a: Address, b: Address) = a == b
}