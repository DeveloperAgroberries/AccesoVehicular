package com.AgroberriesMX.accesovehicular.data.network

import com.AgroberriesMX.accesovehicular.data.network.request.LoginRequest
import com.AgroberriesMX.accesovehicular.data.network.request.UploadResponse
import com.AgroberriesMX.accesovehicular.data.network.response.LoginResponse
import com.AgroberriesMX.accesovehicular.data.network.response.LoginsResponse
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRecordsModel
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRondinModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AccesoVehicularApiService {
    @POST("LoginUser")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("ListLogins")
    suspend fun loginsData(): LoginsResponse

    @POST("SaveIncome")
    suspend fun uploadData(@Body records: List<FormattedRecordsModel>): Response<UploadResponse>

    @POST("SaveRondin")
    suspend fun uploadDataRondin(@Body rondines: List<FormattedRondinModel>): Response<UploadResponse>
}