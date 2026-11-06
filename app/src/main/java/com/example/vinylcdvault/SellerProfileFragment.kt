package com.example.vinylcdvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class SellerProfileFragment : Fragment(R.layout.fragment_seller_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.rowStore).setOnClickListener { showInfo(R.string.info_store_title, R.string.info_store_msg) }
        view.findViewById<View>(R.id.rowShipping).setOnClickListener { showInfo(R.string.info_shipping_title, R.string.info_shipping_msg) }
        view.findViewById<View>(R.id.rowPayments).setOnClickListener { showInfo(R.string.info_payments_title, R.string.info_payments_msg) }
        view.findViewById<View>(R.id.rowHelp).setOnClickListener { showInfo(R.string.info_help_title, R.string.info_help_msg) }
        view.findViewById<View>(R.id.btnSellerLogout).setOnClickListener {
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}