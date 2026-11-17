package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.openProductDetail
import com.example.vinylcdvault.ui.common.ProductAdapter
import com.example.vinylcdvault.ui.common.ProductViewModel
import com.example.vinylcdvault.ui.common.ProductViewModelFactory
import com.example.vinylcdvault.util.Resource
import kotlinx.coroutines.launch

/** Inicio del comprador: muestra los 4 discos publicados más recientes como destacados. */
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: ProductViewModel by viewModels { ProductViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvFeatured)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { state ->
                    if (state is Resource.Success) {
                        // La lista ya viene del más reciente al más antiguo
                        rv.adapter = ProductAdapter(state.data.take(4), R.layout.item_product) {
                            requireContext().openProductDetail(it)
                        }
                    }
                }
            }
        }
    }
}