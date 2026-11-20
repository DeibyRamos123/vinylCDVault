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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.openProductDetail
import com.example.vinylcdvault.ui.common.ProductAdapter
import com.example.vinylcdvault.ui.common.showAvatar
import com.example.vinylcdvault.util.Resource
import kotlinx.coroutines.launch

/** Perfil público de un vendedor: su foto, su nombre y todos los discos que tiene publicados. */
class SellerPublicActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SELLER_ID = "seller_id"
    }

    private val viewModel: SellerPublicViewModel by viewModels { SellerPublicViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_public)

        val sellerId = intent.getStringExtra(EXTRA_SELLER_ID).orEmpty()
        if (sellerId.isBlank()) {
            finish()
            return
        }

        val ivAvatar = findViewById<ImageView>(R.id.ivAvatar)
        val tvName = findViewById<TextView>(R.id.tvSellerName)
        val tvCount = findViewById<TextView>(R.id.tvSellerCount)
        val tvEmpty = findViewById<View>(R.id.tvSellerEmpty)
        val loading = findViewById<View>(R.id.loadingOverlay)
        val rv = findViewById<RecyclerView>(R.id.rvSellerProducts)
        rv.layoutManager = GridLayoutManager(this, 2)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        viewModel.loadSeller(sellerId)
        val sellerProducts = viewModel.productsOf(sellerId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Perfil del vendedor: mientras llega, se muestra el spinner
                launch {
                    viewModel.seller.collect { state ->
                        loading.visibility = if (state is Resource.Loading) View.VISIBLE else View.GONE
                        when (state) {
                            is Resource.Success -> {
                                tvName.text = state.data.name
                                ivAvatar.showAvatar(state.data.photoUrl)
                            }
                            is Resource.Error -> {
                                Toast.makeText(this@SellerPublicActivity, state.message, Toast.LENGTH_LONG).show()
                                finish()
                            }
                            Resource.Loading -> Unit
                        }
                    }
                }
                // Discos de ese vendedor
                launch {
                    sellerProducts.collect { state ->
                        if (state is Resource.Success) {
                            tvCount.text = getString(R.string.seller_public_count, state.data.size)
                            tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                            rv.adapter = ProductAdapter(state.data, R.layout.item_product) { openProductDetail(it) }
                        }
                    }
                }
            }
        }
    }
}