package com.example.vinylcdvault.ui.admin

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.vinylcdvault.R
import com.example.vinylcdvault.ui.common.logoutToLogin
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCompactCop
import com.example.vinylcdvault.util.toCop
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

/** Panel principal del administrador: estadísticas en tiempo real y accesos rápidos. */
class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    private val viewModel: AdminViewModel by viewModels { AdminViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnAdminLogout).setOnClickListener { logoutToLogin() }

        // Las tarjetas de usuarios y discos llevan a su pestaña
        view.findViewById<View>(R.id.cardUsers).setOnClickListener { goTo(R.id.nav_admin_users) }
        view.findViewById<View>(R.id.cardProducts).setOnClickListener { goTo(R.id.nav_admin_products) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboard.collect { state ->
                    reportLoading(state)
                    if (state is Resource.Success) show(view, state.data)
                }
            }
        }
    }

    // Escribe las cifras en pantalla
    private fun show(root: View, stats: DashboardStats) {
        fun set(id: Int, value: String) { root.findViewById<TextView>(id).text = value }
        set(R.id.tvStatUsers, stats.users.toString())
        set(R.id.tvStatProducts, stats.products.toString())
        set(R.id.tvStatOrders, stats.ordersThisMonth.toString())
        set(R.id.tvStatSales, stats.salesThisMonth.toCompactCop())
        set(R.id.tvOpToShip, stats.ordersToShip.toString())
        set(R.id.tvOpShipped, stats.ordersShipped.toString())
        set(R.id.tvOpSuspended, stats.suspendedUsers.toString())
        set(R.id.tvOpNoPhoto, stats.productsWithoutPhoto.toString())

        // Últimos pedidos: una fila por pedido, o un aviso si todavía no hay
        val container = root.findViewById<LinearLayout>(R.id.llRecentOrders)
        container.removeAllViews()
        val rows = stats.recentOrders.map { o ->
            getString(R.string.admin_recent_line, o.id.takeLast(5).uppercase(), o.buyerName, o.total.toCop())
        }.ifEmpty { listOf(getString(R.string.admin_recent_empty)) }
        rows.forEach { text ->
            container.addView(
                TextView(requireContext(), null, 0, R.style.App_ProfileRow).apply {
                    this.text = text
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            )
        }
    }

    // Cambia la pestaña de la barra inferior del administrador
    private fun goTo(itemId: Int) {
        requireActivity().findViewById<BottomNavigationView>(R.id.adminBottomNav).selectedItemId = itemId
    }
}