package com.tmark.client.data.api

import com.tmark.client.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTH ─────────────────────────────────────────────────────────────────
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body body: SendOtpRequest): Response<SendOtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("auth/login-email")
    suspend fun loginEmail(@Body body: LoginEmailRequest): Response<VerifyOtpResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<Unit>

    // ── DASHBOARD ────────────────────────────────────────────────────────────
    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    // ── CATALOG ──────────────────────────────────────────────────────────────
    @GET("catalog/packages")
    suspend fun getPackages(): Response<PackagesResponse>

    @GET("catalog/packages/{id}")
    suspend fun getPackageDetail(@Path("id") id: String): Response<PackageDetailResponse>

    @GET("catalog/items")
    suspend fun getItems(): Response<ItemsResponse>

    // ── REQUESTS ─────────────────────────────────────────────────────────────
    @GET("requests")
    suspend fun getRequests(): Response<RequestsResponse>

    @POST("requests")
    suspend fun createRequest(@Body body: CreateRequestBody): Response<CreateRequestResponse>

    @POST("requests/{id}/cancel")
    suspend fun cancelRequest(@Path("id") id: String): Response<Unit>

    @GET("request/bootstrap")
    suspend fun getBootstrap(): Response<BootstrapResponse>

    @POST("phone-lookup")
    suspend fun lookupPhone(@Body body: PhoneLookupRequest): Response<PhoneLookupResponse>

    // ── ORDERS ───────────────────────────────────────────────────────────────
    @GET("orders")
    suspend fun getOrders(): Response<OrdersResponse>

    @GET("orders/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): Response<OrderDetailResponse>

    // ── INVOICES ─────────────────────────────────────────────────────────────
    @GET("invoices")
    suspend fun getInvoices(): Response<InvoicesResponse>

    // ── PROFILE ──────────────────────────────────────────────────────────────
    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PATCH("profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ProfileResponse>
}
