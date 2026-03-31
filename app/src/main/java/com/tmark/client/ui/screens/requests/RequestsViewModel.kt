package com.tmark.client.ui.screens.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.model.RentalRequest
import com.tmark.client.data.repository.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RequestsUiState(
    val loading: Boolean = false,
    val requests: List<RentalRequest> = emptyList(),
    val error: String? = null,
    val cancelling: String? = null
)

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val repo: RequestRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(RequestsUiState())
    val ui: StateFlow<RequestsUiState> = _ui

    init { load() }

    fun load() {
        viewModelScope.launch {
            _ui.value = RequestsUiState(loading = true)
            when (val r = repo.getRequests()) {
                is ApiResult.Success   -> _ui.value = RequestsUiState(requests = r.data.requests)
                is ApiResult.Error     -> _ui.value = RequestsUiState(error = r.message)
                is ApiResult.Exception -> _ui.value = RequestsUiState(error = "Network error")
            }
        }
    }

    fun cancel(id: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cancelling = id)
            repo.cancelRequest(id)
            load()
        }
    }
}
