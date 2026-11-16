package com.example.vinylcdvault.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.vinylcdvault.R
import com.example.vinylcdvault.ui.auth.LoginActivity
import com.example.vinylcdvault.ui.common.logoutToLogin

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnAdminLogout).setOnClickListener { logoutToLogin() }
    }
}