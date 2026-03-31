package com.tmark.client.data.repository

import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.api.ApiService
import com.tmark.client.data.api.safeApiCall
import com.tmark.client.data.model.DashboardResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(private val api: ApiService) {
    suspend fun getDashboard(): ApiResult<DashboardResponse> =
        safeApiCall { api.getDashboard() }
}
