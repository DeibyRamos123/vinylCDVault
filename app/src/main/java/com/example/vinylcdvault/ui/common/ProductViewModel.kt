package com.example.vinylcdvault.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Product
import com.example.vinylcdvault.repository.ProductRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lógica compartida de los discos: lista en tiempo real y operaciones de guardar y eliminar. */
class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    /** Todos los discos publicados; cada pantalla filtra lo que necesita. */
    val products: StateFlow<Resource<List<Product>>> = repository.observeProducts()
        .map<List<Product>, Resource<List<Product>>> { Resource.Success(it) }
        .catch { emit(Resource.Error(it.toFriendlyMessage())) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    // Resultado de la última operación: éxito con el id del mensaje, o error con texto
    private val _opState = MutableStateFlow<Resource<Int>?>(null)
    val opState: StateFlow<Resource<Int>?> = _opState

    /** Crea o actualiza un disco según tenga o no id. */
    fun save(product: Product, imageBytes: ByteArray? = null) {
        val message = if (product.id.isEmpty()) R.string.product_saved else R.string.product_updated
        runOp(message) { repository.saveProduct(product, imageBytes) }
    }

    fun delete(id: String) = runOp(R.string.seller_deleted) { repository.deleteProduct(id) }

    /** Limpia el resultado una vez mostrado. */
    fun resetOp() { _opState.value = null }

    // Ejecuta una operación y publica Loading, Success o Error
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
}

/** Fábrica que entrega el repositorio del AppContainer al ViewModel. */
class ProductViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProductViewModel(AppContainer.productRepository) as T
}