package com.example.vinylcdvault.ui.buyer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.ui.common.PhotoPicker
import com.example.vinylcdvault.ui.common.ProfileViewModel
import com.example.vinylcdvault.ui.common.ProfileViewModelFactory
import com.example.vinylcdvault.ui.common.logoutToLogin
import com.example.vinylcdvault.ui.common.readCompressed
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.ui.common.showAvatar
import com.example.vinylcdvault.util.Resource
import kotlinx.coroutines.launch

/** Perfil del comprador: foto, datos de la cuenta y accesos a pedidos, direcciones, favoritos y retracto. */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory() }
    private val photoPicker = PhotoPicker(this) { uri -> uploadPhoto(uri) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)

        ivAvatar.setOnClickListener { photoPicker.show() }
        view.findViewById<View>(R.id.tvChangePhoto).setOnClickListener { photoPicker.show() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Datos del usuario: el spinner se oculta cuando llegan
                launch {
                    profileViewModel.profile.collect { state ->
                        reportLoading(state)
                        if (state is Resource.Success) {
                            tvName.text = state.data.name
                            tvEmail.text = state.data.email
                            ivAvatar.showAvatar(state.data.photoUrl)
                        }
                    }
                }
                // Resultado del cambio de foto
                launch {
                    profileViewModel.photoOp.collect { state ->
                        when (state) {
                            is Resource.Success -> { toast(getString(state.data)); profileViewModel.resetPhotoOp() }
                            is Resource.Error -> { toast(state.message); profileViewModel.resetPhotoOp() }
                            else -> Unit
                        }
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

    // Comprime la foto elegida y la manda a subir
    private fun uploadPhoto(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                profileViewModel.updatePhoto(requireContext().readCompressed(uri))
            } catch (e: Exception) {
                toast(getString(R.string.error_image))
            }
        }
    }

    // Abre la pantalla de lista en el modo indicado (null = mis pedidos)
    private fun openList(mode: String?) {
        startActivity(Intent(requireContext(), ListActivity::class.java).putExtra(ListActivity.EXTRA_MODE, mode))
    }

    private fun toast(message: String) = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}