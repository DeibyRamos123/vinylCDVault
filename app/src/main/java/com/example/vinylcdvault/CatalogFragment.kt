package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CatalogFragment : Fragment(R.layout.fragment_catalog) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvCatalog)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = ProductAdapter(SampleData.products, R.layout.item_product) {
            requireContext().openProductDetail(it)
        }
    }
}