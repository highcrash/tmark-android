package com.tmark.client.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.model.InvoiceSummary
import com.tmark.client.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoicesUiState(
    val loading: Boolean = false,
    val invoices: List<InvoiceSummary> = emptyList(),
    val outstandingBalance: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class InvoicesViewModel @Inject constructor(
    private val repo: InvoiceRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(InvoicesUiState())
    val ui: StateFlow<InvoicesUiState> = _ui

    init { load() }

    fun load() {
        viewModelScope.launch {
            _ui.value = InvoicesUiState(loading = true)
            when (val r = repo.getInvoices()) {
                is ApiResult.Success -> _ui.value = InvoicesUiState(
                    invoices = r.data.invoices,
                    outstandingBalance = r.data.outstandingBalance
                )
                is ApiResult.Error     -> _ui.value = InvoicesUiState(error = r.message)
                is ApiResult.Exception -> _ui.value = InvoicesUiState(error = "Network error")
            }
        }
    }
}
