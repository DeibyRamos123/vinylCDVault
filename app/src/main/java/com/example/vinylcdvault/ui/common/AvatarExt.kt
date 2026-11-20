package com.example.vinylcdvault.ui.common

import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.example.vinylcdvault.R

/** Muestra la foto de perfil recortada en círculo; sin foto, un ícono blanco sobre el círculo de color. */
fun ImageView.showAvatar(url: String) {
    if (url.isBlank()) {
        // El margen interno es un cuarto del tamaño, así sirve para avatares grandes y pequeños
        val pad = (layoutParams?.width?.takeIf { it > 0 } ?: 0) / 4
        setPadding(pad, pad, pad, pad)
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
        setImageResource(R.drawable.ic_person)
    } else {
        setPadding(0, 0, 0, 0)
        imageTintList = null
        load(url) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }
}