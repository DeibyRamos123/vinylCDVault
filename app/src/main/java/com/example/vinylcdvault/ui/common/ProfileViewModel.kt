package com.example.vinylcdvault.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Carga el perfil del usuario con sesión abierta para mostrar su nombre y correo. */
class ProfileViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _profile = MutableStateFlow<User?>(null)
    val profile: StateFlow<User?> = _profile

    init {
        viewModelScope.launch {
            // Si falla la lectura, el perfil queda en null y la pantalla no cambia
            _profile.value = try { repository.currentProfile() } catch (e: Exception) { null }
        }
    }
}

/** Fábrica que entrega el repositorio del AppContainer al ViewModel. */
class ProfileViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProfileViewModel(AppContainer.authRepository) as T
}