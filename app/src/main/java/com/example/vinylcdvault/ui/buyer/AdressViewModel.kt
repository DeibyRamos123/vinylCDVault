package com.example.vinylcdvault.ui.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.Address
import com.example.vinylcdvault.repository.AddressRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.asResource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Lógica de las direcciones guardadas: lista en tiempo real y operaciones de guardar, predeterminar y eliminar. */
class AddressViewModel(private val repository: AddressRepository) : ViewModel() {

    val addresses: StateFlow<Resource<List<Address>>> = repository.observeAddresses().asResource(viewModelScope)

    // Resultado de la última operación: éxito con el id del mensaje, o error con texto
    private val _opState = MutableStateFlow<Resource<Int>?>(null)
    val opState: StateFlow<Resource<Int>?> = _opState

    fun save(address: Address) = runOp(R.string.address_saved) { repository.save(address) }
    fun setPrimary(id: String) = runOp(R.string.address_saved) { repository.setPrimary(id) }
    fun delete(id: String) = runOp(R.string.address_deleted) { repository.delete(id) }

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
class AddressViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AddressViewModel(AppContainer.addressRepository) as T
}