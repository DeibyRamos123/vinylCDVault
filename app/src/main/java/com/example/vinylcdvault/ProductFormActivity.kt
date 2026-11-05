package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProductFormActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etArtist = findViewById<TextInputEditText>(R.id.etArtist)
        val etPrice = findViewById<TextInputEditText>(R.id.etPrice)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val tilTitle = findViewById<TextInputLayout>(R.id.tilTitle)
        val tilArtist = findViewById<TextInputLayout>(R.id.tilArtist)
        val tilPrice = findViewById<TextInputLayout>(R.id.tilPrice)

        // Modo editar: si llega un producto, se rellenan los campos
        val product = intent.getSerializableExtra("product") as? Product
        if (product != null) {
            findViewById<TextView>(R.id.tvFormTitle).setText(R.string.form_edit_title)
            etTitle.setText(product.title)
            etArtist.setText(product.artist)
            etPrice.setText(product.price.filter { it.isDigit() })
            etDescription.setText(product.description)
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        // La cámara y la galería reales no se piden en esta entrega
        findViewById<View>(R.id.btnGallery).setOnClickListener {
            Toast.makeText(this, R.string.form_pending, Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnCamera).setOnClickListener {
            Toast.makeText(this, R.string.form_pending, Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnSave).setOnClickListener {
            val required = "Campo obligatorio"
            tilTitle.error = if (etTitle.text.isNullOrBlank()) required else null
            tilArtist.error = if (etArtist.text.isNullOrBlank()) required else null
            tilPrice.error = if (etPrice.text.isNullOrBlank()) required else null

            if (tilTitle.error == null && tilArtist.error == null && tilPrice.error == null) {
                Toast.makeText(this, R.string.form_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}