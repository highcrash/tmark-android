package com.tmark.client.data

import com.tmark.client.data.model.SelectedEquipment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartManager @Inject constructor() {

    private val _items = MutableStateFlow<Map<String, SelectedEquipment>>(emptyMap())
    val items: StateFlow<Map<String, SelectedEquipment>> = _items.asStateFlow()

    private val _selectedDates = MutableStateFlow<List<String>>(emptyList())
    val selectedDates: StateFlow<List<String>> = _selectedDates.asStateFlow()

    val count: Int get() = _items.value.values.sumOf { it.quantity }

    fun add(entityId: String, type: String, name: String, price: Double, maxQty: Int = Int.MAX_VALUE, qty: Int = 1) {
        val current = _items.value.toMutableMap()
        val existing = current[entityId]
        val newQty = ((existing?.quantity ?: 0) + qty).coerceAtMost(maxQty)
        current[entityId] = SelectedEquipment(entityId, type, name, price, newQty, maxQty)
        _items.value = current
    }

    fun decrement(entityId: String) {
        val current = _items.value.toMutableMap()
        val existing = current[entityId] ?: return
        if (existing.quantity <= 1) current.remove(entityId)
        else current[entityId] = existing.copy(quantity = existing.quantity - 1)
        _items.value = current
    }

    fun removeItem(entityId: String) {
        val current = _items.value.toMutableMap()
        current.remove(entityId)
        _items.value = current
    }

    fun setDates(dates: List<String>) { _selectedDates.value = dates }

    fun clear() {
        _items.value = emptyMap()
        _selectedDates.value = emptyList()
    }

    fun snapshot(): Map<String, SelectedEquipment> = _items.value

    fun isEmpty() = _items.value.isEmpty()
}
