package com.tmark.client.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderDate(
    @Json(name = "date") val date: String,
    @Json(name = "cancelled") val cancelled: Boolean
)

@JsonClass(generateAdapter = true)
data class OrderSummary(
    @Json(name = "id") val id: String,
    @Json(name = "orderCode") val orderCode: String,
    @Json(name = "projectName") val projectName: String,
    @Json(name = "status") val status: String,
    @Json(name = "totalAmount") val totalAmount: Double,
    @Json(name = "amountPaid") val amountPaid: Double,
    @Json(name = "balanceDue") val balanceDue: Double,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "dates") val dates: List<OrderDate>
)

@JsonClass(generateAdapter = true)
data class OrdersResponse(@Json(name = "orders") val orders: List<OrderSummary>)

@JsonClass(generateAdapter = true)
data class OrderDetailItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "ratePerDay") val ratePerDay: Double,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "activeDays") val activeDays: Int,
    @Json(name = "subtotal") val subtotal: Double
)

@JsonClass(generateAdapter = true)
data class OrderPayment(
    @Json(name = "id") val id: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "dateReceived") val dateReceived: String,
    @Json(name = "method") val method: String,
    @Json(name = "reference") val reference: String?
)

@JsonClass(generateAdapter = true)
data class OrderContact(
    @Json(name = "contactName") val contactName: String,
    @Json(name = "designation") val designation: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "email") val email: String?
)

@JsonClass(generateAdapter = true)
data class OrderDetail(
    @Json(name = "id") val id: String,
    @Json(name = "orderCode") val orderCode: String,
    @Json(name = "projectName") val projectName: String,
    @Json(name = "projectType") val projectType: String?,
    @Json(name = "projectLocation") val projectLocation: String?,
    @Json(name = "status") val status: String,
    @Json(name = "totalAmount") val totalAmount: Double,
    @Json(name = "amountPaid") val amountPaid: Double,
    @Json(name = "balanceDue") val balanceDue: Double,
    @Json(name = "dates") val dates: List<OrderDate>,
    @Json(name = "items") val items: List<OrderDetailItem>,
    @Json(name = "payments") val payments: List<OrderPayment>,
    @Json(name = "contacts") val contacts: List<OrderContact>
)

@JsonClass(generateAdapter = true)
data class OrderDetailResponse(@Json(name = "order") val order: OrderDetail)
