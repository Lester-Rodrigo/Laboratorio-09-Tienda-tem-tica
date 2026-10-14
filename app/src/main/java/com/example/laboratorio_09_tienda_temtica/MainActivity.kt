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
import com.example.laboratorio_09_tienda_temtica.ui.state.SubmitOrderUiState
import com.example.laboratorio_09_tienda_temtica.ui.orders.OrdersViewModel
import com.example.laboratorio_09_tienda_temtica.ui.theme.Laboratorio_09_Tienda_temáticaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Laboratorio_09_Tienda_temáticaTheme {
                val storeViewModel: StoreViewModel = viewModel()
                val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()
                val checkoutUiState by storeViewModel.checkoutUiState.collectAsStateWithLifecycle()
                val submitOrderUiState by storeViewModel.submitOrderUiState.collectAsStateWithLifecycle()
                val ordersViewModel: OrdersViewModel = viewModel()
                val ordersUiState by ordersViewModel.ordersUiState.collectAsStateWithLifecycle()
                val orderDetailUiState by ordersViewModel.orderDetailUiState.collectAsStateWithLifecycle()

                StoreNavigation(
                    books = uiState.books,
                    filteredBooks = uiState.filteredBooks,
                    authors = uiState.authors,
                    favoriteBookIds = uiState.favoriteBookIds,
                    searchQuery = uiState.searchQuery,
                    sortOrder = uiState.sortOrder,
                    onSearchQueryChange = storeViewModel::updateSearchQuery,
                    onClearSearch = storeViewModel::clearSearchQuery,
                    onSortOrderChange = storeViewModel::updateSortOrder,
                    onFavoriteClick = storeViewModel::toggleFavorite,
                    orderLines = uiState.orderLines,
                    orderTotal = uiState.orderTotal,
                    orderUnitCount = uiState.orderUnitCount,
                    onAddToOrder = { bookId -> storeViewModel.addToOrder(bookId) },
                    onIncreaseQuantity = storeViewModel::increaseQuantity,
                    onDecreaseQuantity = storeViewModel::decreaseQuantity,
                    onRemoveFromOrder = storeViewModel::removeFromOrder,
                    checkoutUiState = checkoutUiState,
                    onFullNameChange = storeViewModel::updateFullName,
                    onPhoneChange = storeViewModel::updatePhone,
                    onBillingTypeChange = storeViewModel::updateBillingType,
                    onNitChange = storeViewModel::updateNit,
                    onBusinessNameChange = storeViewModel::updateBusinessName,
                    onPaymentMethodChange = storeViewModel::updatePaymentMethod,
                    submitOrderUiState = submitOrderUiState,
                    onConfirmOrder = storeViewModel::confirmOrder,
                    onRetryOrder = storeViewModel::confirmOrder,
                    onResetSubmitOrderState = storeViewModel::resetSubmitOrderState,
                    ordersUiState = ordersUiState,
                    orderDetailUiState = orderDetailUiState,
                    onLoadOrders = ordersViewModel::loadOrders,
                    onLoadOrderDetail = ordersViewModel::loadOrderDetail
                )
            }
        }
    }
}
