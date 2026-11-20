package com.example.vinylcdvault.ui.buyer

import androidx.lifecycle.ViewModel
import com.example.vinylcdvault.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.Normalizer

/** Criterios activos del catálogo: texto de búsqueda, género y formato (null = sin filtrar). */
data class CatalogFilter(
    val query: String = "",
    val genre: String? = null,
    val format: String? = null
)

// Pasa a minúsculas y quita las tildes, para que "cafe" encuentre "Café"
private fun String.normalized(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

/** Devuelve solo los discos que cumplen la búsqueda (título o artista), el género y el formato. */
fun List<Product>.applyFilter(criteria: CatalogFilter): List<Product> {
    val q = criteria.query.trim().normalized()
    return filter { p ->
        (q.isEmpty() || p.title.normalized().contains(q) || p.artist.normalized().contains(q)) &&
                (criteria.genre == null || p.genre == criteria.genre) &&
                (criteria.format == null || p.format.startsWith(criteria.format, ignoreCase = true))
    }
}

/**
 * Estado del filtro compartido entre Inicio y Catálogo (vive en la Activity del comprador).
 * Así, tocar un género en Inicio abre el Catálogo ya filtrado.
 */
class CatalogFilterViewModel : ViewModel() {
    private val _filter = MutableStateFlow(CatalogFilter())
    val filter: StateFlow<CatalogFilter> = _filter

    fun setQuery(query: String) = _filter.update { it.copy(query = query) }
    fun setGenre(genre: String?) = _filter.update { it.copy(genre = genre) }
    fun setFormat(format: String?) = _filter.update { it.copy(format = format) }

    /** Deja un único criterio de género y borra los demás. */
    fun showOnly(genre: String?) { _filter.value = CatalogFilter(genre = genre) }
}