package com.example.vinylcdvault.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.R
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.model.User
import com.example.vinylcdvault.repository.AuthRepository
import com.example.vinylcdvault.repository.UserRepository
import com.example.vinylcdvault.util.Resource
import com.example.vinylcdvault.util.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Perfil del usuario con sesión abierta: lo carga, y permite cambiar su foto. */
class ProfileViewModel(
    private val auth: AuthRepository,
    private val users: UserRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<Resource<User>>(Resource.Loading)
    val profile: StateFlow<Resource<User>> = _profile

    // Resultado del cambio de foto: éxito con el id del mensaje, o error con texto
    private val _photoOp = MutableStateFlow<Resource<Int>?>(null)
    val photoOp: StateFlow<Resource<Int>?> = _photoOp

    init { load() }

    // Lee el perfil de Firestore; no pasa por "cargando" al recargar, para que el spinner no parpadee
    private fun load() {
        viewModelScope.launch {
            _profile.value = try {
                auth.currentProfile()?.let { Resource.Success(it) }
                    ?: Resource.Error("Inicia sesión para ver tu perfil")
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }

    /** Sube la foto (ya comprimida) y recarga el perfil para mostrarla. */
    fun updatePhoto(imageBytes: ByteArray) {
        val uid = (_profile.value as? Resource.Success)?.data?.uid ?: return
        _photoOp.value = Resource.Loading
        viewModelScope.launch {
            _photoOp.value = try {
                users.updatePhoto(uid, imageBytes)
                load()
                Resource.Success(R.string.photo_updated)
            } catch (e: Exception) {
                Resource.Error(e.toFriendlyMessage())
            }
        }
    }

    fun resetPhotoOp() { _photoOp.value = null }
}

/** Fábrica que entrega los repositorios del AppContainer al ViewModel. */
class ProfileViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProfileViewModel(AppContainer.authRepository, AppContainer.userRepository) as T
}