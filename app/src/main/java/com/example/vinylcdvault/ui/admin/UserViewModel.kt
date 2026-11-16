package com.example.vinylcdvault.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.repository.UserRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lógica de la gestión de usuarios: lista en tiempo real y operaciones de crear, editar y eliminar. */
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    /** Lista de usuarios; se mantiene mientras haya una pantalla observándola. */
    val users: StateFlow<Resource<List<User>>> = repository.observeUsers()
        .map<List<User>, Resource<List<User>>> { Resource.Success(it) }
        .catch { emit(Resource.Error(it.toFriendlyMessage())) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    // Resultado de la última operación: éxito con el id del mensaje, o error con texto
    private val _opState = MutableStateFlow<Resource<Int>?>(null)
    val opState: StateFlow<Resource<Int>?> = _opState

    fun createUser(name: String, email: String, password: String, role: String, active: Boolean) =
        runOp(R.string.user_created) { repository.createUser(name, email, password, role, active) }

    fun updateUser(uid: String, name: String, role: String, active: Boolean) =
        runOp(R.string.user_updated) { repository.updateUser(uid, name, role, active) }

    fun deleteUser(uid: String) =
        runOp(R.string.admin_user_deleted) { repository.deleteUser(uid) }

    /** Limpia el resultado una vez mostrado, para no repetir el aviso. */
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
class UserViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        UserViewModel(AppContainer.userRepository) as T
}