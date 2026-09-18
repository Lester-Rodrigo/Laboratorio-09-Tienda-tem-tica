package com.example.laboratorio_09_tienda_temtica.ui

import androidx.lifecycle.ViewModel
import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.model.generateBookCatalog
import com.example.laboratorio_09_tienda_temtica.model.stableBookCoverUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {
    private val originalBooks = listOf(
        Books(
            id = "book-1",
            title = "El principito",
            description = "Una fábula sobre la amistad, el amor y aquello que realmente importa.",
            price = 89.90,
            authorId = "author-1",
            details = "Antoine de Saint-Exupéry · Primera edición: 1943 · Narrativa breve",
            genre = "Narrativa breve",
            format = "Tapa blanda",
            stock = 8,
            imageUrl = stableBookCoverUrl("book-1")
        ),
        Books(
            id = "book-2",
            title = "Cien años de soledad",
            description = "La historia de la familia Buendía y el pueblo inolvidable de Macondo.",
            price = 149.50,
            authorId = "author-2",
            details = "Gabriel García Márquez · Primera edición: 1967 · Realismo mágico",
            genre = "Realismo mágico",
            format = "Tapa dura",
            stock = 3,
            imageUrl = stableBookCoverUrl("book-2")
        ),
        Books(
            id = "book-3",
            title = "El amor en los tiempos del cólera",
            description = "Una historia sobre un amor que persiste durante más de medio siglo.",
            price = 124.00,
            authorId = "author-2",
            details = "Gabriel García Márquez · Primera edición: 1985 · Novela",
            genre = "Romance",
            format = "Tapa blanda",
            stock = 0,
            imageUrl = stableBookCoverUrl("book-3")
        )
    )
    private val authors = listOf(
        AuthorProfile(
            id = "author-1",
            name = "Antoine de Saint-Exupéry",
            role = "Escritor y aviador",
            location = "Lyon, Francia",
            description = "Autor francés cuya experiencia como aviador inspiró relatos sobre la aventura, la responsabilidad y los vínculos humanos."
        ),
        AuthorProfile(
            id = "author-2",
            name = "Gabriel García Márquez",
            role = "Escritor y periodista",
            location = "Aracataca, Colombia",
            description = "Novelista colombiano y Premio Nobel de Literatura, reconocido como una figura esencial del realismo mágico latinoamericano."
        ),
        AuthorProfile(
            id = "author-3",
            name = "Lucía Herrera",
            role = "Novelista",
            location = "Ciudad de Guatemala, Guatemala",
            description = "Autora de ficción contemporánea, misterio y relatos inspirados en la memoria de las ciudades."
        ),
        AuthorProfile(
            id = "author-4",
            name = "Mateo Salvatierra",
            role = "Escritor de aventuras",
            location = "Quetzaltenango, Guatemala",
            description = "Escritor de aventuras y fantasía cuyos personajes exploran paisajes y leyendas de América Latina."
        ),
        AuthorProfile(
            id = "author-5",
            name = "Elena Robles",
            role = "Historiadora y ensayista",
            location = "Puebla, México",
            description = "Investigadora dedicada a convertir episodios históricos en relatos accesibles para nuevos lectores."
        ),
        AuthorProfile(
            id = "author-6",
            name = "Nicolás Vega",
            role = "Autor de ciencia ficción",
            location = "San José, Costa Rica",
            description = "Autor interesado en la relación entre tecnología, identidad y futuros posibles."
        )
    )
    private val _uiState = MutableStateFlow(
        StoreUiState(
            books = generateBookCatalog(
                originalBooks = originalBooks,
                authorProfiles = authors),
            authors = authors
        )
    )
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(bookId: String) {
        _uiState.update { currentState ->
            if (currentState.books.none { it.id == bookId }) {
                currentState
            } else {
                val updatedFavorites = if (bookId in currentState.favoriteBookIds) {
                    currentState.favoriteBookIds - bookId
                } else {
                    currentState.favoriteBookIds + bookId
                }
                currentState.copy(favoriteBookIds = updatedFavorites)
            }
        }
    }
}