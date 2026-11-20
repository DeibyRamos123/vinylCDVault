package com.example.vinylcdvault.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.ui.common.ProfileViewModel
import com.example.vinylcdvault.ui.common.ProfileViewModelFactory
import com.example.vinylcdvault.ui.common.logoutToLogin
import kotlinx.coroutines.launch

/** Perfil del comprador: datos de la cuenta y accesos a pedidos, direcciones, favoritos y retracto. */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)

        // Nombre y correo reales, guardados en Firestore al registrarse
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.profile.collect { user ->
                    if (user != null) {
                        tvName.text = user.name
                        tvEmail.text = user.email
                    }
                }
            }
        }

        view.findViewById<View>(R.id.rowOrders).setOnClickListener { openList(null) }
        view.findViewById<View>(R.id.rowFavorites).setOnClickListener { openList(ListActivity.MODE_FAVORITES) }
        view.findViewById<View>(R.id.rowWithdrawal).setOnClickListener { openList(ListActivity.MODE_WITHDRAWAL) }
        view.findViewById<View>(R.id.rowAddresses).setOnClickListener {
            startActivity(Intent(requireContext(), AddressesActivity::class.java))
        }
        view.findViewById<View>(R.id.btnLogout).setOnClickListener { logoutToLogin() }
    }

    // Abre la pantalla de lista en el modo indicado (null = mis pedidos)
    private fun openList(mode: String?) {
        startActivity(Intent(requireContext(), ListActivity::class.java).putExtra(ListActivity.EXTRA_MODE, mode))
    }
}