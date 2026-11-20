package com.example.vinylcdvault.ui.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.repository.OrderRepository
import com.example.vinylcdvault.repository.ProductRepository
import com.example.vinylcdvault.ui.admin.SellerStats
import com.example.vinylcdvault.ui.admin.StatsCalculator
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.asResource
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/** Estadísticas del vendedor en tiempo real, calculadas con sus discos y sus pedidos pagados. */
class SellerProfileViewModel(
    products: ProductRepository,
    orders: OrderRepository,
    sellerId: String
) : ViewModel() {

    val stats: StateFlow<Resource<SellerStats>> =
        combine(products.observeProducts(), orders.observeSellerOrders()) { p, o ->
            StatsCalculator.seller(sellerId, p, o)
        }.asResource(viewModelScope)
}

/** Fábrica que entrega los repositorios y el id del vendedor con sesión abierta. */
class SellerProfileViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SellerProfileViewModel(
            AppContainer.productRepository,
            AppContainer.orderRepository,
            AppContainer.auth.currentUser?.uid.orEmpty()
        ) as T
}