package com.tmark.client.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LastInvoice(
    @Json(name = "id") val id: String,
    @Json(name = "invoiceCode") val invoiceCode: String,
    @Json(name = "issueDate") val issueDate: String,
    @Json(name = "totalAmount") val totalAmount: Double,
    @Json(name = "balanceDue") val balanceDue: Double,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class DashboardResponse(
    @Json(name = "pendingRequests") val pendingRequests: Int,
    @Json(name = "activeOrders") val activeOrders: Int,
    @Json(name = "outstandingBalance") val outstandingBalance: Double,
    @Json(name = "lastInvoice") val lastInvoice: LastInvoice?
)
