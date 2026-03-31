package com.tmark.client.data.repository

import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.api.ApiService
import com.tmark.client.data.api.safeApiCall
import com.tmark.client.data.model.InvoicesResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(private val api: ApiService) {
    suspend fun getInvoices(): ApiResult<InvoicesResponse> =
        safeApiCall { api.getInvoices() }
}
