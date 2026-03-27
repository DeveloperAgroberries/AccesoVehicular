package com.AgroberriesMX.accesovehicular.data

import android.util.Log
import com.AgroberriesMX.accesovehicular.data.network.AccesoVehicularApiService
import com.AgroberriesMX.accesovehicular.data.network.request.LoginRequest
import com.AgroberriesMX.accesovehicular.data.network.request.SyncRequest
import com.AgroberriesMX.accesovehicular.data.network.response.LoginsResponse
import com.AgroberriesMX.accesovehicular.domain.Repository
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRecordsModel
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRondinModel
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.TokenModel
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val apiService: AccesoVehicularApiService
) : Repository {
        companion object{
            private const val APP_INFO_TAG_KEY = "AgroAccess"
        }

        override suspend fun getToken(loginRequest: LoginRequest): TokenModel?{
            runCatching { apiService.login(loginRequest) }
                .onSuccess { return it.toDomain() }
                .onFailure { Log.i(APP_INFO_TAG_KEY, "Ha ocurrido un error ${it.message}") }

            return null
        }

        override suspend fun getLogins(syncRequest: SyncRequest): List<LoginModel>?{
            return runCatching {
                val logins: LoginsResponse = apiService.loginsData()
                if (logins.logins.isEmpty()) {
                    throw Exception("No hay logins disponibles")
                }
                logins.logins.mapIndexed{ index, loginResponseItem ->
                    loginResponseItem.toDomain(controlLog = index.toLong())
                }
            }.onFailure {
                Log.i(APP_INFO_TAG_KEY, "Ha ocurrido un error ${it.message}")
            }.getOrNull()
        }

        override suspend fun uploadRecords(records: List<FormattedRecordsModel>): Pair<String?, Int?>{
            return runCatching {
                val response = apiService.uploadData(records)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        Pair(it.message, response.code()) // Retorna el mensaje y el código de estado como Int
                    } ?: Pair(null, response.code()) // Si no hay cuerpo, retorna null
                } else {
                    Pair(null, response.code()) // Si no es exitoso, retorna el código de estado
                }
            }.getOrElse {exception ->
                val errorCode = when (exception) {
                    is HttpException -> exception.code() // Captura el código de la excepción HTTP
                    is IOException -> -1 // Puedes manejar errores de red de otra manera
                    else -> -2
                }
                Pair(null, errorCode)
            }
        }

        override suspend fun uploadRondin(rondines: List<FormattedRondinModel>): Pair<String?, Int?>{
            return runCatching {
                val response = apiService.uploadDataRondin(rondines)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        Pair(it.message, response.code()) // Retorna el mensaje y el código de estado como Int
                    } ?: Pair(null, response.code()) // Si no hay cuerpo, retorna null
                } else {
                    Pair(null, response.code()) // Si no es exitoso, retorna el código de estado
                }
            }.getOrElse {exception ->
                val errorCode = when (exception) {
                    is HttpException -> exception.code() // Captura el código de la excepción HTTP
                    is IOException -> -1 // Puedes manejar errores de red de otra manera
                    else -> -2
                }
                Pair(null, errorCode)
            }
        }
    }