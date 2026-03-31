package com.tmark.client.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileDesignation(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class Profile(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "address") val address: String?,
    @Json(name = "nid") val nid: String?,
    @Json(name = "allowPhoneLookup") val allowPhoneLookup: Boolean,
    @Json(name = "memberSince") val memberSince: String,
    @Json(name = "designation") val designation: ProfileDesignation?,
    @Json(name = "hasPassword") val hasPassword: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ProfileResponse(@Json(name = "profile") val profile: Profile)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "email") val email: String?,
    @Json(name = "address") val address: String?
)
