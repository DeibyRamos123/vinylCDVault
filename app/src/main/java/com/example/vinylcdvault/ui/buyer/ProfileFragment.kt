package com.example.vinylcdvault.ui.buyer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.vinylcdvault.R
import com.example.vinylcdvault.ui.common.showInfo
import com.example.vinylcdvault.ui.auth.LoginActivity
import com.example.vinylcdvault.ui.common.logoutToLogin
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.rowOrders).setOnClickListener {
            startActivity(Intent(requireContext(), ListActivity::class.java).putExtra("mode", "orders"))
        }
        view.findViewById<View>(R.id.rowFavorites).setOnClickListener {
            startActivity(Intent(requireContext(), ListActivity::class.java).putExtra("mode", "favorites"))
        }
        view.findViewById<View>(R.id.rowAddresses).setOnClickListener {
            showInfo(R.string.info_addresses_title, R.string.info_addresses_msg)
        }
        view.findViewById<View>(R.id.rowWithdrawal).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.withdrawal_title)
                .setMessage(R.string.withdrawal_msg)
                .setPositiveButton(R.string.withdrawal_confirm) { _, _ ->
                    Toast.makeText(requireContext(), R.string.withdrawal_sent, Toast.LENGTH_LONG).show()
                }
                .setNegativeButton(R.string.seller_delete_cancel, null)
                .show()
        }

        view.findViewById<View>(R.id.btnLogout).setOnClickListener { logoutToLogin() }
    }
}