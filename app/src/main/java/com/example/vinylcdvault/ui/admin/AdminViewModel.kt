package com.example.vinylcdvault.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Order
import com.example.vinylcdvault.repository.OrderRepository
import com.example.vinylcdvault.repository.ProductRepository
import com.example.vinylcdvault.repository.UserRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.asResource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estadísticas del panel y de los reportes, calculadas en tiempo real con los datos de Firestore. */
class AdminViewModel(
    users: UserRepository,
    products: ProductRepository,
    orderRepository: OrderRepository
) : ViewModel() {

    /** Todos los pedidos; la lista vacía se usa también si falla la lectura. */
    val orders: StateFlow<List<Order>> = orderRepository.observeAllOrders()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Cifras del panel principal. */
    val dashboard: StateFlow<Resource<DashboardStats>> =
        combine(users.observeUsers(), products.observeProducts(), orders) { u, p, o ->
            StatsCalculator.dashboard(u, p, o)
        }.asResource(viewModelScope)

    /** Cifras de los reportes de ventas. */
    val report: StateFlow<Resource<ReportStats>> =
        orders.map { StatsCalculator.report(it) }.asResource(viewModelScope)
}

/** Fábrica que entrega los repositorios del AppContainer al ViewModel. */
class AdminViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AdminViewModel(AppContainer.userRepository, AppContainer.productRepository, AppContainer.orderRepository) as T
}