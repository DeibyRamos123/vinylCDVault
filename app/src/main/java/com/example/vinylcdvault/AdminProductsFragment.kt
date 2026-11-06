package com.example.vinylcdvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminProductsFragment : Fragment(R.layout.fragment_admin_products) {

    private val products = SampleData.products.toMutableList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvAdminProducts)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = SellerProductAdapter(
            products,
            onEdit = { product ->
                startActivity(
                    Intent(requireContext(), ProductFormActivity::class.java)
                        .putExtra("product", product)
                )
            },
            onDelete = { product, position ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.seller_delete_title)
                    .setMessage(R.string.seller_delete_message)
                    .setPositiveButton(R.string.seller_delete_confirm) { _, _ ->
                        products.remove(product)
                        rv.adapter?.notifyItemRemoved(position)
                        Toast.makeText(requireContext(), R.string.seller_deleted, Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.seller_delete_cancel, null)
                    .show()
            }
        )
    }
}