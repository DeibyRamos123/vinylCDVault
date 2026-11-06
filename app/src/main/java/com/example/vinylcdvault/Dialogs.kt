package com.example.vinylcdvault

import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.showInfo(titleRes: Int, messageRes: Int) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(titleRes)
        .setMessage(messageRes)
        .setPositiveButton(R.string.dialog_close, null)
        .show()
}