package com.example.vinylcdvault.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.ui.common.UserAdapter
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.ui.common.roleLabelRes
import com.example.vinylcdvault.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** Listado de usuarios del administrador con búsqueda, edición y eliminación. */
class AdminUsersFragment : Fragment(R.layout.fragment_admin_users) {

    private val viewModel: UserViewModel by viewModels { UserViewModelFactory() }
    private val userAdapter = UserAdapter(
        onEdit = { user -> openForm(user) },
        onDelete = { user -> confirmDelete(user) }
    )

    private var allUsers: List<User> = emptyList()
    private lateinit var etSearch: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etSearch = view.findViewById(R.id.etUserSearch)

        view.findViewById<RecyclerView>(R.id.rvUsers).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
        // Cada vez que cambia el texto, se vuelve a filtrar la lista
        etSearch.doAfterTextChanged { applyFilter() }
        view.findViewById<View>(R.id.fabAddUser).setOnClickListener { openForm(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Lista en tiempo real
                launch {
                    viewModel.users.collect { state ->
                        reportLoading(state)
                        when (state) {
                            is Resource.Success -> { allUsers = state.data; applyFilter() }
                            is Resource.Error -> toast(state.message)
                            Resource.Loading -> Unit
                        }
                    }
                }
                // Resultado de eliminar
                launch {
                    viewModel.opState.collect { state ->
                        when (state) {
                            is Resource.Success -> { toast(getString(state.data)); viewModel.resetOp() }
                            is Resource.Error -> { toast(state.message); viewModel.resetOp() }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    // Abre el formulario: sin usuario = crear, con usuario = editar
    private fun openForm(user: User?) {
        val intent = Intent(requireContext(), UserFormActivity::class.java)
        if (user != null) intent.putExtra("user", user)
        startActivity(intent)
    }

    // Pide confirmación antes de eliminar y evita que el admin se elimine a sí mismo
    private fun confirmDelete(user: User) {
        if (user.uid == AppContainer.auth.currentUser?.uid) {
            toast(getString(R.string.error_own_account))
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.admin_delete_user)
            .setMessage(R.string.admin_delete_user_message)
            .setPositiveButton(R.string.seller_delete_confirm) { _, _ -> viewModel.deleteUser(user.uid) }
            .setNegativeButton(R.string.seller_delete_cancel, null)
            .show()
    }

    // Filtra por nombre, correo o rol según lo escrito en el buscador
    private fun applyFilter() {
        val query = etSearch.text.toString().trim().lowercase()
        val filtered = if (query.isEmpty()) allUsers else allUsers.filter {
            it.name.lowercase().contains(query) ||
                    it.email.lowercase().contains(query) ||
                    getString(roleLabelRes(it.role)).lowercase().contains(query)
        }
        userAdapter.submitList(filtered)
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}