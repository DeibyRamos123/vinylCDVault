package com.example.vinylcdvault.ui.seller

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.model.OrderStatus
import com.example.vinylcdvault.model.WithdrawalStatus
import com.example.vinylcdvault.ui.common.OrderAdapter
import com.example.vinylcdvault.ui.common.OrdersViewModel
import com.example.vinylcdvault.ui.common.OrdersViewModelFactory
import com.example.vinylcdvault.ui.common.orderStatusRes
import com.example.vinylcdvault.ui.common.reportLoading
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toCop
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Pedidos recibidos por el vendedor. Al tocar uno puede cambiar su estado de envío,
 * o, si el comprador pidió un retracto, aceptarlo o rechazarlo.
 */
class SellerOrdersFragment : Fragment(R.layout.fragment_seller_orders) {

    private val viewModel: OrdersViewModel by viewModels { OrdersViewModelFactory() }
    private val statuses = listOf(OrderStatus.PENDING, OrderStatus.SHIPPED, OrderStatus.DELIVERED)

    // El adaptador se crea con el uid del vendedor para mostrar solo sus discos de cada pedido
    private val orderAdapter by lazy {
        OrderAdapter(sellerId = AppContainer.auth.currentUser?.uid, onClick = ::onOrderClick)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<RecyclerView>(R.id.rvOrders).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sellerOrders.collect { state ->
                        reportLoading(state)
                        when (state) {
                            is Resource.Success -> orderAdapter.submitList(state.data)
                            is Resource.Error -> toast(state.message)
                            Resource.Loading -> Unit
                        }
                    }
                }
                launch {
                    viewModel.opState.collect { state ->
                        when (state) {
                            is Resource.Success -> { toast(getString(state.data)); viewModel.resetOp() }
                            is Resource.Error -> { toast(state.message); viewModel.resetOp() }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    // Según el pedido: responder el retracto, cambiar el envío, o avisar que ya fue resuelto
    private fun onOrderClick(order: Order) {
        when (order.withdrawalStatus) {
            WithdrawalStatus.REQUESTED -> showWithdrawalDialog(order)
            WithdrawalStatus.NONE -> showStatusDialog(order)
            else -> toast(getString(R.string.withdrawal_already))
        }
    }

    // Diálogo con los tres estados posibles del pedido
    private fun showStatusDialog(order: Order) {
        val labels = statuses.map { getString(orderStatusRes(it)) }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.order_status_title)
            .setItems(labels) { _, which -> viewModel.updateStatus(order.id, statuses[which]) }
            .show()
    }

    // Diálogo para aceptar o rechazar la solicitud de retracto del comprador
    private fun showWithdrawalDialog(order: Order) {
        val orderName = getString(R.string.order_number, order.id.takeLast(5).uppercase())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.withdrawal_seller_title)
            .setMessage(getString(R.string.withdrawal_seller_msg, order.buyerName, orderName, order.total.toCop()))
            .setPositiveButton(R.string.withdrawal_accept) { _, _ -> viewModel.resolveWithdrawal(order.id, true) }
            .setNegativeButton(R.string.withdrawal_reject) { _, _ -> viewModel.resolveWithdrawal(order.id, false) }
            .setNeutralButton(R.string.dialog_close, null)
            .show()
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}