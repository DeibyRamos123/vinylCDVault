package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProductDetailActivity : AppCompatActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val p = intent.getSerializableExtra("product") as Product
        findViewById<TextView>(R.id.tvDetailTitle).text = p.title
        findViewById<TextView>(R.id.tvDetailArtist).text = p.artist
        findViewById<TextView>(R.id.tvDetailFormat).text = p.format
        findViewById<TextView>(R.id.tvDetailPrice).text = p.price
        findViewById<TextView>(R.id.tvDetailCondition).text = p.condition
        findViewById<TextView>(R.id.tvDetailDescription).text = p.description
        findViewById<ImageView>(R.id.ivCover).setImageResource(p.imageRes)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAddToCart).setOnClickListener {
            Toast.makeText(this, R.string.detail_added, Toast.LENGTH_SHORT).show()
        }
    }
}