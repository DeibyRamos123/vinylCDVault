package com.example.vinylcdvault.ui.buyer

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
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
import com.example.vinylcdvault.ui.common.fillSingleChoice
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.util.Resource
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Catálogo del comprador: discos publicados con búsqueda en vivo y filtros por género y formato. */
class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private val productVm: ProductViewModel by viewModels { ProductViewModelFactory() }
    private val filterVm: CatalogFilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvCatalog)
        val tvEmpty = view.findViewById<TextView>(R.id.tvCatalogEmpty)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)

        val current = filterVm.filter.value

        // Chips de género: "Todos" más cada género; quedan marcados según el filtro actual
        val genreOptions = buildList<Pair<String?, Int>> {
            add(null to R.string.filter_all)
            Genres.all.forEach { add(it.code to it.labelRes) }
        }
        view.findViewById<ChipGroup>(R.id.cgGenre)
            .fillSingleChoice(genreOptions, current.genre) { filterVm.setGenre(it) }

        // Chips de formato: el código es el mismo texto con que se guarda el disco
        val formatOptions = buildList<Pair<String?, Int>> {
            add(null to R.string.filter_all)
            add(getString(R.string.format_vinyl) to R.string.format_vinyl)
            add(getString(R.string.format_cd) to R.string.format_cd)
            add(getString(R.string.format_cassette) to R.string.format_cassette)
        }
        view.findViewById<ChipGroup>(R.id.cgFormat)
            .fillSingleChoice(formatOptions, current.format) { filterVm.setFormat(it) }

        // Búsqueda en vivo: cada letra actualiza el filtro
        etSearch.setText(current.query)
        etSearch.doAfterTextChanged { filterVm.setQuery(it?.toString().orEmpty()) }

        // Cada vez que llegan discos nuevos o cambia el filtro, se vuelve a calcular la lista
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(productVm.products, filterVm.filter) { state, criteria -> state to criteria }
                    .collect { (state, criteria) ->
                        reportLoading(state)
                        if (state is Resource.Success) {
                            val list = state.data.applyFilter(criteria)
                            rv.adapter = ProductAdapter(list, R.layout.item_product) {
                                requireContext().openProductDetail(it)
                            }
                            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
            }
        }
    }
}