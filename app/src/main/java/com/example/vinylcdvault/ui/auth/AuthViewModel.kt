package com.example.vinylcdvault.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.repository.AuthRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Lógica de las pantallas de acceso. Expone el estado de cada operación como StateFlow
 * (null = nada pendiente) y las pantallas solo lo observan.
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState

    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState

    private val _recoverState = MutableStateFlow<Resource<Unit>?>(null)
    val recoverState: StateFlow<Resource<Unit>?> = _recoverState

    /** Inicia sesión con correo y contraseña. */
    fun login(email: String, password: String) = launchLogin { repository.login(email, password) }

    /** Inicia sesión con google. */

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _loginState.value = repository.loginWithGoogle(idToken)
        }
    }

    /** Entra con la sesión ya guardada (se usa después de verificar la huella). */
    fun restoreSession() = launchLogin {
        repository.currentProfile() ?: throw IllegalStateException("Inicia sesión con tu correo primero")
    }

    fun hasSession(): Boolean = repository.hasSession()

    fun register(name: String, email: String, phone: String, password: String, role: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            _registerState.value = try {
                Resource.Success(repository.register(name, email, phone, password, role))
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }

    fun sendPasswordReset(email: String) {
        _recoverState.value = Resource.Loading
        viewModelScope.launch {
            _recoverState.value = try {
                repository.sendPasswordReset(email)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }

    /** Limpia los estados una vez atendidos, para no repetir mensajes al girar la pantalla. */
    fun resetStates() {
        _loginState.value = null
        _registerState.value = null
        _recoverState.value = null
    }

    // Ejecuta una operación de login y publica Loading, Success o Error
    private fun launchLogin(block: suspend () -> User) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            _loginState.value = try {
                Resource.Success(block())
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }
}

/** Fábrica que le entrega al ViewModel el repositorio del AppContainer. */
class AuthViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AuthViewModel(AppContainer.authRepository) as T
}