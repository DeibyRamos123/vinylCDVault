package com.example.vinylcdvault.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.ui.common.ProductViewModel
import com.example.vinylcdvault.ui.common.ProductViewModelFactory
import com.example.vinylcdvault.ui.common.SellerProductAdapter
import com.example.vinylcdvault.ui.seller.ProductFormActivity
import com.example.vinylcdvault.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** Gestión de productos del administrador: ve todos los discos de la tienda y puede editarlos o eliminarlos. */
class AdminProductsFragment : Fragment(R.layout.fragment_admin_products) {

    private val viewModel: ProductViewModel by viewModels { ProductViewModelFactory() }
    private val productAdapter = SellerProductAdapter(
        onEdit = { product -> openForm(product) },
        onDelete = { product -> confirmDelete(product) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<RecyclerView>(R.id.rvAdminProducts).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Lista en tiempo real con todos los discos, sin filtrar por vendedor
                launch {
                    viewModel.products.collect { state ->
                        when (state) {
                            is Resource.Success -> productAdapter.submitList(state.data)
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

    // Abre el formulario de edición con el disco seleccionado
    private fun openForm(product: Product) {
        startActivity(
            Intent(requireContext(), ProductFormActivity::class.java)
                .putExtra("product", product)
        )
    }

    // Pide confirmación antes de eliminar el disco
    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.seller_delete_title)
            .setMessage(R.string.seller_delete_message)
            .setPositiveButton(R.string.seller_delete_confirm) { _, _ -> viewModel.delete(product.id) }
            .setNegativeButton(R.string.seller_delete_cancel, null)
            .show()
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}