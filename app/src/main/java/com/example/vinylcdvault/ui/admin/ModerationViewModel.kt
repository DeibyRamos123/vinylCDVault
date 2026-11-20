package com.example.vinylcdvault.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.repository.ProductRepository
import com.example.vinylcdvault.repository.UserRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Lógica de moderación de un disco: datos de quien lo publicó, suspender o reactivar al vendedor y eliminar el disco. */
class ModerationViewModel(
    private val users: UserRepository,
    private val products: ProductRepository
) : ViewModel() {

    private val _seller = MutableStateFlow<Resource<User>>(Resource.Loading)
    val seller: StateFlow<Resource<User>> = _seller

    // Resultado de la última operación: éxito con el id del mensaje, o error con texto
    private val _opState = MutableStateFlow<Resource<Int>?>(null)
    val opState: StateFlow<Resource<Int>?> = _opState

    /** Carga el perfil de quien publicó el disco; falla si la cuenta ya no existe. */
    fun loadSeller(sellerId: String) {
        viewModelScope.launch {
            _seller.value = try {
                users.getUser(sellerId)?.let { Resource.Success(it) }
                    ?: Resource.Error("Vendedor no disponible")
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }

    /**
     * Suspende o reactiva al vendedor. El repositorio oculta o vuelve a mostrar
     * todos sus discos junto con el cambio de estado de la cuenta.
     */
    fun setSellerActive(seller: User, active: Boolean) =
        runOp(if (active) R.string.moderation_seller_reactivated else R.string.moderation_seller_suspended) {
            users.updateUser(seller.uid, seller.name, seller.role, active)
            _seller.value = Resource.Success(seller.copy(active = active))   // se refleja sin volver a leer
        }

    fun deleteProduct(productId: String) = runOp(R.string.seller_deleted) { products.deleteProduct(productId) }

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

/** Fábrica que entrega los repositorios del AppContainer al ViewModel. */
class ModerationViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ModerationViewModel(AppContainer.userRepository, AppContainer.productRepository) as T
}