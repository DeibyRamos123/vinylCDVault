package com.example.vinylcdvault.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vinylcdvault.di.AppContainer
import com.example.vinylcdvault.repository.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Ids de los discos favoritos y la acción de marcar o desmarcar. */
class FavoriteViewModel(private val repository: FavoriteRepository) : ViewModel() {

    val ids: StateFlow<Set<String>> = repository.observeIds()
        .catch { emit(emptySet()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** Si el disco es favorito lo quita; si no, lo agrega. Si falla, la lista no cambia. */
    fun toggle(productId: String) {
        val makeFavorite = productId !in ids.value
        viewModelScope.launch {
            try { repository.setFavorite(productId, makeFavorite) } catch (e: Exception) { /* sin cambios */ }
        }
    }
}

/** Fábrica que entrega el repositorio del AppContainer al ViewModel. */
class FavoriteViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        FavoriteViewModel(AppContainer.favoriteRepository) as T
}