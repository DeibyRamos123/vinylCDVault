package com.example.vinylcdvault.ui.common

import android.view.LayoutInflater
import android.view.View
import com.example.vinylcdvault.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Llena el grupo con chips de selección única. Cada opción es (código, texto);
 * un código null significa "Todos". onSelect recibe el código del chip tocado.
 */
fun ChipGroup.fillSingleChoice(
    options: List<Pair<String?, Int>>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    removeAllViews()
    options.forEach { (code, labelRes) ->
        val chip = LayoutInflater.from(context).inflate(R.layout.item_filter_chip, this, false) as Chip
        chip.id = View.generateViewId()
        chip.setText(labelRes)
        addView(chip)
        chip.isChecked = code == selected
        chip.setOnClickListener { onSelect(code) }
    }
}

/** Llena el grupo con chips que funcionan como botones (no quedan marcados). */
fun ChipGroup.fillActionChips(options: List<Pair<String, Int>>, onClick: (String) -> Unit) {
    removeAllViews()
    options.forEach { (code, labelRes) ->
        val chip = LayoutInflater.from(context).inflate(R.layout.item_filter_chip, this, false) as Chip
        chip.setText(labelRes)
        chip.isCheckable = false
        chip.setOnClickListener { onClick(code) }
        addView(chip)
    }
}