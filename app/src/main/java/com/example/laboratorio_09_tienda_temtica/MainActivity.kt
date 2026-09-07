package com.example.laboratorio_09_tienda_temtica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laboratorio_09_tienda_temtica.navigation.StoreNavigation
import com.example.laboratorio_09_tienda_temtica.ui.StoreViewModel
import com.example.laboratorio_09_tienda_temtica.ui.theme.Laboratorio_09_Tienda_temáticaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Laboratorio_09_Tienda_temáticaTheme {
                val storeViewModel: StoreViewModel = viewModel()
                val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()

                StoreNavigation(
                    books = uiState.books,
                    authors = uiState.authors,
                    favoriteBookIds = uiState.favoriteBookIds,
                    onFavoriteClick = storeViewModel::toggleFavorite
                )
            }
        }
    }
}
