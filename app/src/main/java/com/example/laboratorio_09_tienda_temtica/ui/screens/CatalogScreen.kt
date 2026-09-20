package com.example.laboratorio_09_tienda_temtica.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.laboratorio_09_tienda_temtica.model.Books
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    books: List<Books>,
    favoriteBookIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onBookClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex >= 6 ||
                (gridState.firstVisibleItemIndex > 0 && gridState.firstVisibleItemScrollOffset > 200)
        }
    }
    val updateQueryAndResetScroll: (String) -> Unit = { newQuery ->
        if (newQuery != searchQuery) {
            onSearchQueryChange(newQuery)
            coroutineScope.launch { gridState.scrollToItem(0) }
        }
    }
    val clearQueryAndResetScroll: () -> Unit = {
        if (searchQuery.isNotEmpty()) {
            onClearSearch()
            coroutineScope.launch { gridState.scrollToItem(0) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Librería Literaria")
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch { gridState.animateScrollToItem(0) }
                    }
                ) {
                    Text(
                        text = "↑",
                        modifier = Modifier.semantics {
                            contentDescription = "Volver al inicio del catálogo"
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Catálogo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecciona un libro para conocer sus detalles.",
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = updateQueryAndResetScroll,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Buscar por título") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = clearQueryAndResetScroll) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda"
                                )
                            }
                        }
                    }
                )
                Text(
                    text = "${books.size} resultados",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (books.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "No encontramos libros con ese título.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(onClick = clearQueryAndResetScroll) {
                            Text(text = "Limpiar búsqueda")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = books,
                        key = { book -> book.id }
                    ) { book ->
                        ProductCard(
                            book = book,
                            isFavorite = book.id in favoriteBookIds,
                            onBookClick = { onBookClick(book.id) },
                            onFavoriteClick = { onFavoriteClick(book.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    book: Books,
    isFavorite: Boolean,
    onBookClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onBookClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProductImage(
                imageUrl = book.imageUrl,
                bookTitle = book.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = book.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onFavoriteClick) {
                    Text(
                        text = if (isFavorite) "★" else "☆",
                        modifier = Modifier.semantics {
                            contentDescription = if (isFavorite) {
                                "Quitar ${book.title} de favoritos"
                            } else {
                                "Agregar ${book.title} a favoritos"
                            }
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Text(
                text = book.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Q %.2f".format(book.price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (book.stock == 0) "Agotado" else "${book.stock} disponibles",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Toca para ver el detalle",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
