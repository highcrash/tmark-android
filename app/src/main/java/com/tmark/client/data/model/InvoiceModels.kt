package com.tmark.client.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InvoiceOrder(
    @Json(name = "orderCode") val orderCode: String,
    @Json(name = "projectName") val projectName: String
)

@JsonClass(generateAdapter = true)
data class InvoiceSummary(
    @Json(name = "id") val id: String,
    @Json(name = "invoiceCode") val invoiceCode: String,
    @Json(name = "status") val status: String,
    @Json(name = "issueDate") val issueDate: String,
    @Json(name = "dueDate") val dueDate: String?,
    @Json(name = "totalAmount") val totalAmount: Double,
    @Json(name = "amountPaid") val amountPaid: Double,
    @Json(name = "balanceDue") val balanceDue: Double,
    @Json(name = "order") val order: InvoiceOrder,
    @Json(name = "pdfUrl") val pdfUrl: String
)

@JsonClass(generateAdapter = true)
data class InvoicesResponse(
    @Json(name = "outstandingBalance") val outstandingBalance: Double,
    @Json(name = "invoices") val invoices: List<InvoiceSummary>
)
