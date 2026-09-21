package com.example.laboratorio_09_tienda_temtica.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import com.example.laboratorio_09_tienda_temtica.model.OrderLine
import com.example.laboratorio_09_tienda_temtica.model.OrderResult
import com.example.laboratorio_09_tienda_temtica.ui.screens.AuthorProfileScreen
import com.example.laboratorio_09_tienda_temtica.ui.screens.BookDetailScreen
import com.example.laboratorio_09_tienda_temtica.ui.screens.CatalogScreen
import com.example.laboratorio_09_tienda_temtica.ui.screens.OrderScreen
import java.math.BigDecimal

@Composable
fun StoreNavigation(
    books: List<Books>,
    filteredBooks: List<Books>,
    authors: List<AuthorProfile>,
    favoriteBookIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFavoriteClick: (String) -> Unit,
    orderLines: List<OrderLine>,
    orderTotal: BigDecimal,
    orderUnitCount: Int,
    onAddToOrder: (String) -> OrderResult,
    onIncreaseQuantity: (String) -> OrderResult,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveFromOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(CatalogKey)
    val navigateBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
    val openOrder: () -> Unit = {
        if (backStack.lastOrNull() != OrderKey) {
            backStack.add(OrderKey)
        }
    }
    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = navigateBack,
        transitionSpec = {
            (slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(220)
            ) + fadeIn(tween(220))) togetherWith
                (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(220)
                ) + fadeOut(tween(160)))
        },
        popTransitionSpec = {
            (slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(220)
            ) + fadeIn(tween(220))) togetherWith
                (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(220)
                ) + fadeOut(tween(160)))
        },
        predictivePopTransitionSpec = { _ ->
            (slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(220)
            ) + fadeIn(tween(220))) togetherWith
                (slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(220)
                ) + fadeOut(tween(160)))
        },
        entryProvider = entryProvider {
            entry<CatalogKey> {
                CatalogScreen(
                    books = filteredBooks,
                    favoriteBookIds = favoriteBookIds,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onClearSearch = onClearSearch,
                    onBookClick = { bookId -> backStack.add(BookDetailKey(bookId = bookId)) },
                    onFavoriteClick = onFavoriteClick,
                    orderUnitCount = orderUnitCount,
                    onOrderClick = openOrder
                )
            }
            entry<BookDetailKey> { key ->
                val selectedBook = books.firstOrNull { book ->
                    book.id == key.bookId
                }

                if (selectedBook != null) {
                    val selectedAuthor = authors.firstOrNull { author ->
                        author.id == selectedBook.authorId
                    }
                    BookDetailScreen(
                        book = selectedBook,
                        authorName = selectedAuthor?.name ?: "Autor no disponible",
                        isFavorite = selectedBook.id in favoriteBookIds,
                        onFavoriteClick = onFavoriteClick,
                        onAddToOrder = onAddToOrder,
                        orderUnitCount = orderUnitCount,
                        onOrderClick = openOrder,
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
            entry<OrderKey> {
                OrderScreen(
                    orderLines = orderLines,
                    orderTotal = orderTotal,
                    orderUnitCount = orderUnitCount,
                    onIncrease = onIncreaseQuantity,
                    onDecrease = onDecreaseQuantity,
                    onRemove = onRemoveFromOrder,
                    onGoToCatalog = {
                        while (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    },
                    onBackClick = navigateBack
                )
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
