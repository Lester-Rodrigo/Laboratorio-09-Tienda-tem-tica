package com.example.laboratorio_09_tienda_temtica.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.laboratorio_09_tienda_temtica.model.AuthorProfile
import com.example.laboratorio_09_tienda_temtica.model.Books
import com.example.laboratorio_09_tienda_temtica.ui.screens.AuthorProfileScreen
import com.example.laboratorio_09_tienda_temtica.ui.screens.BookDetailScreen
import com.example.laboratorio_09_tienda_temtica.ui.screens.CatalogScreen

@Composable
fun StoreNavigation(
    books: List<Books>,
    authors: List<AuthorProfile>,
    favoriteBookIds: Set<String>,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(CatalogKey)
    val navigateBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = navigateBack,
        entryProvider = entryProvider {
            entry<CatalogKey> {
                CatalogScreen(
                    books = books,
                    favoriteBookIds = favoriteBookIds,
                    onBookClick = { bookId -> backStack.add(BookDetailKey(bookId = bookId)) },
                    onFavoriteClick = onFavoriteClick
                )
            }
            entry<BookDetailKey> { key ->
                val selectedBook = books.firstOrNull { book ->
                    book.id == key.bookId
                }

                if (selectedBook != null) {
                    BookDetailScreen(
                        book = selectedBook,
                        isFavorite = selectedBook.id in favoriteBookIds,
                        onFavoriteClick = onFavoriteClick,
                        onAuthorClick = { authorId ->
                            backStack.add(AuthorProfileKey(authorId = authorId))
                        },
                        onBackClick = navigateBack
                    )
                } else {
                    MissingDestinationScreen(
                        message = "No se encontró el libro solicitado.",
                        onBackClick = navigateBack
                    )
                }
            }
            entry<AuthorProfileKey> { key ->
                val selectedAuthor = authors.firstOrNull { author ->
                    author.id == key.authorId
                }

                if (selectedAuthor != null) {
                    AuthorProfileScreen(
                        author = selectedAuthor,
                        onBackClick = navigateBack
                    )
                } else {
                    MissingDestinationScreen(
                        message = "No se encontró el autor solicitado.",
                        onBackClick = navigateBack
                    )
                }
            }
        }
    )
}

@Composable
private fun MissingDestinationScreen(
    message: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Regresar")
        }
    }
}
