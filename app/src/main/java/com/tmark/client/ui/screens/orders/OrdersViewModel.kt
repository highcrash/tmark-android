package com.tmark.client.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.model.OrderDetail
import com.tmark.client.data.model.OrderSummary
import com.tmark.client.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val loading: Boolean = false,
    val orders: List<OrderSummary> = emptyList(),
    val error: String? = null
)

data class OrderDetailUiState(
    val loading: Boolean = false,
    val order: OrderDetail? = null,
    val error: String? = null
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repo: OrderRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(OrdersUiState())
    val ui: StateFlow<OrdersUiState> = _ui

    private val _detail = MutableStateFlow(OrderDetailUiState())
    val detail: StateFlow<OrderDetailUiState> = _detail

    init { load() }

    fun load() {
        viewModelScope.launch {
            _ui.value = OrdersUiState(loading = true)
            when (val r = repo.getOrders()) {
                is ApiResult.Success   -> _ui.value = OrdersUiState(orders = r.data.orders)
                is ApiResult.Error     -> _ui.value = OrdersUiState(error = r.message)
                is ApiResult.Exception -> _ui.value = OrdersUiState(error = "Network error")
            }
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detail.value = OrderDetailUiState(loading = true)
            when (val r = repo.getOrderDetail(id)) {
                is ApiResult.Success   -> _detail.value = OrderDetailUiState(order = r.data.order)
                is ApiResult.Error     -> _detail.value = OrderDetailUiState(error = r.message)
                is ApiResult.Exception -> _detail.value = OrderDetailUiState(error = "Network error")
            }
        }
    }
}
