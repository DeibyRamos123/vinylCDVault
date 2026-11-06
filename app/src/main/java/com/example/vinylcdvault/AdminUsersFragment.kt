package com.example.vinylcdvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminUsersFragment : Fragment(R.layout.fragment_admin_users) {

    private val users = SampleUsers.users.toMutableList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvUsers)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = UserAdapter(
            users,
            onEdit = { user ->
                startActivity(
                    Intent(requireContext(), UserFormActivity::class.java)
                        .putExtra("user", user)
                )
            },
            onDelete = { user, position ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.admin_delete_user)
                    .setMessage(R.string.admin_delete_user_message)
                    .setPositiveButton(R.string.seller_delete_confirm) { _, _ ->
                        users.remove(user)
                        rv.adapter?.notifyItemRemoved(position)
                        Toast.makeText(requireContext(), R.string.admin_user_deleted, Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.seller_delete_cancel, null)
                    .show()
            }
        )

        view.findViewById<View>(R.id.fabAddUser).setOnClickListener {
            startActivity(Intent(requireContext(), UserFormActivity::class.java))
        }
    }
}