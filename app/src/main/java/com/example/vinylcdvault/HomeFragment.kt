package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

// hereda de la clase de fragment que es similar a un componente en react
class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvFeatured)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = ProductAdapter(SampleData.products.take(4), R.layout.item_product) {
            requireContext().openProductDetail(it)
        }
    }
}