package com.example.vinylcdvault.ui.seller

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
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.ui.common.PhotoPicker
import com.example.vinylcdvault.ui.common.ProfileViewModel
import com.example.vinylcdvault.ui.common.ProfileViewModelFactory
import com.example.vinylcdvault.ui.common.logoutToLogin
import com.example.vinylcdvault.ui.common.readCompressed
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.ui.common.showAvatar
import com.example.vinylcdvault.ui.common.showInfo
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCompactCop
import kotlinx.coroutines.launch

/** Perfil del vendedor: foto, datos de la cuenta, estadísticas reales y opciones de la tienda. */
class SellerProfileFragment : Fragment(R.layout.fragment_seller_profile) {

    private val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory() }
    private val statsViewModel: SellerProfileViewModel by viewModels { SellerProfileViewModelFactory() }
    private val photoPicker = PhotoPicker(this) { uri -> uploadPhoto(uri) }

    private var currentUser: User? = null   // se usa en el diálogo "Datos de la tienda"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val tvName = view.findViewById<TextView>(R.id.tvSellerName)
        val tvEmail = view.findViewById<TextView>(R.id.tvSellerEmail)

        ivAvatar.setOnClickListener { photoPicker.show() }
        view.findViewById<View>(R.id.tvChangePhoto).setOnClickListener { photoPicker.show() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Datos de la cuenta: el spinner se oculta cuando llegan
                launch {
                    profileViewModel.profile.collect { state ->
                        reportLoading(state)
                        if (state is Resource.Success) {
                            currentUser = state.data
                            tvName.text = state.data.name
                            tvEmail.text = state.data.email
                            ivAvatar.showAvatar(state.data.photoUrl)
                        }
                    }
                }
                // Estadísticas reales
                launch {
                    statsViewModel.stats.collect { state ->
                        if (state is Resource.Success) {
                            view.findViewById<TextView>(R.id.tvStatProducts).text = state.data.products.toString()
                            view.findViewById<TextView>(R.id.tvStatSales).text = state.data.unitsSold.toString()
                            view.findViewById<TextView>(R.id.tvStatRevenue).text = state.data.revenue.toCompactCop()
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

        // Filas informativas; la de la tienda muestra los datos reales del vendedor
        view.findViewById<View>(R.id.rowStore).setOnClickListener {
            val user = currentUser
            showInfo(R.string.info_store_title, getString(R.string.info_store_msg, user?.name.orEmpty(), user?.email.orEmpty()))
        }
        view.findViewById<View>(R.id.rowShipping).setOnClickListener {
            showInfo(R.string.info_shipping_title, getString(R.string.info_shipping_msg))
        }
        view.findViewById<View>(R.id.rowPayments).setOnClickListener {
            showInfo(R.string.info_payments_title, getString(R.string.info_payments_msg))
        }
        view.findViewById<View>(R.id.rowHelp).setOnClickListener {
            showInfo(R.string.info_help_title, getString(R.string.info_help_msg))
        }

        view.findViewById<View>(R.id.btnSellerLogout).setOnClickListener { logoutToLogin() }
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

    private fun toast(message: String) = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}