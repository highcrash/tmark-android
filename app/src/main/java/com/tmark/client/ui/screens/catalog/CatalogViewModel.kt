package com.tmark.client.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.CartManager
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.model.CatalogItem
import com.tmark.client.data.model.PackageDetail
import com.tmark.client.data.model.PackageSummary
import com.tmark.client.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogUiState(
    val loading: Boolean = false,
    val packages: List<PackageSummary> = emptyList(),
    val items: List<CatalogItem> = emptyList(),
    val error: String? = null
)

data class PackageDetailUiState(
    val loading: Boolean = true,
    val pkg: PackageDetail? = null,
    val error: String? = null
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repo: CatalogRepository,
    private val cartManager: CartManager
) : ViewModel() {

    private val _ui = MutableStateFlow(CatalogUiState())
    val ui: StateFlow<CatalogUiState> = _ui

    private val _detail = MutableStateFlow(PackageDetailUiState())
    val detail: StateFlow<PackageDetailUiState> = _detail

    private val _selectedType = MutableStateFlow("")
    val selectedType: StateFlow<String> = _selectedType

    // Cart state exposed to UI
    val cartItems = cartManager.items
    val cartCount: StateFlow<Int> = cartManager.items
        .map { it.values.sumOf { eq -> eq.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Unique package types for filter chips
    val packageTypes: StateFlow<List<String>> = _ui
        .map { state -> state.packages.map { it.type }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Packages filtered by selected type
    val filteredPackages: StateFlow<List<PackageSummary>> = combine(_ui, _selectedType) { state, type ->
        if (type.isBlank()) state.packages else state.packages.filter { it.type == type }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init { load() }

    fun load() {
        viewModelScope.launch {
            _ui.value = CatalogUiState(loading = true)
            val pkgResult  = repo.getPackages()
            val itemResult = repo.getItems()
            val pkgs  = if (pkgResult  is ApiResult.Success) pkgResult.data.packages  else emptyList()
            val items = if (itemResult is ApiResult.Success) itemResult.data.items     else emptyList()
            val err = when {
                pkgResult  is ApiResult.Error -> pkgResult.message
                itemResult is ApiResult.Error -> itemResult.message
                else -> null
            }
            _ui.value = CatalogUiState(loading = false, packages = pkgs, items = items, error = err)
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detail.value = PackageDetailUiState(loading = true)
            when (val r = repo.getPackageDetail(id)) {
                is ApiResult.Success   -> _detail.value = PackageDetailUiState(loading = false, pkg = r.data.pkg)
                is ApiResult.Error     -> _detail.value = PackageDetailUiState(loading = false, error = r.message)
                is ApiResult.Exception -> _detail.value = PackageDetailUiState(loading = false, error = "Network error")
            }
        }
    }

    fun setTypeFilter(type: String) {
        _selectedType.value = if (_selectedType.value == type) "" else type
    }

    fun addToCart(entityId: String, type: String, name: String, price: Double, maxQty: Int = Int.MAX_VALUE, qty: Int = 1) {
        cartManager.add(entityId, type, name, price, maxQty, qty)
    }

    fun decrementCart(entityId: String) {
        cartManager.decrement(entityId)
    }
}
