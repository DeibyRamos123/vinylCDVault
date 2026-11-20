package com.example.vinylcdvault.ui.buyer

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.ui.common.FavoriteViewModel
import com.example.vinylcdvault.ui.common.FavoriteViewModelFactory
import com.example.vinylcdvault.ui.common.showAvatar
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCop
import kotlinx.coroutines.launch

/** Detalle de un disco: datos, vendedor, carrito y favoritos. */
class ProductDetailActivity : AppCompatActivity() {

    private val cartViewModel: CartViewModel by viewModels { CartViewModelFactory() }
    private val favoriteViewModel: FavoriteViewModel by viewModels { FavoriteViewModelFactory() }
    private val sellerViewModel: SellerPublicViewModel by viewModels { SellerPublicViewModelFactory() }

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

        val btnFavorite = findViewById<ImageButton>(R.id.btnFavorite)
        val rowSeller = findViewById<View>(R.id.rowSeller)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAddToCart).setOnClickListener { cartViewModel.add(p) }
        btnFavorite.setOnClickListener {
            val adding = p.id !in favoriteViewModel.ids.value
            favoriteViewModel.toggle(p.id)
            Toast.makeText(this, if (adding) R.string.favorite_added else R.string.favorite_removed, Toast.LENGTH_SHORT).show()
        }

        // La fila del vendedor aparece cuando se cargan sus datos; al tocarla abre su perfil público
        if (p.sellerId.isNotBlank()) {
            sellerViewModel.loadSeller(p.sellerId)
            rowSeller.setOnClickListener {
                startActivity(
                    Intent(this, SellerPublicActivity::class.java)
                        .putExtra(SellerPublicActivity.EXTRA_SELLER_ID, p.sellerId)
                )
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // El corazón se pinta según si el disco está en favoritos
                launch { favoriteViewModel.ids.collect { renderFavorite(btnFavorite, p.id in it) } }
                // Nombre y foto del vendedor
                launch {
                    sellerViewModel.seller.collect { state ->
                        if (state is Resource.Success) {
                            rowSeller.visibility = View.VISIBLE
                            findViewById<TextView>(R.id.tvSellerName).text = state.data.name
                            findViewById<ImageView>(R.id.ivSellerAvatar).showAvatar(state.data.photoUrl)
                        }
                    }
                }
                // Aviso de agregado al carrito o de error
                launch {
                    cartViewModel.opState.collect { state ->
                        when (state) {
                            is Resource.Success -> {
                                Toast.makeText(this@ProductDetailActivity, state.data, Toast.LENGTH_SHORT).show()
                                cartViewModel.resetOp()
                            }
                            is Resource.Error -> {
                                Toast.makeText(this@ProductDetailActivity, state.message, Toast.LENGTH_LONG).show()
                                cartViewModel.resetOp()
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    // Corazón relleno y rojo si es favorito; de línea y azul marino si no
    private fun renderFavorite(button: ImageButton, isFavorite: Boolean) {
        button.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
        button.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, if (isFavorite) R.color.accent else R.color.primary)
        )
    }
}