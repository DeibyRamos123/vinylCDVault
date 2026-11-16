package com.example.vinylcdvault.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.vinylcdvault.R
import com.example.vinylcdvault.ui.common.showInfo
import com.example.vinylcdvault.ui.auth.LoginActivity
import com.example.vinylcdvault.ui.common.logoutToLogin

class SellerProfileFragment : Fragment(R.layout.fragment_seller_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.rowStore).setOnClickListener { showInfo(R.string.info_store_title, R.string.info_store_msg) }
        view.findViewById<View>(R.id.rowShipping).setOnClickListener { showInfo(R.string.info_shipping_title, R.string.info_shipping_msg) }
        view.findViewById<View>(R.id.rowPayments).setOnClickListener { showInfo(R.string.info_payments_title, R.string.info_payments_msg) }
        view.findViewById<View>(R.id.rowHelp).setOnClickListener { showInfo(R.string.info_help_title, R.string.info_help_msg) }
        view.findViewById<View>(R.id.btnSellerLogout).setOnClickListener { logoutToLogin() }
    }
}