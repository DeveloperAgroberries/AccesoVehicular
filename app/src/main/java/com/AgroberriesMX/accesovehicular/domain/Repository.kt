package com.AgroberriesMX.accesovehicular.domain

import com.AgroberriesMX.accesovehicular.data.network.request.LoginRequest
import com.AgroberriesMX.accesovehicular.data.network.request.SyncRequest
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRecordsModel
import com.AgroberriesMX.accesovehicular.domain.model.FormattedRondinModel
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.TokenModel

interface Repository {
    suspend fun getToken(loginRequest: LoginRequest): TokenModel?
    suspend fun getLogins(syncRequest: SyncRequest): List<LoginModel>?
    suspend fun uploadRecords(records: List<FormattedRecordsModel>): Pair<String?, Int?>
    suspend fun uploadRondin(rondines: List<FormattedRondinModel>): Pair<String?, Int?>
}