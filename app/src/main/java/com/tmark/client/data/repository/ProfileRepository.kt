package com.tmark.client.data.repository

import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.api.ApiService
import com.tmark.client.data.api.safeApiCall
import com.tmark.client.data.model.ProfileResponse
import com.tmark.client.data.model.UpdateProfileRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val api: ApiService) {
    suspend fun getProfile(): ApiResult<ProfileResponse> =
        safeApiCall { api.getProfile() }

    suspend fun updateProfile(email: String?, address: String?): ApiResult<ProfileResponse> =
        safeApiCall { api.updateProfile(UpdateProfileRequest(email, address)) }
}
