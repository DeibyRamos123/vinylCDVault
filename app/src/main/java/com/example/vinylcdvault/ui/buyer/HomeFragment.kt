package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.openProductDetail
import com.example.vinylcdvault.ui.common.Genres
import com.example.vinylcdvault.ui.common.ProductAdapter
import com.example.vinylcdvault.ui.common.ProductViewModel
import com.example.vinylcdvault.ui.common.ProductViewModelFactory
import com.example.vinylcdvault.ui.common.fillActionChips
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.util.Resource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

/** Inicio del comprador: géneros que abren el catálogo filtrado y los 4 discos más recientes. */
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: ProductViewModel by viewModels { ProductViewModelFactory() }
    private val filterVm: CatalogFilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvFeatured)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)

        // Tocar un género deja ese filtro y abre el catálogo
        view.findViewById<ChipGroup>(R.id.cgHomeGenres)
            .fillActionChips(Genres.all.map { it.code to it.labelRes }) { code ->
                filterVm.showOnly(code)
                goToCatalog()
            }

        // Tocar el buscador abre el catálogo sin filtros para escribir ahí
        view.findViewById<View>(R.id.tvHomeSearch).setOnClickListener {
            filterVm.showOnly(null)
            goToCatalog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visible.collect { state ->
                    reportLoading(state)
                    if (state is Resource.Success) {
                        rv.adapter = ProductAdapter(state.data.take(4), R.layout.item_product) {
                            requireContext().openProductDetail(it)
                        }
                    }
                }
            }
        }
    }

    // Cambia a la pestaña Catálogo de la barra inferior
    private fun goToCatalog() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_catalog
    }
}