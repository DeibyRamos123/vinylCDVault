package com.example.vinylcdvault.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.User
import android.widget.ImageView

/**
 * Muestra la lista de usuarios. Usa DiffUtil para animar solo lo que cambió
 * cuando llega una lista nueva desde Firestore.
 */
class UserAdapter(
    private val onEdit: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : ListAdapter<User, UserAdapter.VH>(UserDiff) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.ivUserAvatar)
        val name: TextView = view.findViewById(R.id.tvUserName)
        val email: TextView = view.findViewById(R.id.tvUserEmail)
        val role: TextView = view.findViewById(R.id.tvUserRole)
        val status: TextView = view.findViewById(R.id.tvUserStatus)
        val edit: View = view.findViewById(R.id.btnEditUser)
        val delete: View = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = getItem(position)
        val context = holder.itemView.context
        holder.avatar.showAvatar(u.photoUrl)   // foto en círculo, o el ícono si no tiene
        holder.name.text = u.name
        holder.email.text = u.email
        holder.role.setText(roleLabelRes(u.role))
        holder.status.setText(if (u.active) R.string.user_status_active else R.string.user_status_suspended)
        holder.status.setTextColor(
            ContextCompat.getColor(context, if (u.active) R.color.success else R.color.accent)
        )
        holder.edit.setOnClickListener { onEdit(u) }
        holder.delete.setOnClickListener { onDelete(u) }
    }
}

// Compara usuarios por uid para saber si es el mismo elemento, y por contenido para saber si cambió
private object UserDiff : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(a: User, b: User) = a.uid == b.uid
    override fun areContentsTheSame(a: User, b: User) = a == b
}