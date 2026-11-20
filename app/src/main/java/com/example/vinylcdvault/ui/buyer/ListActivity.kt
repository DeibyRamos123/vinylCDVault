package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.model.OrderStatus
import com.example.vinylcdvault.model.openProductDetail
import com.example.vinylcdvault.ui.common.FavoriteViewModel
import com.example.vinylcdvault.ui.common.FavoriteViewModelFactory
import com.example.vinylcdvault.ui.common.OrderAdapter
import com.example.vinylcdvault.ui.common.OrdersViewModel
import com.example.vinylcdvault.ui.common.OrdersViewModelFactory
import com.example.vinylcdvault.ui.common.ProductAdapter
import com.example.vinylcdvault.ui.common.ProductViewModel
import com.example.vinylcdvault.ui.common.ProductViewModelFactory
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.WithdrawalPolicy
import com.example.vinylcdvault.util.toDateText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Pantalla de lista reutilizable del perfil del comprador. Según el modo muestra:
 * sus pedidos, sus favoritos, o sus pedidos para solicitar el retracto.
 */
class ListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_FAVORITES = "favorites"
        const val MODE_WITHDRAWAL = "withdrawal"
    }

    private val ordersVm: OrdersViewModel by viewModels { OrdersViewModelFactory() }
    private val productVm: ProductViewModel by viewModels { ProductViewModelFactory() }
    private val favoriteVm: FavoriteViewModel by viewModels { FavoriteViewModelFactory() }

    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val title = findViewById<TextView>(R.id.tvListTitle)
        val rv = findViewById<RecyclerView>(R.id.rvList)
        tvEmpty = findViewById(R.id.tvListEmpty)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        when (intent.getStringExtra(EXTRA_MODE)) {
            MODE_FAVORITES -> showFavorites(title, rv)
            MODE_WITHDRAWAL -> showOrders(title, rv, withdrawal = true)
            else -> showOrders(title, rv, withdrawal = false)
        }
    }

    // Discos que el comprador marcó con el corazón (se cruzan los ids favoritos con los discos publicados)
    private fun showFavorites(title: TextView, rv: RecyclerView) {
        title.setText(R.string.profile_favorites_title)
        tvEmpty.setText(R.string.favorites_empty)
        rv.layoutManager = GridLayoutManager(this, 2)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(productVm.products, favoriteVm.ids) { state, ids ->
                    if (state is Resource.Success) state.data.filter { it.id in ids } else null
                }.collect { list ->
                    if (list != null) {
                        rv.adapter = ProductAdapter(list, R.layout.item_product) { openProductDetail(it) }
                        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    // Pedidos del comprador; en modo retracto, tocar uno inicia la solicitud
    private fun showOrders(title: TextView, rv: RecyclerView, withdrawal: Boolean) {
        title.setText(if (withdrawal) R.string.withdrawal_list_title else R.string.profile_orders_title)
        tvEmpty.setText(R.string.orders_empty)
        rv.layoutManager = LinearLayoutManager(this)
        val click: ((Order) -> Unit)? = if (withdrawal) ::askWithdrawal else null
        val adapter = OrderAdapter(onClick = click)
        rv.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    ordersVm.myOrders.collect { state ->
                        if (state is Resource.Success) {
                            adapter.submitList(state.data)
                            tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }
                // Resultado de la solicitud de retracto
                launch {
                    ordersVm.opState.collect { state ->
                        when (state) {
                            is Resource.Success -> { toast(getString(state.data)); ordersVm.resetOp() }
                            is Resource.Error -> { toast(state.message); ordersVm.resetOp() }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    // Aplica la regla del retracto: si el pedido califica, pide confirmación; si no, explica por qué
    private fun askWithdrawal(order: Order) {
        when (WithdrawalPolicy.check(order)) {
            WithdrawalPolicy.Check.ALREADY_REQUESTED -> toast(getString(R.string.withdrawal_already))
            WithdrawalPolicy.Check.EXPIRED -> toast(getString(R.string.withdrawal_expired))
            WithdrawalPolicy.Check.ALLOWED -> {
                // Entregado: se informa la fecha límite; sin entregar: se ofrece cancelar
                val message = if (order.status == OrderStatus.DELIVERED) {
                    val limit = WithdrawalPolicy.deadline(WithdrawalPolicy.baseDate(order)).toDateText()
                    getString(R.string.withdrawal_msg_delivered, limit)
                } else getString(R.string.withdrawal_msg_cancel)

                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.withdrawal_title)
                    .setMessage(message)
                    .setPositiveButton(R.string.withdrawal_confirm) { _, _ -> ordersVm.requestWithdrawal(order.id) }
                    .setNegativeButton(R.string.seller_delete_cancel, null)
                    .show()
            }
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}