package com.example.laboratorio_09_tienda_temtica.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.laboratorio_09_tienda_temtica.model.Books

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Books,
    isFavorite: Boolean,
    onFavoriteClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTechnicalDetails by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
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