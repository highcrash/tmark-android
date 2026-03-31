package com.tmark.client.data.repository

import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.api.ApiService
import com.tmark.client.data.api.safeApiCall
import com.tmark.client.data.model.OrderDetailResponse
import com.tmark.client.data.model.OrdersResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(private val api: ApiService) {
    suspend fun getOrders(): ApiResult<OrdersResponse> =
        safeApiCall { api.getOrders() }

    suspend fun getOrderDetail(id: String): ApiResult<OrderDetailResponse> =
        safeApiCall { api.getOrderDetail(id) }
}
