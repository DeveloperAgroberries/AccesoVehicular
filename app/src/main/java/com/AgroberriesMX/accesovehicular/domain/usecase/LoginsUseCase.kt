package com.AgroberriesMX.accesovehicular.domain.usecase

import com.AgroberriesMX.accesovehicular.data.network.request.SyncRequest
import com.AgroberriesMX.accesovehicular.domain.Repository
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import javax.inject.Inject

class LoginsUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(syncRequest: SyncRequest): List<LoginModel>? = repository.getLogins(syncRequest)
}