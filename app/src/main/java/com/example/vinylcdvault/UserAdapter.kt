package com.example.vinylcdvault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val items: MutableList<User>,
    private val onEdit: (User) -> Unit,
    private val onDelete: (User, Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
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
        val u = items[position]
        val context = holder.itemView.context
        holder.name.text = u.name
        holder.email.text = u.email
        holder.role.text = u.role
        holder.status.setText(if (u.active) R.string.user_status_active else R.string.user_status_suspended)
        holder.status.setTextColor(
            ContextCompat.getColor(context, if (u.active) R.color.success else R.color.accent)
        )
        holder.edit.setOnClickListener { onEdit(u) }
        holder.delete.setOnClickListener { onDelete(u, holder.bindingAdapterPosition) }
    }

    override fun getItemCount() = items.size
}