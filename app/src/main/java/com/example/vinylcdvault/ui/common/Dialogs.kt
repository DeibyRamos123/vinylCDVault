package com.example.vinylcdvault.ui.common

import androidx.fragment.app.Fragment
import com.example.vinylcdvault.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.showInfo(titleRes: Int, messageRes: Int) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(titleRes)
        .setMessage(messageRes)
        .setPositiveButton(R.string.dialog_close, null)
        .show()
}