package com.example.vinylcdvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartFragment : Fragment(R.layout.fragment_cart) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvCart)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = ProductAdapter(SampleData.cart, R.layout.item_cart)

        view.findViewById<View>(R.id.btnCheckout).setOnClickListener {
            startActivity(Intent(requireContext(), CheckoutActivity::class.java))
        }
    }
}