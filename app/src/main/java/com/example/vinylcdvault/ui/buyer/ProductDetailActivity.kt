package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCop
import kotlinx.coroutines.launch

/** Detalle de un disco: muestra sus datos y permite agregarlo al carrito. */
class ProductDetailActivity : AppCompatActivity() {

    private val viewModel: CartViewModel by viewModels { CartViewModelFactory() }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val p = intent.getSerializableExtra("product") as Product
        findViewById<TextView>(R.id.tvDetailTitle).text = p.title
        findViewById<TextView>(R.id.tvDetailArtist).text = p.artist
        findViewById<TextView>(R.id.tvDetailFormat).text = p.format
        findViewById<TextView>(R.id.tvDetailPrice).text = p.price.toCop()
        findViewById<TextView>(R.id.tvDetailCondition).text = p.condition
        findViewById<TextView>(R.id.tvDetailDescription).text = p.description
        findViewById<ImageView>(R.id.ivCover).load(p.imageUrl.ifBlank { null }) {
            crossfade(true)
            placeholder(R.drawable.bg_cover)
            error(R.drawable.bg_cover)
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAddToCart).setOnClickListener { viewModel.add(p) }

        // Avisa si se agregó al carrito o si hubo un error (por ejemplo, sin sesión)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.opState.collect { state ->
                    when (state) {
                        is Resource.Success -> {
                            Toast.makeText(this@ProductDetailActivity, state.data, Toast.LENGTH_SHORT).show()
                            viewModel.resetOp()
                        }
                        is Resource.Error -> {
                            Toast.makeText(this@ProductDetailActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetOp()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}