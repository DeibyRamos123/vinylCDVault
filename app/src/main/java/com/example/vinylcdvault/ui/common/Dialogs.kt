package com.example.vinylcdvault.ui.common

import androidx.fragment.app.Fragment
import com.example.vinylcdvault.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Diálogo informativo cuyo mensaje se arma en tiempo de ejecución, por ejemplo con datos del usuario. */
fun Fragment.showInfo(titleRes: Int, message: String) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(titleRes)
        .setMessage(message)
        .setPositiveButton(R.string.dialog_close, null)
        .show()
}