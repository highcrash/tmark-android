package com.tmark.client.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PackageSummary(
    @Json(name = "id") val id: String,
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "heroVideoUrl") val heroVideoUrl: String?,
    @Json(name = "pricePerDay") val pricePerDay: Double,
    @Json(name = "maxQtyPerDay") val maxQtyPerDay: Int,
    @Json(name = "type") val type: String,
    @Json(name = "itemCount") val itemCount: Int
)

@JsonClass(generateAdapter = true)
data class PackageIncludedItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "condition") val condition: String
)

@JsonClass(generateAdapter = true)
data class SubPackageDetail(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "includes") val includes: List<PackageIncludedItem>
)

@JsonClass(generateAdapter = true)
data class PackageDetail(
    @Json(name = "id") val id: String,
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "heroVideoUrl") val heroVideoUrl: String?,
    @Json(name = "pricePerDay") val pricePerDay: Double,
    @Json(name = "maxQtyPerDay") val maxQtyPerDay: Int,
    @Json(name = "type") val type: String,
    @Json(name = "includes") val includes: List<PackageIncludedItem>,
    @Json(name = "subPackages") val subPackages: List<SubPackageDetail> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PackagesResponse(@Json(name = "packages") val packages: List<PackageSummary>)

@JsonClass(generateAdapter = true)
data class PackageDetailResponse(@Json(name = "package") val pkg: PackageDetail)

@JsonClass(generateAdapter = true)
data class CatalogItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "pricePerDay") val pricePerDay: Double,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "condition") val condition: String
)

@JsonClass(generateAdapter = true)
data class ItemsResponse(@Json(name = "items") val items: List<CatalogItem>)
