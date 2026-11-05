package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SellerOrdersFragment : Fragment(R.layout.fragment_seller_orders) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvOrders)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = OrderAdapter(SampleOrders.orders)
    }
}