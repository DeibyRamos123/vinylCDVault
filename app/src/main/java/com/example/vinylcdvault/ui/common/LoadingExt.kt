package com.example.vinylcdvault.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import com.example.vinylcdvault.R
import com.example.vinylcdvault.util.Resource

/**
 * Muestra el spinner mientras el estado es "cargando" y lo oculta cuando llegan los datos o falla.
 * La capa (loadingOverlay) vive en el layout de la Activity que contiene las pestañas.
 */
fun Fragment.reportLoading(state: Resource<*>) {
    activity?.findViewById<View>(R.id.loadingOverlay)?.visibility =
        if (state is Resource.Loading) View.VISIBLE else View.GONE
}