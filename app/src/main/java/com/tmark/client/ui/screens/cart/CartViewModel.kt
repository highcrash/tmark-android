package com.tmark.client.ui.screens.cart

import androidx.lifecycle.ViewModel
import com.tmark.client.data.CartManager
import com.tmark.client.data.model.SelectedEquipment
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartManager: CartManager
) : ViewModel() {

    val items = cartManager.items
    val selectedDates = cartManager.selectedDates

    fun increment(eq: SelectedEquipment) {
        cartManager.add(eq.entityId, eq.type, eq.name, eq.pricePerDay, eq.maxQtyPerDay, 1)
    }

    fun decrement(entityId: String) {
        cartManager.decrement(entityId)
    }

    fun remove(entityId: String) {
        cartManager.removeItem(entityId)
    }

    fun setDates(dates: List<String>) {
        cartManager.setDates(dates)
    }
}
