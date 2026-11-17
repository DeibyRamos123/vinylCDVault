package com.example.vinylcdvault.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.repository.OrderRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.asResource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Lista de pedidos del comprador y del vendedor, y cambio de estado. Solo escucha la lista que se observa. */
class OrdersViewModel(private val repository: OrderRepository) : ViewModel() {

    val myOrders: StateFlow<Resource<List<Order>>> = repository.observeMyOrders().asResource(viewModelScope)
    val sellerOrders: StateFlow<Resource<List<Order>>> = repository.observeSellerOrders().asResource(viewModelScope)

    private val _opState = MutableStateFlow<Resource<Int>?>(null)
    val opState: StateFlow<Resource<Int>?> = _opState

    /** Cambia el estado de entrega de un pedido. */
    fun updateStatus(orderId: String, status: String) {
        viewModelScope.launch {
            _opState.value = try {
                repository.updateStatus(orderId, status)
                Resource.Success(R.string.order_status_updated)
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }

    fun resetOp() { _opState.value = null }
}

/** Fábrica que entrega el repositorio del AppContainer al ViewModel. */
class OrdersViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OrdersViewModel(AppContainer.orderRepository) as T
}