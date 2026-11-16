package com.example.vinylcdvault.ui.common

import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Roles

/** Devuelve el texto (recurso de strings) que se muestra al usuario para un código de rol. */
fun roleLabelRes(role: String): Int = when (role) {
    Roles.ADMIN -> R.string.role_admin
    Roles.SELLER -> R.string.role_seller
    else -> R.string.role_buyer
}