package com.tmark.client.data.repository

import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.api.ApiService
import com.tmark.client.data.api.safeApiCall
import com.tmark.client.data.local.TokenStore
import com.tmark.client.data.model.ChangePasswordRequest
import com.tmark.client.data.model.LoginEmailRequest
import com.tmark.client.data.model.RegisterRequest
import com.tmark.client.data.model.RegisterResponse
import com.tmark.client.data.model.SendOtpRequest
import com.tmark.client.data.model.SendOtpResponse
import com.tmark.client.data.model.VerifyOtpRequest
import com.tmark.client.data.model.VerifyOtpResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {
    suspend fun sendOtp(phone: String): ApiResult<SendOtpResponse> =
        safeApiCall { api.sendOtp(SendOtpRequest(phone)) }

    suspend fun register(phone: String, name: String, email: String?, password: String): ApiResult<RegisterResponse> =
        safeApiCall { api.register(RegisterRequest(phone, name, email, password)) }

    suspend fun loginEmail(email: String, password: String): ApiResult<VerifyOtpResponse> {
        val result = safeApiCall { api.loginEmail(LoginEmailRequest(email, password)) }
        if (result is ApiResult.Success) {
            val data = result.data
            tokenStore.saveAuth(
                token      = data.accessToken,
                clientId   = data.user.id,
                clientName = data.user.name,
                phone      = data.user.phone
            )
        }
        return result
    }

    suspend fun verifyOtp(phone: String, code: String): ApiResult<VerifyOtpResponse> {
        val result = safeApiCall { api.verifyOtp(VerifyOtpRequest(phone, code)) }
        if (result is ApiResult.Success) {
            val data = result.data
            tokenStore.saveAuth(
                token      = data.accessToken,
                clientId   = data.user.id,
                clientName = data.user.name,
                phone      = data.user.phone
            )
        }
        return result
    }

    suspend fun logout() {
        safeApiCall { api.logout() }
        tokenStore.clearAuth()
    }

    suspend fun changePassword(current: String?, newPw: String): ApiResult<Unit> =
        safeApiCall { api.changePassword(ChangePasswordRequest(current, newPw)) }
}
