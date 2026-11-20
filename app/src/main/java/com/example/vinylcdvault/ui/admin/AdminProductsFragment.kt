package com.example.vinylcdvault.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.ui.buyer.CatalogFilter
import com.example.vinylcdvault.ui.buyer.applyFilter
import com.example.vinylcdvault.ui.common.ProductViewModel
import com.example.vinylcdvault.ui.common.ProductViewModelFactory
import com.example.vinylcdvault.ui.common.SellerProductAdapter
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.ui.seller.ProductFormActivity
import com.example.vinylcdvault.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** Gestión de productos del administrador: busca, abre el detalle, edita o elimina cualquier disco. */
class AdminProductsFragment : Fragment(R.layout.fragment_admin_products) {

    private val viewModel: ProductViewModel by viewModels { ProductViewModelFactory() }
    private val productAdapter = SellerProductAdapter(
        onEdit = { openForm(it) },
        onDelete = { confirmDelete(it) },
        onOpen = { openDetail(it) }
    )

    private var allProducts: List<Product> = emptyList()
    private lateinit var etSearch: EditText
    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etSearch = view.findViewById(R.id.etProductSearch)
        tvCount = view.findViewById(R.id.tvProductsCount)
        tvEmpty = view.findViewById(R.id.tvAdminProductsEmpty)

        view.findViewById<RecyclerView>(R.id.rvAdminProducts).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
        // Cada vez que cambia el texto, se vuelve a filtrar la lista
        etSearch.doAfterTextChanged { refreshList() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Lista en tiempo real con todos los discos, incluidos los ocultos
                launch {
                    viewModel.products.collect { state ->
                        reportLoading(state)
                        when (state) {
                            is Resource.Success -> { allProducts = state.data; refreshList() }
                            is Resource.Error -> toast(state.message)
                            Resource.Loading -> Unit
                        }
                    }
                }
                // Resultado de eliminar
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

    // Aplica la búsqueda (título o artista, sin tildes) y actualiza el contador y el aviso de "sin resultados"
    private fun refreshList() {
        val filtered = allProducts.applyFilter(CatalogFilter(query = etSearch.text.toString()))
        productAdapter.submitList(filtered)
        tvCount.text = getString(R.string.admin_products_showing, filtered.size, allProducts.size)
        tvEmpty.visibility = if (filtered.isEmpty() && allProducts.isNotEmpty()) View.VISIBLE else View.GONE
    }

    // Abre el detalle de moderación del disco
    private fun openDetail(product: Product) {
        startActivity(
            Intent(requireContext(), AdminProductDetailActivity::class.java).putExtra("product", product)
        )
    }

    // Abre el formulario de edición con el disco seleccionado
    private fun openForm(product: Product) {
        startActivity(
            Intent(requireContext(), ProductFormActivity::class.java).putExtra("product", product)
        )
    }

    // Pide confirmación antes de eliminar el disco
    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.seller_delete_title)
            .setMessage(R.string.moderation_delete_msg)
            .setPositiveButton(R.string.seller_delete_confirm) { _, _ -> viewModel.delete(product.id) }
            .setNegativeButton(R.string.seller_delete_cancel, null)
            .show()
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}