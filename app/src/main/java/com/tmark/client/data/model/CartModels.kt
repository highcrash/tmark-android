package com.tmark.client.data.model

data class SelectedEquipment(
    val entityId: String,
    val type: String,
    val name: String,
    val pricePerDay: Double,
    val quantity: Int,
    val maxQtyPerDay: Int = Int.MAX_VALUE
)
