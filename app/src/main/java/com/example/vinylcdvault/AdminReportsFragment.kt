package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

class AdminReportsFragment : Fragment(R.layout.fragment_admin_reports) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnExport).setOnClickListener {
            Toast.makeText(requireContext(), R.string.reports_export_toast, Toast.LENGTH_SHORT).show()
        }
    }
}