package com.example.vinylcdvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnAdminLogout).setOnClickListener {
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}