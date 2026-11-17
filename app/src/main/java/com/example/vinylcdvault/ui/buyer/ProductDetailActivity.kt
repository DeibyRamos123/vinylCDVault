package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.util.toCop

/** Detalle de un disco: recibe el Product por el Intent y muestra sus datos. */
class ProductDetailActivity : AppCompatActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val p = intent.getSerializableExtra("product") as Product
        findViewById<TextView>(R.id.tvDetailTitle).text = p.title
        findViewById<TextView>(R.id.tvDetailArtist).text = p.artist
        findViewById<TextView>(R.id.tvDetailFormat).text = p.format
        findViewById<TextView>(R.id.tvDetailPrice).text = p.price.toCop()   // 189900 → "$189.900"
        findViewById<TextView>(R.id.tvDetailCondition).text = p.condition
        findViewById<TextView>(R.id.tvDetailDescription).text = p.description
        // La portada (ivCover) se carga desde p.imageUrl en la parte B, con Coil
        findViewById<ImageView>(R.id.ivCover).load(p.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_cover)
            error(R.drawable.bg_cover)
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAddToCart).setOnClickListener {
            Toast.makeText(this, R.string.detail_added, Toast.LENGTH_SHORT).show()
        }
    }
}