package com.tmark.client.data.repository

import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.api.ApiService
import com.tmark.client.data.api.safeApiCall
import com.tmark.client.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(private val api: ApiService) {
    suspend fun getRequests(): ApiResult<RequestsResponse> =
        safeApiCall { api.getRequests() }

    suspend fun createRequest(body: CreateRequestBody): ApiResult<CreateRequestResponse> =
        safeApiCall { api.createRequest(body) }

    suspend fun cancelRequest(id: String): ApiResult<Unit> =
        safeApiCall { api.cancelRequest(id) }

    suspend fun getBootstrap(): ApiResult<BootstrapResponse> =
        safeApiCall { api.getBootstrap() }

    suspend fun lookupPhone(phone: String): ApiResult<PhoneLookupResponse> =
        safeApiCall { api.lookupPhone(PhoneLookupRequest(phone)) }
}
