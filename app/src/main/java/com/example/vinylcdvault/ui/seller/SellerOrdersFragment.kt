package com.example.vinylcdvault.ui.seller

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.ui.common.OrderAdapter
import com.example.vinylcdvault.R
import com.example.vinylcdvault.model.SampleOrders

class SellerOrdersFragment : Fragment(R.layout.fragment_seller_orders) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvOrders)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = OrderAdapter(SampleOrders.orders)
    }
}