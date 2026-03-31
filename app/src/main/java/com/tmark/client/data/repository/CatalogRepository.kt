package com.tmark.client.data.repository

import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.api.ApiService
import com.tmark.client.data.api.safeApiCall
import com.tmark.client.data.model.ItemsResponse
import com.tmark.client.data.model.PackageDetailResponse
import com.tmark.client.data.model.PackagesResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(private val api: ApiService) {
    suspend fun getPackages(): ApiResult<PackagesResponse> =
        safeApiCall { api.getPackages() }

    suspend fun getPackageDetail(id: String): ApiResult<PackageDetailResponse> =
        safeApiCall { api.getPackageDetail(id) }

    suspend fun getItems(): ApiResult<ItemsResponse> =
        safeApiCall { api.getItems() }
}
