package com.example.vinylcdvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class SellerProfileFragment : Fragment(R.layout.fragment_seller_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnSellerLogout).setOnClickListener {
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}