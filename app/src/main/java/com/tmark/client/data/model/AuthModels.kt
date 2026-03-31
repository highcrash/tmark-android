package com.tmark.client.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendOtpRequest(@Json(name = "phone") val phone: String)

@JsonClass(generateAdapter = true)
data class SendOtpResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "normalizedPhone") val normalizedPhone: String,
    @Json(name = "newUser") val newUser: Boolean = false
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String?,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "normalizedPhone") val normalizedPhone: String
)

@JsonClass(generateAdapter = true)
data class LoginEmailRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "otp") val otp: String
)

@JsonClass(generateAdapter = true)
data class ClientProfile(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "address") val address: String?
)

@JsonClass(generateAdapter = true)
data class AuthUser(
    @Json(name = "id") val id: String,
    @Json(name = "role") val role: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String?,
    @Json(name = "phone") val phone: String?
)

@JsonClass(generateAdapter = true)
data class VerifyOtpResponse(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "tokenType") val tokenType: String,
    @Json(name = "expiresIn") val expiresIn: Long,
    @Json(name = "user") val user: AuthUser,
    @Json(name = "client") val client: ClientProfile
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "currentPassword") val currentPassword: String?,
    @Json(name = "newPassword") val newPassword: String
)
