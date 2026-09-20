package com.example.laboratorio_09_tienda_temtica.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.laboratorio_09_tienda_temtica.model.Books
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Books,
    authorName: String,
    isFavorite: Boolean,
    onFavoriteClick: (String) -> Unit,
    onAddToOrder: (String) -> Boolean,
    onAuthorClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTechnicalDetails by rememberSaveable { mutableStateOf(false) }
    var orderRequestInProgress by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalle del libro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al catálogo"
                        )
                    }
                }
            )
        }
    ) { innerPadding -> Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProductImage(
            imageUrl = book.imageUrl,
            bookTitle = book.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 2f)
        )
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = book.description,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Q %.2f".format(book.price),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (book.stock == 0) "Agotado" else "Existencias: ${book.stock}",
            style = MaterialTheme.typography.titleMedium,
            color = if (book.stock == 0) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Button(
            enabled = !orderRequestInProgress,
            onClick = {
                if (!orderRequestInProgress) {
                    orderRequestInProgress = true
                    coroutineScope.launch {
                        val accepted = onAddToOrder(book.id)
                        snackbarHostState.showSnackbar(
                            message = if (accepted) {
                                "Libro agregado al pedido."
                            } else {
                                "No se puede agregar: el libro no tiene existencias."
                            },
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                        orderRequestInProgress = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Agregar al pedido")
        }
        Button(
            onClick = { onFavoriteClick(book.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isFavorite) {
                    "★ Quitar de favoritos"
                } else {
                    "☆ Agregar a favoritos"
                }
            )
        }
        HorizontalDivider()
        OutlinedButton(
            onClick = { showTechnicalDetails = !showTechnicalDetails },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (showTechnicalDetails) {
                    "Ocultar ficha técnica"
                } else {
                    "Ver ficha técnica"
                }
            )
        }

        if (showTechnicalDetails) {
            TechnicalDetailsCard(detail = book.details)
        }
        HorizontalDivider()
        Text(
            text = "Autor relacionado",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = authorName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Consulta el perfil del autor de este libro.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = { onAuthorClick(book.authorId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Ver perfil del autor")
        }
    }
    }
}

@Composable
private fun TechnicalDetailsCard(
    detail: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ficha técnica",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
