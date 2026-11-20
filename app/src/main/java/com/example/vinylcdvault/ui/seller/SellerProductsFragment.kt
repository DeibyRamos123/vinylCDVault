package com.example.vinylcdvault.ui.seller

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
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.ui.common.ProductViewModel
import com.example.vinylcdvault.ui.common.ProductViewModelFactory
import com.example.vinylcdvault.ui.common.SellerProductAdapter
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** Inventario del vendedor: lista de sus discos con editar, eliminar y el botón de publicar. */
class SellerProductsFragment : Fragment(R.layout.fragment_seller_products) {

    private val viewModel: ProductViewModel by viewModels { ProductViewModelFactory() }
    private val productAdapter = SellerProductAdapter(
        onEdit = { product -> openForm(product) },
        onDelete = { product -> confirmDelete(product) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<RecyclerView>(R.id.rvSellerProducts).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
        view.findViewById<View>(R.id.fabAdd).setOnClickListener { openForm(null) }

        val myUid = AppContainer.auth.currentUser?.uid
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.products.collect { state ->
                        reportLoading(state)
                        when (state) {
                            // Solo los discos publicados por este vendedor
                            is Resource.Success -> productAdapter.submitList(state.data.filter { it.sellerId == myUid })
                            is Resource.Error -> toast(state.message)
                            Resource.Loading -> Unit
                        }
                    }
                }
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

    // Sin disco = formulario de crear; con disco = formulario de editar
    private fun openForm(product: Product?) {
        val intent = Intent(requireContext(), ProductFormActivity::class.java)
        if (product != null) intent.putExtra("product", product)
        startActivity(intent)
    }

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