package com.example.vinylcdvault.ui.common

import com.example.vinylcdvault.R

/** Un género musical: su código se guarda en Firestore y su texto se muestra al usuario. */
data class Genre(val code: String, val labelRes: Int)

/** Lista única de géneros, compartida por el formulario del vendedor, el inicio y el catálogo. */
object Genres {
    const val DEFAULT = "other"

    val all = listOf(
        Genre("rock", R.string.genre_rock),
        Genre("metal", R.string.genre_metal),
        Genre("jazz", R.string.genre_jazz),
        Genre("electronic", R.string.genre_electronic),
        Genre("indie", R.string.genre_indie),
        Genre("pop", R.string.genre_pop),
        Genre("soundtrack", R.string.genre_soundtracks),
        Genre(DEFAULT, R.string.genre_other)
    )
}