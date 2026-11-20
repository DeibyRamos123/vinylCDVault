package com.example.vinylcdvault.ui.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.CartItem
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.repository.CartRepository
import com.example.vinylcdvault.repository.OrderRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.asResource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Lógica del carrito y de la creación del pedido. */
class CartViewModel(
    private val cart: CartRepository,
    private val orders: OrderRepository
) : ViewModel() {

    /** Discos del carrito en tiempo real. */
    val items: StateFlow<Resource<List<CartItem>>> = cart.observeCart().asResource(viewModelScope)

    // Resultado de la última operación con aviso: éxito con el id del mensaje, o error con texto
    private val _opState = MutableStateFlow<Resource<Int>?>(null)
    val opState: StateFlow<Resource<Int>?> = _opState

    fun add(product: Product) = runOp(R.string.detail_added) { cart.add(product) }

    fun increase(item: CartItem) = quiet { cart.changeQuantity(item.productId, item.quantity + 1) }

    /** Con una sola unidad no baja más; para quitar el disco se usa el botón de eliminar. */
    fun decrease(item: CartItem) {
        if (item.quantity > 1) quiet { cart.changeQuantity(item.productId, item.quantity - 1) }
    }

    fun remove(item: CartItem) = runOp(R.string.cart_item_removed) { cart.remove(item.productId) }


    /** Crea el pedido con los discos recibidos, la dirección y las coordenadas de entrega. */
    fun placeOrder(
        items: List<CartItem>, address: String, latitude: Double, longitude: Double,
        paymentReference: String, transactionId: String
    ) = runOp(R.string.order_placed) {
        orders.placeOrder(items, address, latitude, longitude, paymentReference, transactionId)
    }

    fun resetOp() { _opState.value = null }

    // Operación con aviso de éxito o error
    private fun runOp(successMessage: Int, block: suspend () -> Unit) {
        _opState.value = Resource.Loading
        viewModelScope.launch {
            _opState.value = try {
                block()
                Resource.Success(successMessage)
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }

    // Operación sin aviso de éxito (la lista se actualiza sola); solo informa si falla
    private fun quiet(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                _opState.value = Resource.Error(e.toFriendlyMessage())
            }
        }
    }
}

/** Fábrica que entrega los repositorios del AppContainer al ViewModel. */
class CartViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CartViewModel(AppContainer.cartRepository, AppContainer.orderRepository) as T
}