package com.tmark.client.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.model.DashboardResponse
import com.tmark.client.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = false,
    val data: DashboardResponse? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: DashboardRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(DashboardUiState())
    val ui: StateFlow<DashboardUiState> = _ui

    init { load() }

    fun load() {
        viewModelScope.launch {
            _ui.value = DashboardUiState(loading = true)
            when (val r = repo.getDashboard()) {
                is ApiResult.Success   -> _ui.value = DashboardUiState(data = r.data)
                is ApiResult.Error     -> _ui.value = DashboardUiState(error = r.message)
                is ApiResult.Exception -> _ui.value = DashboardUiState(error = "Network error")
            }
        }
    }
}
