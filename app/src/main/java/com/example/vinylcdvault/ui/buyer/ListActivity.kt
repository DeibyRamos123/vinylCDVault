package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.SampleData
import com.example.vinylcdvault.model.openProductDetail
import com.example.vinylcdvault.ui.common.OrderAdapter
import com.example.vinylcdvault.ui.common.OrdersViewModel
import com.example.vinylcdvault.ui.common.OrdersViewModelFactory
import com.example.vinylcdvault.ui.common.ProductAdapter
import com.example.vinylcdvault.util.Resource
import kotlinx.coroutines.launch

/** Pantalla de lista reutilizable: "Mis pedidos" (datos reales) o "Favoritos" (modo "favorites"). */
class ListActivity : AppCompatActivity() {

    private val ordersViewModel: OrdersViewModel by viewModels { OrdersViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val title = findViewById<TextView>(R.id.tvListTitle)
        val rv = findViewById<RecyclerView>(R.id.rvList)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        if (intent.getStringExtra("mode") == "favorites") {
            title.setText(R.string.profile_favorites_title)
            rv.layoutManager = GridLayoutManager(this, 2)
            rv.adapter = ProductAdapter(SampleData.products.take(3), R.layout.item_product) {
                openProductDetail(it)
            }
        } else {
            title.setText(R.string.profile_orders_title)
            rv.layoutManager = LinearLayoutManager(this)
            val adapter = OrderAdapter()
            rv.adapter = adapter

            // Pedidos del comprador en tiempo real
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    ordersViewModel.myOrders.collect { state ->
                        if (state is Resource.Success) adapter.submitList(state.data)
                    }
                }
            }
        }
    }
}