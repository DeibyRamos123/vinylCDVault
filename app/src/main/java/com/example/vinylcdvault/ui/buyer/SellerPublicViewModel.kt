package com.example.vinylcdvault.ui.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.repository.ProductRepository
import com.example.vinylcdvault.repository.UserRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.asResource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Datos públicos de un vendedor, vistos por los compradores: su perfil y los discos que publicó. */
class SellerPublicViewModel(
    private val users: UserRepository,
    private val products: ProductRepository
) : ViewModel() {

    private val _seller = MutableStateFlow<Resource<User>>(Resource.Loading)
    val seller: StateFlow<Resource<User>> = _seller

    /** Carga el perfil del vendedor; si no existe, informa el error. */
    fun loadSeller(sellerId: String) {
        viewModelScope.launch {
            _seller.value = try {
                users.getUser(sellerId)?.let { Resource.Success(it) }
                    ?: Resource.Error("No se encontró al vendedor")
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }

    /** Discos publicados por ese vendedor, en tiempo real. */
    fun productsOf(sellerId: String): StateFlow<Resource<List<Product>>> =
        products.observeProducts()
            .map { list -> list.filter { it.sellerId == sellerId } }
            .asResource(viewModelScope)
}

/** Fábrica que entrega los repositorios del AppContainer al ViewModel. */
class SellerPublicViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SellerPublicViewModel(AppContainer.userRepository, AppContainer.productRepository) as T
}