package com.example.laboratorio_09_tienda_temtica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

private enum class ProductImageState {
    Loading,
    Success,
    Error
}

@Composable
fun ProductImage(
    imageUrl: String,
    bookTitle: String,
    modifier: Modifier = Modifier
) {
    var imageState by remember(imageUrl) {
        mutableStateOf(ProductImageState.Loading)
    }
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Portada de $bookTitle",
            contentScale = ContentScale.Crop,
            onLoading = { imageState = ProductImageState.Loading },
            onSuccess = { imageState = ProductImageState.Success },
            onError = { imageState = ProductImageState.Error },
            modifier = Modifier.fillMaxSize()
        )

        when (imageState) {
            ProductImageState.Loading -> {
                Text(
                    text = "Cargando portada…",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            ProductImageState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Portada no disponible",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
            ProductImageState.Success -> Unit
        }
    }
}
