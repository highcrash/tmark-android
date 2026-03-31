package com.tmark.client.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestItem(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "entityId") val entityId: String,
    @Json(name = "name") val name: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "ratePerDay") val ratePerDay: Double
)

@JsonClass(generateAdapter = true)
data class RentalRequest(
    @Json(name = "id") val id: String,
    @Json(name = "projectName") val projectName: String,
    @Json(name = "projectType") val projectType: String?,
    @Json(name = "projectLocation") val projectLocation: String?,
    @Json(name = "notes") val notes: String?,
    @Json(name = "status") val status: String,
    @Json(name = "adminMessage") val adminMessage: String?,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "requestedDates") val requestedDates: List<String>,
    @Json(name = "estimatedTotal") val estimatedTotal: Double,
    @Json(name = "items") val items: List<RequestItem>
)

@JsonClass(generateAdapter = true)
data class RequestsResponse(@Json(name = "requests") val requests: List<RentalRequest>)

@JsonClass(generateAdapter = true)
data class SelectedItem(
    @Json(name = "type") val type: String,
    @Json(name = "entityId") val entityId: String,
    @Json(name = "quantity") val quantity: Int
)

@JsonClass(generateAdapter = true)
data class ContactBody(
    @Json(name = "designationId") val designationId: String,
    @Json(name = "contactName") val contactName: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "email") val email: String?
)

@JsonClass(generateAdapter = true)
data class NewProductionHouseBody(
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "address") val address: String?,
    @Json(name = "contactPerson") val contactPerson: String?
)

@JsonClass(generateAdapter = true)
data class CreateRequestBody(
    @Json(name = "projectName") val projectName: String,
    @Json(name = "requestedDates") val requestedDates: List<String>,
    @Json(name = "selectedItems") val selectedItems: List<SelectedItem>,
    @Json(name = "notes") val notes: String?,
    @Json(name = "projectType") val projectType: String?,
    @Json(name = "projectLocation") val projectLocation: String?,
    @Json(name = "productionHouseId") val productionHouseId: String?,
    @Json(name = "productionHouse") val productionHouse: NewProductionHouseBody?,
    @Json(name = "contacts") val contacts: List<ContactBody>?
)

@JsonClass(generateAdapter = true)
data class CreateRequestResponse(@Json(name = "request") val request: RentalRequest)

// Phone lookup
@JsonClass(generateAdapter = true)
data class PhoneLookupRequest(@Json(name = "phone") val phone: String)

@JsonClass(generateAdapter = true)
data class PhoneLookupClient(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String?,
    @Json(name = "designationId") val designationId: String?,
    @Json(name = "designationName") val designationName: String?
)

@JsonClass(generateAdapter = true)
data class PhoneLookupResponse(
    @Json(name = "found") val found: Boolean,
    @Json(name = "blocked") val blocked: Boolean?,
    @Json(name = "normalizedPhone") val normalizedPhone: String,
    @Json(name = "client") val client: PhoneLookupClient?
)

// Bootstrap returns a slimmer package shape (no code/description/heroVideoUrl/itemCount)
@JsonClass(generateAdapter = true)
data class BootstrapPackage(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "pricePerDay") val pricePerDay: Double,
    @Json(name = "maxQtyPerDay") val maxQtyPerDay: Int
)

// Bootstrap returns items with maxQtyPerDay instead of quantity, no condition
@JsonClass(generateAdapter = true)
data class BootstrapItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "pricePerDay") val pricePerDay: Double,
    @Json(name = "maxQtyPerDay") val maxQtyPerDay: Int
)

@JsonClass(generateAdapter = true)
data class BootstrapDesignation(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class BootstrapProductionHouse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "address") val address: String?,
    @Json(name = "contactPerson") val contactPerson: String?
)

@JsonClass(generateAdapter = true)
data class BootstrapResponse(
    @Json(name = "packages") val packages: List<BootstrapPackage>,
    @Json(name = "items") val items: List<BootstrapItem>,
    @Json(name = "designations") val designations: List<BootstrapDesignation>,
    @Json(name = "productionHouses") val productionHouses: List<BootstrapProductionHouse>
)
