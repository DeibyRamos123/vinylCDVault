package com.example.vinylcdvault.ui.seller

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
import com.example.vinylcdvault.ui.common.showInfo
import kotlinx.coroutines.launch

/** Perfil del vendedor: muestra el nombre y el correo con que se registró y sus opciones. */
class SellerProfileFragment : Fragment(R.layout.fragment_seller_profile) {

    private val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvName = view.findViewById<TextView>(R.id.tvSellerName)
        val tvEmail = view.findViewById<TextView>(R.id.tvSellerEmail)

        // Cuando el perfil llega de Firestore, se escribe en pantalla
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

        // Filas informativas
        view.findViewById<View>(R.id.rowStore).setOnClickListener { showInfo(R.string.info_store_title, R.string.info_store_msg) }
        view.findViewById<View>(R.id.rowShipping).setOnClickListener { showInfo(R.string.info_shipping_title, R.string.info_shipping_msg) }
        view.findViewById<View>(R.id.rowPayments).setOnClickListener { showInfo(R.string.info_payments_title, R.string.info_payments_msg) }
        view.findViewById<View>(R.id.rowHelp).setOnClickListener { showInfo(R.string.info_help_title, R.string.info_help_msg) }

        view.findViewById<View>(R.id.btnSellerLogout).setOnClickListener { logoutToLogin() }
    }
}