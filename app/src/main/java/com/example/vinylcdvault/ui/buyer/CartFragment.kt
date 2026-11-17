package com.example.vinylcdvault.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.ui.common.CartAdapter
import com.example.vinylcdvault.util.Pricing
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCop
import kotlinx.coroutines.launch

/** Carrito del comprador: lista de discos, resumen de costos y paso al pago. */
class CartFragment : Fragment(R.layout.fragment_cart) {

    private val viewModel: CartViewModel by viewModels { CartViewModelFactory() }
    private val cartAdapter = CartAdapter(
        onPlus = { viewModel.increase(it) },
        onMinus = { viewModel.decrease(it) },
        onRemove = { viewModel.remove(it) }
    )
    private var currentItems: List<CartItem> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<RecyclerView>(R.id.rvCart).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
        val tvEmpty = view.findViewById<View>(R.id.tvCartEmpty)

        view.findViewById<View>(R.id.btnCheckout).setOnClickListener {
            if (currentItems.isEmpty()) {
                Toast.makeText(requireContext(), R.string.cart_empty, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(requireContext(), CheckoutActivity::class.java))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Lista y totales en tiempo real
                launch {
                    viewModel.items.collect { state ->
                        if (state is Resource.Success) {
                            currentItems = state.data
                            cartAdapter.submitList(state.data)
                            tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                            showTotals(view, state.data)
                        }
                    }
                }
                // Avisos de las operaciones (quitar un disco, errores)
                launch {
                    viewModel.opState.collect { state ->
                        when (state) {
                            is Resource.Success -> { toast(getString(state.data)); viewModel.resetOp() }
                            is Resource.Error -> { toast(state.message); viewModel.resetOp() }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    // Calcula y muestra subtotal, envío, IVA y total
    private fun showTotals(root: View, items: List<CartItem>) {
        val subtotal = Pricing.subtotal(items)
        root.findViewById<TextView>(R.id.tvSubtotal).text = subtotal.toCop()
        root.findViewById<TextView>(R.id.tvShipping).text = Pricing.shipping(items).toCop()
        root.findViewById<TextView>(R.id.tvTax).text = Pricing.tax(subtotal).toCop()
        root.findViewById<TextView>(R.id.tvTotal).text = Pricing.total(items).toCop()
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}